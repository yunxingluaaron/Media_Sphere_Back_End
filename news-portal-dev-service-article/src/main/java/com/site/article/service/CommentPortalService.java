package com.site.article.service;

import com.site.utils.PagedGridResult;

public interface CommentPortalService {

    /**
     * Post a comment
     */
    public void createComment(String articleId,
                              String fatherCommentId,
                              String content,
                              String userId,
                              String nickname,
                              String face);

    /**
     * Query all comments of current article
     */
    public PagedGridResult queryArticleComments(String articleId,
                                                Integer page,
                                                Integer pageSize);

    /**
     * Query all comment for management
     */
    public PagedGridResult queryWriterCommentsMng(String writerId, Integer page, Integer pageSize);

    /**
     * Delete comment
     */
    public void deleteComment(String writerId, String commentId);

}
