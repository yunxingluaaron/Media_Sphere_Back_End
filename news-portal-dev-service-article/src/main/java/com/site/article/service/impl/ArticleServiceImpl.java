package com.site.article.service.impl;


import com.github.pagehelper.PageHelper;
import com.mongodb.client.gridfs.GridFSBucket;
import com.site.api.config.RabbitMQConfig;
import com.site.api.config.RabbitMQDelayConfig;
import com.site.api.service.BaseService;
import com.site.article.mapper.ArticleMapper;
import com.site.article.mapper.ArticleMapperCustom;
import com.site.article.service.ArticleService;
import com.site.enums.ArticleAppointType;
import com.site.enums.ArticleReviewLevel;
import com.site.enums.ArticleReviewStatus;
import com.site.enums.YesOrNo;
import com.site.exception.GraceException;
import com.site.grace.result.ResponseStatusEnum;
import com.site.pojo.Article;
import com.site.pojo.Category;
import com.site.pojo.bo.NewArticleBO;
import com.site.pojo.eo.ArticleEO;
import com.site.utils.PagedGridResult;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.n3r.idworker.Sid;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.IndexQueryBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.List;

@Service
public class ArticleServiceImpl extends BaseService implements ArticleService {

    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private ArticleMapperCustom articleMapperCustom;

    @Autowired
    private Sid sid;

    @Autowired
    private ElasticsearchTemplate esTemplate;

    @Transactional
    @Override
    public void createArticle(NewArticleBO newArticleBO, Category category) {
        String articleId = sid.nextShort();

        Article article = new Article();
        BeanUtils.copyProperties(newArticleBO, article);

        article.setId(articleId);

        article.setCategoryId(category.getId());
        article.setArticleStatus(ArticleReviewStatus.REVIEWING.type);
        article.setCommentCounts(0);
        article.setReadCounts(0);
        article.setIsDelete(YesOrNo.NO.type);
        article.setCreateTime(new Date());
        article.setUpdateTime(new Date());

        if (article.getIsAppoint() == ArticleAppointType.TIMING.type) {
            article.setPublishTime(newArticleBO.getPublishTime());
        } else if (article.getIsAppoint() == ArticleAppointType.IMMEDIATELY.type) {
            article.setPublishTime(new Date());
        }

        int res = articleMapper.insert(article);
        if (res != 1) {
            GraceException.display(ResponseStatusEnum.ARTICLE_CREATE_ERROR);
        }

        // Send delayed message to MQ. Calculate the difference between the scheduled time and current time.
        // Set the delayed time with time difference
        if (article.getIsAppoint() == ArticleAppointType.TIMING.type) {

            Date scheduledTime = newArticleBO.getPublishTime();
            Date now = new Date();

            int delayTimes = (int)(scheduledTime.getTime() - now.getTime());

            MessagePostProcessor messagePostProcessor = new MessagePostProcessor() {
                @Override
                public Message postProcessMessage(Message message) throws AmqpException {
                    // Set persistence mode for messages
                    message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                    // Set the delayed time for messages(ms)
                    message.getMessageProperties().setDelay(delayTimes);
                    return message;
                }
            };

            rabbitTemplate.convertAndSend(RabbitMQDelayConfig.EXCHANGE_DELAY,
                    "publish.delay.display",
                    articleId,
                    messagePostProcessor);

            System.out.println("Delayed message (timed article posting): " + new Date());

        }


        // Check article context by cloud AI (Automated Review)
        String reviewTextResult = ArticleReviewLevel.REVIEW.type;


        if (reviewTextResult.equalsIgnoreCase(ArticleReviewLevel.PASS.type)) {
            // update article status to "passed"
            this.updateArticleStatus(articleId, ArticleReviewStatus.SUCCESS.type);
        } else if (reviewTextResult.equalsIgnoreCase(ArticleReviewLevel.REVIEW.type)) {
            // update article status to "Manual review required"
            this.updateArticleStatus(articleId, ArticleReviewStatus.REVIEWING.type);
        } else if (reviewTextResult.equalsIgnoreCase(ArticleReviewLevel.BLOCK.type)) {
            // update article status to "Blocked"
            this.updateArticleStatus(articleId, ArticleReviewStatus.FAILED.type);
        }

    }

    @Transactional
    @Override
    public void updateArticleStatus(String articleId, Integer pendingStatus) {
        Example example = new Example(Article.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("id", articleId);

        Article pendingArticle = new Article();
        pendingArticle.setArticleStatus(pendingStatus);

        int  res = articleMapper.updateByExampleSelective(pendingArticle, example);
        if (res != 1) {
            GraceException.display(ResponseStatusEnum.ARTICLE_REVIEW_ERROR);
        }

        // If passed check, quary article and store article info into es.
        if (pendingStatus == ArticleReviewStatus.SUCCESS.type){
            Article result =  articleMapper.selectByPrimaryKey(articleId);
            // if the article is posted to be published immediately, save the article into es after content check.
            if (result.getIsAppoint() == ArticleAppointType.IMMEDIATELY.type){
                ArticleEO articleEO = new ArticleEO();
                BeanUtils.copyProperties(result, articleEO); // they hace common properties in common
                IndexQuery iq = new IndexQueryBuilder().withObject(articleEO).build();
                esTemplate.index(iq);
            }
            // If the article post time has a postponed time, it won't be send to es here.
            // The article will be send to the postponed queue
        }

    }

    @Transactional
    @Override
    public void updateArticleToGridFS(String articleId, String articleMongoId) {
        Article pendingArticle = new Article();
        pendingArticle.setId(articleId);
        pendingArticle.setMongoFileId(articleMongoId);
        int  res = articleMapper.updateByPrimaryKeySelective(pendingArticle);
        if (res != 1) {
            GraceException.display(ResponseStatusEnum.ARTICLE_REVIEW_ERROR);
        }
    }

    @Transactional
    @Override
    public void updateAppointToPublish() {
        articleMapperCustom.updateAppointToPublish();
    }

    @Transactional
    @Override
    public void updateArticleToPublish(String articleId) {
        Article article = new Article();
        article.setId(articleId);
        article.setIsAppoint(ArticleAppointType.IMMEDIATELY.type);
        articleMapper.updateByPrimaryKeySelective(article);
    }

    @Override
    public PagedGridResult queryMyArticleList(String userId, String keyword, Integer status, Date startDate,
                                              Date endDate, Integer page, Integer pageSize) {

//        System.out.println(startDate.toString() + " " + endDate.toString());
        Example example = new Example(Article.class);
        example.orderBy("createTime").desc();
        Example.Criteria criteria = example.createCriteria();

        criteria.andEqualTo("publishUserId", userId);

        if (StringUtils.isNotBlank(keyword)) {
            criteria.andLike("title", "%" + keyword + "%");
        }

        if (startDate != null) {
            criteria.andGreaterThanOrEqualTo("publishTime", startDate);
        }
        if (endDate != null) {
            criteria.andLessThanOrEqualTo("publishTime", endDate);
        }

        if (ArticleReviewStatus.isArticleStatusValid(status)) {

            criteria.andEqualTo("articleStatus", status);
        }

        if (status != null && status == 12 ) {
            criteria.andEqualTo("articleStatus", ArticleReviewStatus.REVIEWING.type)
                    .orEqualTo("articleStatus", ArticleReviewStatus.WAITING_MANUAL.type);
        }

        criteria.andEqualTo("isDelete", YesOrNo.NO.type);

        PageHelper.startPage(page, pageSize);
        List<Article> list = articleMapper.selectByExample(example);

        return setterPagedGrid(list, page);
    }

    @Override
    public PagedGridResult queryAllArticleListAdmin(Integer status, Integer page, Integer pageSize) {
        Example articleExample = new Example(Article.class);
        articleExample.orderBy("createTime").desc();

        Example.Criteria criteria = articleExample.createCriteria();
        if (ArticleReviewStatus.isArticleStatusValid(status)) {
            criteria.andEqualTo("articleStatus", status);
        }

        // Under review
        if (status != null && status == 12) {
            criteria.andEqualTo("articleStatus", ArticleReviewStatus.REVIEWING.type)
                    .orEqualTo("articleStatus", ArticleReviewStatus.WAITING_MANUAL.type);
        }

        //isDelete must be 0
        criteria.andEqualTo("isDelete", YesOrNo.NO.type);

        /**
         * page: page Num
         * pageSize: Item number on each page
         */
        PageHelper.startPage(page, pageSize);
        List<Article> list = articleMapper.selectByExample(articleExample);
        return setterPagedGrid(list, page);
    }

    @Transactional
    @Override
    public void deleteArticle(String userId, String articleId) {
        Example articleExample = makeExampleCriteria(userId, articleId);

        Article pending = new Article();
        pending.setIsDelete(YesOrNo.YES.type);

        int result = articleMapper.updateByExampleSelective(pending, articleExample);
        if (result != 1) {
            GraceException.display(ResponseStatusEnum.ARTICLE_DELETE_ERROR);
        }
        deleteHTML(articleId);

        esTemplate.delete(ArticleEO.class, articleId);
    }

    @Autowired
    private GridFSBucket gridFSBucket;
    /**
     * 文章撤回删除后，删除静态化的html
     */
    private void deleteHTML(String articleId) {
        // 1. Query article's mongoFileId
        Article pending = articleMapper.selectByPrimaryKey(articleId);
        if (StringUtils.isBlank(pending.getMongoFileId())) {
            return;
        }

        String articleMongoId = pending.getMongoFileId();

        // 2. Delete static html in GridFS
        gridFSBucket.delete(new ObjectId(articleMongoId));

        // 3. Delete static HTML in front end server
//        doDeleteArticleHTML(articleId);
        doDeleteArticleHTMLByMQ(articleId);
    }

    @Autowired
    public RestTemplate restTemplate;
    private void doDeleteArticleHTML(String articleId) {
        String url = "http://html.inews.com:8002/article/html/delete?articleId=" + articleId;
        ResponseEntity<Integer> responseEntity = restTemplate.getForEntity(url, Integer.class);
        int status = responseEntity.getBody();
        if (status != HttpStatus.OK.value()) {
            GraceException.display(ResponseStatusEnum.SYSTEM_OPERATION_ERROR);
        }
    }

    @Autowired
    private RabbitTemplate rabbitTemplate;
    private void doDeleteArticleHTMLByMQ(String articleId) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_ARTICLE,
                "article.html.download.do", articleId);
    }

    @Transactional
    @Override
    public void withdrawArticle(String userId, String articleId) {
        Example articleExample = makeExampleCriteria(userId, articleId);

        Article pending = new Article();
        pending.setArticleStatus(ArticleReviewStatus.WITHDRAW.type);

        int result = articleMapper.updateByExampleSelective(pending, articleExample);
        if (result != 1) {
            GraceException.display(ResponseStatusEnum.ARTICLE_WITHDRAW_ERROR);
        }
        deleteHTML(articleId);
        esTemplate.delete(ArticleEO.class, articleId);
    }

    private Example makeExampleCriteria(String userId, String articleId) {
        Example articleExample = new Example(Article.class);
        Example.Criteria criteria = articleExample.createCriteria();
        criteria.andEqualTo("publishUserId", userId);
        criteria.andEqualTo("id", articleId);
        return articleExample;
    }
}
