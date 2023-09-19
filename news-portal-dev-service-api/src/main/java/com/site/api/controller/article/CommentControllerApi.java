package com.site.api.controller.article;

import com.site.grace.result.GraceJSONResult;
import com.site.pojo.bo.CommentReplyBO;
import com.site.pojo.bo.NewArticleBO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Date;

@Api(value = "Article comment controller", tags = {"Article comment controller"})
@RequestMapping("comment")
public interface CommentControllerApi {

    @PostMapping("createComment")
    @ApiOperation(value = "User posts a comment", notes = "User posts a comment", httpMethod = "POST")
    public GraceJSONResult createComment(@RequestBody @Valid CommentReplyBO commentReplyBO);

    @GetMapping("counts")
    @ApiOperation(value = "Count comments number", notes = "Count comments number", httpMethod = "GET")
    public GraceJSONResult commentCounts(@RequestParam String articleId);

    @GetMapping("list")
    @ApiOperation(value = "Query comment list of current article", notes = "Query comment list of current article", httpMethod = "GET")
    public GraceJSONResult list(@RequestParam String articleId,
                                @RequestParam Integer page,
                                @RequestParam Integer pageSize);

    @PostMapping("mng")
    @ApiOperation(value = "Query comment list in admin management page", notes = "Query comment list in admin management page", httpMethod = "POST")
    public GraceJSONResult mng(@RequestParam String writerId,
                               @ApiParam(name = "page", value = "page number", required = false)
                               @RequestParam Integer page,
                               @ApiParam(name = "pageSize", value = "Items on each page", required = false)
                               @RequestParam Integer pageSize);

    @PostMapping("/delete")
    @ApiOperation(value = "Delete comments of the article", notes = "Delete comments of the article", httpMethod = "POST")
    public GraceJSONResult delete(@RequestParam String writerId,
                                  @RequestParam String commentId);
}
