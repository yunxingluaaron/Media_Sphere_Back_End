package com.site.article.controller;

import com.site.api.BaseController;
import com.site.api.controller.article.ArticleControllerApi;
import com.site.api.controller.article.ArticlePortalControllerApi;
import com.site.api.controller.user.UserControllerApi;
import com.site.article.service.ArticlePortalService;
import com.site.article.service.ArticleService;
import com.site.enums.ArticleCoverType;
import com.site.enums.ArticleReviewStatus;
import com.site.enums.YesOrNo;
import com.site.grace.result.GraceJSONResult;
import com.site.grace.result.ResponseStatusEnum;
import com.site.pojo.Article;
import com.site.pojo.Category;
import com.site.pojo.bo.NewArticleBO;
import com.site.pojo.eo.ArticleEO;
import com.site.pojo.vo.AppUserVO;
import com.site.pojo.vo.ArticleDetailVO;
import com.site.pojo.vo.IndexArticleVO;
import com.site.utils.IPUtil;
import com.site.utils.JsonUtils;
import com.site.utils.PagedGridResult;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.units.qual.A;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import springfox.documentation.spring.web.json.Json;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.*;

@RestController
public class ArticlePortalController extends BaseController implements ArticlePortalControllerApi {

    final static Logger logger = LoggerFactory.getLogger(ArticlePortalController.class);

    @Autowired
    private ArticlePortalService articlePortalService;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ElasticsearchTemplate esTemplate;

    @Override
    public GraceJSONResult eslist(String keyword, Integer category, Integer page, Integer pageSize) {

        /*
        * ES query:
        *   1. query on homepage, without any parameter
        *   2. quary by article category
        *   3. query by key works of article title
        * */

        // page number of es is counted from 0.
        if (page < 1) return null;
        page--;
        Pageable pageable = PageRequest.of(page, pageSize);
        SearchQuery query = null;
        AggregatedPage<ArticleEO> pagedArticle = null;
        // match case 1:
        if (StringUtils.isBlank(keyword) && category == null) {
            query = new NativeSearchQueryBuilder().withQuery(QueryBuilders.matchAllQuery())
                    .withPageable(pageable)
                    .build();
            pagedArticle =  esTemplate.queryForPage(query, ArticleEO.class);
        }
        // match case 2:
        if (StringUtils.isBlank(keyword) && category != null) {
            query = new NativeSearchQueryBuilder()
                    .withQuery(QueryBuilders.termQuery("categoryId", category))
                    .withPageable(pageable)
                    .build();
            pagedArticle =  esTemplate.queryForPage(query, ArticleEO.class);
        }
        // match case 3:
//        if (StringUtils.isNotBlank(keyword) && category == null) {
//            query = new NativeSearchQueryBuilder()
//                    .withQuery(QueryBuilders.matchQuery("title", keyword))
//                    .withPageable(pageable)
//                    .build();
//        }
        // match case 3: highlight the keywords
        String searchTitleField = "title";
        if (StringUtils.isNotBlank(keyword) && category == null) {
            String preTag = "<font color='red'>";
            String postTag = "</font>";
            query = new NativeSearchQueryBuilder()
                    .withQuery(QueryBuilders.matchQuery(searchTitleField, keyword))
                    .withHighlightFields(new HighlightBuilder.Field(searchTitleField)
                        .preTags(preTag)
                        .postTags(postTag))
                    .withPageable(pageable)
                    .build();
            pagedArticle = esTemplate.queryForPage(query, ArticleEO.class, new SearchResultMapper() {
                @Override
                public <T> AggregatedPage<T> mapResults(SearchResponse searchResponse, Class<T> aClass, Pageable pageable) {
                    List<ArticleEO> articleHighLightList = new ArrayList<>();
                    // Obtain query result set
                    SearchHits hits = searchResponse.getHits();
                    for (SearchHit h : hits) {
                        HighlightField highlightField = h.getHighlightFields().get(searchTitleField);
                        String title = highlightField.getFragments()[0].toString();

                        // Obtain all other fragments, and re-encapsulate with highlighted contents.
                        String articleId = (String)h.getSourceAsMap().get("id");
                        Integer categoryId = (Integer)h.getSourceAsMap().get("categoryId");
                        Integer articleType = (Integer)h.getSourceAsMap().get("articleType");
                        String articleCover = (String)h.getSourceAsMap().get("articleCover");
                        String publishUserId = (String)h.getSourceAsMap().get("publishUserId");
                        Long dateLong = (Long)h.getSourceAsMap().get("publishTime");
                        Date publishTime = new Date(dateLong);

                        ArticleEO articleEO = new ArticleEO();
                        articleEO.setId(articleId);
                        articleEO.setTitle(title);
                        articleEO.setCategoryId(categoryId);
                        articleEO.setArticleType(articleType);
                        articleEO.setArticleCover(articleCover);
                        articleEO.setPublishUserId(publishUserId);
                        articleEO.setPublishTime(publishTime);

                        articleHighLightList.add(articleEO);

                    }

                    return new AggregatedPageImpl<>((List<T>)articleHighLightList,
                            pageable,
                            searchResponse.getHits().totalHits);

                }

                @Override
                public <T> T mapSearchHit(SearchHit searchHit, Class<T> aClass) {
                    return null;
                }
            });
        }

        // Reorganize article list
//        AggregatedPage<ArticleEO> pagedArticle =  esTemplate.queryForPage(query, ArticleEO.class);
        List<ArticleEO> articleEOList = pagedArticle.getContent();
        List<Article> articleList = new ArrayList<>();
        for (ArticleEO a : articleEOList) {
            Article article = new Article();
            BeanUtils.copyProperties(a, article);
            articleList.add(article);
        }
        // re-encapsulate to grid format
        PagedGridResult gridResult = new PagedGridResult();
        gridResult.setRows(articleList);
        gridResult.setPage(page + 1);
        gridResult.setTotal(pagedArticle.getTotalPages());
        gridResult.setRecords(pagedArticle.getTotalElements());

        gridResult = rebuildArticleGrid(gridResult);

        return GraceJSONResult.ok(gridResult);
    }

    @Override
    public GraceJSONResult list(String keyword, Integer category, Integer page, Integer pageSize) {
        if (page == null) {
            page = COMMON_START_PAGE;
        }

        if (pageSize == null) {
            pageSize = COMMON_PAGE_SIZE;
        }

        PagedGridResult gridResult = articlePortalService.queryIndexArticleList(keyword, category, page, pageSize);
        gridResult = rebuildArticleGrid(gridResult);
        return GraceJSONResult.ok(gridResult);
    }

    private PagedGridResult rebuildArticleGrid(PagedGridResult gridResult) {

        // START
        List<Article> list = (List<Article>)gridResult.getRows();

        // 1. Construct user id list
        // User ID de-duplication
        Set<String> idSet = new HashSet<>();
        List<String> idList = new ArrayList<>();

        for (Article a : list) {
            // 1.1 Construct a set for publishers
            idSet.add(a.getPublishUserId());
            // 1.2 Construct a list for article id
            idList.add(REDIS_ARTICLE_READ_COUNTS + ":" + a.getId());
        }

        // Using mget method in redis to query multiple values paired to article id.
        List<String> readCountsRedisList = redis.mget(idList);

        // 2.  Initiating remote call (restTemplate) to request user service to send over user (idSet) list
        List<AppUserVO> publisherList = getPublisherList(idSet);

//        for (AppUserVO u : publisherList) {
//            System.out.println(u.toString());
//        }

        // 3. Concat two lists and reconstruct article list
        // IndexArticleVO contains AppUserVO
        List<IndexArticleVO> indexArticleList = new ArrayList<>();

        for (int i = 0 ; i < list.size() ; i ++) {
            IndexArticleVO indexArticleVO = new IndexArticleVO();
            Article a = list.get(i);
            BeanUtils.copyProperties(a, indexArticleVO);

            // 3.1 Obtain publisher's basic info from publisherList
            AppUserVO publisher  = getUserIfPublisher(a.getPublishUserId(), publisherList);
            indexArticleVO.setPublisherVO(publisher);

            // 3.2 Rebuild the read counter inside the article page
            String redisCountsStr = readCountsRedisList.get(i);
            int readCounts = 0;
            if (StringUtils.isNotBlank(redisCountsStr)) {
                readCounts = Integer.valueOf(redisCountsStr);
            }
            indexArticleVO.setReadCounts(readCounts);

            indexArticleList.add(indexArticleVO);
        }

        gridResult.setRows(indexArticleList);
        //END
        return gridResult;
    }

    // Add service discovery to obtain registered service info
    @Autowired
    private DiscoveryClient discoveryClient;

    @Autowired
    private UserControllerApi userControllerApi;

    private List<AppUserVO> getPublisherList(Set idSet) {

//*********** use restTemplate ***********
//        List<ServiceInstance> instanceList = discoveryClient.getInstances(serviceId);
//        ServiceInstance userService = instanceList.get(0);
//        String userServerUrlExecute
//                = "http://" + userService.getHost()
//                + ":" + userService.getPort()
//                + "/user/queryByIds?userIds="
//                + JsonUtils.objectToJson(idSet);

//*********** use eureka ***********
//        String serviceId = "SERVICE-USER";
//        String userServerUrlExecute
//                = "http://" + serviceId
//                + "/user/queryByIds?userIds="
//                + JsonUtils.objectToJson(idSet);

//*********** use feign ***********
        // use url -> use api
        GraceJSONResult bodyResult = userControllerApi.queryByIds(JsonUtils.objectToJson(idSet));
//        String userServerUrlExecute
//                = "http://user.inews.com:8003/user/queryByIds?userIds=" + JsonUtils.objectToJson(idSet);
//        ResponseEntity<GraceJSONResult> responseEntity
//                = restTemplate.getForEntity(userServerUrlExecute, GraceJSONResult.class);
//        GraceJSONResult bodyResult = responseEntity.getBody();
        List<AppUserVO> publisherList = null;
        if (bodyResult.getStatus() == 200) {
            String userJson = JsonUtils.objectToJson(bodyResult.getData());
            publisherList = JsonUtils.jsonToList(userJson, AppUserVO.class);
        } else {
            publisherList = new ArrayList<>();
        }
        return publisherList;
    }

    private AppUserVO getUserIfPublisher(String publisherId, List<AppUserVO> publisherList) {
        for (AppUserVO user : publisherList) {
            if (user.getId().equalsIgnoreCase(publisherId)) {
                return user;
            }
        }
        return null;
    }

    @Override
    public Integer readCounts(String articleId) {
        return getCountsFromRedis(REDIS_ARTICLE_READ_COUNTS + ":" + articleId);
    }

    @Override
    public GraceJSONResult hotList() {
        return GraceJSONResult.ok(articlePortalService.queryHotList());
    }

    @Override
    public GraceJSONResult queryArticleListOfWriter(String writerId, Integer page, Integer pageSize) {
        if (page == null) {
            page = COMMON_START_PAGE;
        }

        if (pageSize == null) {
            pageSize = COMMON_PAGE_SIZE;
        }

        PagedGridResult gridResult = articlePortalService.queryArticleListOfWriter(writerId, page, pageSize);
        gridResult = rebuildArticleGrid(gridResult);
        return GraceJSONResult.ok(gridResult);
    }

    @Override
    public GraceJSONResult queryGoodArticleListOfWriter(String writerId) {
        PagedGridResult gridResult = articlePortalService.queryGoodArticleListOfWriter(writerId);
        return GraceJSONResult.ok(gridResult);
    }

    @Override
    public GraceJSONResult detail(String articleId) {
        ArticleDetailVO detailVO = articlePortalService.queryDetail(articleId);

        Set<String> idSet = new HashSet<>();
        idSet.add(detailVO.getPublishUserId());
        List<AppUserVO> publisherList = getPublisherList(idSet);

        if (!publisherList.isEmpty()) {
            detailVO.setPublishUserName(publisherList.get(0).getNickname());
        }

        detailVO.setReadCounts(getCountsFromRedis(REDIS_ARTICLE_READ_COUNTS + ":" + articleId));
        return GraceJSONResult.ok(detailVO);
    }

    @Override
    public GraceJSONResult readArticle(String articleId, HttpServletRequest request) {

        String userIp = IPUtil.getRequestIp(request);
        // Create a key for current user Ip address and save it to redis.
        // Once a user has already read article, the read counter should not increase by re-accessing from same ip address
        redis.setnx(REDIS_ALREADY_READ + ":" + articleId + ":" + userIp, userIp); // If key exist, cannot be update.

        redis.increment(REDIS_ARTICLE_READ_COUNTS + ":" + articleId, 1);
        return GraceJSONResult.ok();
    }

}
