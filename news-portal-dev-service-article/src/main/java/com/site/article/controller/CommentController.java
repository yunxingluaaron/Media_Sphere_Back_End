package com.site.article.controller;

import com.site.api.BaseController;
import com.site.api.controller.article.CommentControllerApi;
import com.site.api.controller.user.HelloControllerApi;
import com.site.article.service.CommentPortalService;
import com.site.grace.result.GraceJSONResult;
import com.site.pojo.bo.CommentReplyBO;
import com.site.utils.PagedGridResult;
import com.site.utils.RedisOperator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@RestController
public class CommentController extends BaseController implements CommentControllerApi {

    final static Logger logger = LoggerFactory.getLogger(CommentController.class);

    @Autowired
    private CommentPortalService commentPortalService;

    @Override
    public GraceJSONResult createComment(CommentReplyBO commentReplyBO) {

        // ***** The errors are handled by com.site.exception.GraceExceptionHandler.java
        // 0. Check if BindingResult contains any error info，if true, return error.
//        if (result.hasErrors()) {
//            Map<String, String> errorMap = getErrors(result);
//            return GraceJSONResult.errorMap(errorMap);
//        }

        // 1. Get basic user info based on his id. Those info will be save to comment Sql chart redundantly to avoid
        // the relationship between multiple Sql chart. It improves performance.
        String userId = commentReplyBO.getCommentUserId();

        // 2. Initiate restTemplate to call user service to get user's nickname.
        Set<String> idSet = new HashSet<>();
        idSet.add(userId);
        String nickname = getBasicUserList(idSet).get(0).getNickname();
        String face = getBasicUserList(idSet).get(0).getFace();

        // 3. Save user's comment to database
        commentPortalService.createComment(commentReplyBO.getArticleId(), commentReplyBO.getFatherId(),
                                            commentReplyBO.getContent(), userId, nickname, face);

        return GraceJSONResult.ok();
    }

    @Override
    public GraceJSONResult commentCounts(String articleId) {

        Integer counts = getCountsFromRedis(REDIS_ARTICLE_COMMENT_COUNTS + ":" + articleId);

        return GraceJSONResult.ok(counts);
    }

    @Override
    public GraceJSONResult list(String articleId, Integer page, Integer pageSize) {

        if (page == null) {
            page = COMMON_START_PAGE;
        }
        if (pageSize == null) {
            pageSize = COMMON_PAGE_SIZE;
        }

        PagedGridResult gridResult = commentPortalService.queryArticleComments(articleId, page, pageSize);

        return GraceJSONResult.ok(gridResult);
    }

    @Override
    public GraceJSONResult mng(String writerId, Integer page, Integer pageSize) {
        if (page == null) {
            page = COMMON_START_PAGE;
        }
        if (pageSize == null) {
            pageSize = COMMON_PAGE_SIZE;
        }

        PagedGridResult gridResult = commentPortalService.queryWriterCommentsMng(writerId, page, pageSize);
        return GraceJSONResult.ok(gridResult);
    }

    @Override
    public GraceJSONResult delete(String writerId, String commentId) {
        commentPortalService.deleteComment(writerId, commentId);
        return GraceJSONResult.ok();
    }
}
