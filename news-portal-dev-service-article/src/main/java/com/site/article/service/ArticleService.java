package com.site.article.service;

import com.site.pojo.Category;
import com.site.pojo.bo.NewArticleBO;
import com.site.utils.PagedGridResult;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;

public interface ArticleService {

    /*
    * Post Article
    * */
    public void createArticle(NewArticleBO newArticleBO, Category category);

    /**
     * Update scheduled articles status from scheduled to published.
     */
    public void updateAppointToPublish();

    /**
     * Update ONE scheduled article status from scheduled to published.
     */
    public void updateArticleToPublish(String articleId);

    /**
     * User center - query my article list
     * @return
     */
    public PagedGridResult queryMyArticleList(String userId, String keyword, Integer status, Date startDate,
                                              Date endDate, Integer page, Integer pageSize);

    /**
     * Update article status
     * @param articleId
     * @param pendingStatus
     */
    public void updateArticleStatus(String articleId, Integer pendingStatus);

    /**
     * Combine article id with gridFS address of static article html
     */
    public void updateArticleToGridFS(String articleId, String articleMongoId);

    /**
     * 管理员查询文章列表
     */
    public PagedGridResult queryAllArticleListAdmin(Integer status, Integer page, Integer pageSize);

    /**
     * 删除文章
     */
    public void deleteArticle(String userId, String articleId);

    /**
     * 撤回文章
     */
    public void withdrawArticle(String userId, String articleId);
}


