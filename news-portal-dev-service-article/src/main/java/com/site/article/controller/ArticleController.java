package com.site.article.controller;

import com.mongodb.client.gridfs.GridFSBucket;
import com.site.api.BaseController;
import com.site.api.config.RabbitMQConfig;
import com.site.api.controller.article.ArticleControllerApi;
import com.site.article.service.ArticlePortalService;
import com.site.article.service.ArticleService;
import com.site.enums.ArticleCoverType;
import com.site.enums.ArticleReviewStatus;
import com.site.enums.YesOrNo;
import com.site.exception.GraceException;
import com.site.grace.result.GraceJSONResult;
import com.site.grace.result.ResponseStatusEnum;
import com.site.pojo.Category;
import com.site.pojo.bo.NewArticleBO;
import com.site.pojo.vo.AppUserVO;
import com.site.pojo.vo.ArticleDetailVO;
import com.site.utils.JsonUtils;
import com.site.utils.PagedGridResult;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.Writer;
import java.util.*;

@RestController
public class ArticleController extends BaseController implements ArticleControllerApi {

    final static Logger logger = LoggerFactory.getLogger(ArticleController.class);

    @Autowired
    private ArticleService articleService;

    @Override
    public GraceJSONResult createArticle(@Valid NewArticleBO newArticleBO) {

        // ***** The errors are handled by com.site.exception.GraceExceptionHandler.java
        // Validate BindingResult, if false, return
//        if (result.hasErrors()) {
//            Map<String, String> errorMap = getErrors(result);
//            return GraceJSONResult.errorMap(errorMap);
//        }

        // Check article cover type. If cover enabled, cover cannot be empty, otherwise cover is empty.
        if (newArticleBO.getArticleType() == ArticleCoverType.ONE_IMAGE.type) {
            if (StringUtils.isBlank(newArticleBO.getArticleCover())) {
                return GraceJSONResult.errorCustom(ResponseStatusEnum.ARTICLE_COVER_NOT_EXIST_ERROR);
            }
        } else if (newArticleBO.getArticleType() == ArticleCoverType.WORDS.type) {
            newArticleBO.setArticleCover("");
        }

        // Check if category id is exist -> query Redis
        String allCatJson = redis.get(REDIS_ALL_CATEGORY);
        Category temp = null;
        if (StringUtils.isBlank(allCatJson)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.SYSTEM_OPERATION_ERROR);
        } else {
            List<Category> catList = JsonUtils.jsonToList(allCatJson, Category.class);

            for (Category c : catList) {
                if (c.getId() == newArticleBO.getCategoryId()) {
                    temp = c;
                    break;
                }
            }
            if (temp == null) {
                return GraceJSONResult.errorCustom(ResponseStatusEnum.ARTICLE_CATEGORY_NOT_EXIST_ERROR);
            }
        }
//        System.out.println(newArticleBO.toString());
        articleService.createArticle(newArticleBO, temp);
        return GraceJSONResult.ok();
    }

    @Override
    public GraceJSONResult queryMyList(String userId, String keyword, Integer status, Date startDate,
                                       Date endDate, Integer page, Integer pageSize) {

        if (StringUtils.isBlank(userId)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.ARTICLE_QUERY_PARAMS_ERROR);
        }

        if (page == null) {
            page = COMMON_START_PAGE;
        }
        if (pageSize == null) {
            pageSize = COMMON_PAGE_SIZE;
        }

        // Query article list by using service
        PagedGridResult grid = articleService.queryMyArticleList(userId, keyword, status, startDate, endDate, page, pageSize);

        return GraceJSONResult.ok(grid);
    }

    @Override
    public GraceJSONResult queryAllList(Integer status, Integer page, Integer pageSize) {

        if (page == null) {
            page = COMMON_START_PAGE;
        }

        if (pageSize == null) {
            pageSize = COMMON_PAGE_SIZE;
        }

        PagedGridResult gridResult = articleService.queryAllArticleListAdmin(status, page, pageSize);

        return GraceJSONResult.ok(gridResult);

    }

    @Override
    public GraceJSONResult doReview(String articleId, Integer passOrNot) {
        Integer pendingStatus;
        if (passOrNot == YesOrNo.YES.type) {
            // Review passed
            pendingStatus = ArticleReviewStatus.SUCCESS.type;
        } else if (passOrNot == YesOrNo.NO.type) {
            // Review Failed
            pendingStatus = ArticleReviewStatus.FAILED.type;
        } else {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.ARTICLE_REVIEW_ERROR);
        }

        // Update database with review status
        articleService.updateArticleStatus(articleId, pendingStatus);

        if (pendingStatus == ArticleReviewStatus.SUCCESS.type) {
            // Review passed. Generate static HTML for article contents
            generateStaticHTML(articleId);
        }
        return GraceJSONResult.ok();
    }

    public void generateStaticHTML(String articleId) {
            try {
                createArticleHTML(articleId);
                String articleMongoId = createArticleHTMLToGridFS(articleId);
                // Save the id to relevant article
                articleService.updateArticleToGridFS(articleId, articleMongoId);
                // Download static html
//                doDownloadArticleHTML(articleId, articleMongoId);
                // Send request to MQ queue. Make consumer listen and download static html
                doDownloadArticleHTMLByMQ(articleId, articleMongoId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    @Value("${freemarker.html.article}")
    private String articlePath;

    private void doDownloadArticleHTML(String articleId, String articleMongoId) {
        String url = "http://html.inews.com:8002/article/html/download?articleId=" +
                articleId + "&articleMongoId=" + articleMongoId;
        ResponseEntity<Integer> responseEntity =  restTemplate.getForEntity(url, Integer.class);
        int status = responseEntity.getBody();
        if (status != HttpStatus.OK.value()) {
            GraceException.display(ResponseStatusEnum.ARTICLE_REVIEW_ERROR);
        }
    }

    @Autowired
    private RabbitTemplate rabbitTemplate;
    private void doDownloadArticleHTMLByMQ(String articleId, String articleMongoId) {

        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_ARTICLE,
                "article.download.do",
                articleId + "," + articleMongoId);

    }

    // Generate static article HTML
    public void createArticleHTML(String articleId) throws Exception {

        Template template = getTemplate(articleId);

        // Get detailed data from article
        ArticleDetailVO detailVO = getArticleDetail(articleId);
        Map<String, Object> map = new HashMap<>();
        map.put("articleDetail", detailVO);

        File tempDic = new File(articlePath);
        if (!tempDic.exists()) {
            tempDic.mkdirs();
        }

        String path = articlePath + File.separator + detailVO.getId() + ".html";
        Writer out = new FileWriter(path);
        template.process(map, out);
        out.close();
    }

    @Autowired
    private GridFSBucket gridFSBucket;

    private Template getTemplate(String articleId) throws Exception{
        Configuration cfg = new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
        String classPath = this.getClass().getResource("/").getPath();
        cfg.setDirectoryForTemplateLoading(new File(classPath.replace("%20", " ") + "templates"));

        return cfg.getTemplate("detail.ftl", "utf-8");
    }

    // Generate static article HTML and send to gridFS
    public String createArticleHTMLToGridFS (String articleId) throws Exception {

        Template template = getTemplate(articleId);

        // Get detailed data from article
        ArticleDetailVO detailVO = getArticleDetail(articleId);
        Map<String, Object> map = new HashMap<>();
        map.put("articleDetail", detailVO);

        String htmlContent = FreeMarkerTemplateUtils.processTemplateIntoString(template, map);
//        System.out.println(htmlContent);

        InputStream inputStream = IOUtils.toInputStream(htmlContent);

        ObjectId fileId = gridFSBucket.uploadFromStream(detailVO.getId() + ".html", inputStream);
        return fileId.toString();
    }

    // Set up a restAPI request, get detailed info of the article
    public ArticleDetailVO getArticleDetail(String articleId) {
        String url = "http://www.inews.com:8001/portal/article/detail?articleId="
                + articleId;
        ResponseEntity<GraceJSONResult> responseEntity
                = restTemplate.getForEntity(url, GraceJSONResult.class);
        GraceJSONResult bodyResult = responseEntity.getBody();
        ArticleDetailVO detailVO = null;
        if (bodyResult.getStatus() == 200) {
            String detailJson = JsonUtils.objectToJson(bodyResult.getData());
            detailVO = JsonUtils.jsonToPojo(detailJson, ArticleDetailVO.class);
        }

        return detailVO;
    }



    @Override
    public GraceJSONResult delete(String userId, String articleId) {
        articleService.deleteArticle(userId, articleId);
        return GraceJSONResult.ok();
    }

    @Override
    public GraceJSONResult withdraw(String userId, String articleId) {
        articleService.withdrawArticle(userId, articleId);
        return GraceJSONResult.ok();
    }
}
