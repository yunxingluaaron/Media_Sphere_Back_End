package com.site.api.controller.article;

import com.site.grace.result.GraceJSONResult;
import com.site.pojo.bo.NewArticleBO;
import com.site.pojo.bo.SaveCategoryBO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Date;

@Api(value = "Article controller", tags = {"Article controller"})
@RequestMapping("article")
public interface ArticleControllerApi {

    @ApiOperation(value = "User posts an article", notes = "User posts an article", httpMethod = "POST")
    @PostMapping("createArticle")
    public GraceJSONResult createArticle(@RequestBody @Valid NewArticleBO newArticleBO);

    @ApiOperation(value = "Query all articles posted by current user", notes = "Query all articles posted by current user", httpMethod = "POST")
    @PostMapping("queryMyList")
    public GraceJSONResult queryMyList(@RequestParam String userId,
                                       @RequestParam String keyword,
                                       @RequestParam(required=false) Integer status,
                                       @RequestParam(required=false) Date startDate,
                                       @RequestParam(required=false) Date endDate,
                                       @RequestParam Integer page,
                                       @RequestParam Integer pageSize);

    @PostMapping("queryAllList")
    @ApiOperation(value = "Query the list of all articles by admin", notes = "Query the list of all articles by admin", httpMethod = "POST")
    public GraceJSONResult queryAllList(@RequestParam(required = false) Integer status,
                                        @ApiParam(name = "page", value = "page No.", required = false)
                                        @RequestParam Integer page,
                                        @ApiParam(name = "pageSize", value = "No. of items on each page", required = false)
                                        @RequestParam Integer pageSize);


    @PostMapping("doReview")
    @ApiOperation(value = "Review result of the article by the admin", notes = "Review result of the article by the admin", httpMethod = "POST")
    public GraceJSONResult doReview(@RequestParam String articleId,
                                    @RequestParam Integer passOrNot);

    @PostMapping("/delete")
    @ApiOperation(value = "User delete article", notes = "User delete article", httpMethod = "POST")
    public GraceJSONResult delete(@RequestParam String userId,
                                  @RequestParam String articleId);

    @PostMapping("/withdraw")
    @ApiOperation(value = "User withdraw article", notes = "User withdraw article", httpMethod = "POST")
    public GraceJSONResult withdraw(@RequestParam String userId,
                                    @RequestParam String articleId);
}
