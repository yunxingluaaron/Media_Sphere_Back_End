package com.site.article.service;

import com.site.pojo.Article;
import com.site.pojo.vo.ArticleDetailVO;
import com.site.utils.PagedGridResult;

import java.util.List;

public interface ArticlePortalService {

    /**
     * Query article list on homepage
     */
    public PagedGridResult queryIndexArticleList(String keyword,
                                                 Integer category,
                                                 Integer page,
                                                 Integer pageSize);
    /**
     * Query hot news list on homepage
     */
    public List<Article> queryHotList();

    /**
     * Query all articles form a single author
     */
    public PagedGridResult queryArticleListOfWriter(String writerId,
                                                    Integer page,
                                                    Integer pageSize);

    /**
     * Query selected articles from a single author
     */
    public PagedGridResult queryGoodArticleListOfWriter(String writerId);

    /**
     * Query article details
     */
    public ArticleDetailVO queryDetail(String articleId);

}
