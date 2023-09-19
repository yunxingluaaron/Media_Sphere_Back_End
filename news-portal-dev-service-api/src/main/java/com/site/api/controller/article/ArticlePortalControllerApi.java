package com.site.api.controller.article;

import com.site.grace.result.GraceJSONResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Api(value = "[portal] article controller", tags = {"[portal] article controller"})
@RequestMapping("portal/article")
public interface ArticlePortalControllerApi {

    @GetMapping("es/list")
    @ApiOperation(value = "[Homepage] Query article list from es", notes = "[Homepage] Query article list from es", httpMethod = "GET")
    public GraceJSONResult eslist(@RequestParam String keyword,
                                        @RequestParam(required = false) Integer category,
                                        @ApiParam(name = "page", value = "page number", required = false)
                                        @RequestParam Integer page,
                                        @ApiParam(name = "pageSize", value = "Item number on each pages", required = false)
                                        @RequestParam Integer pageSize);

    @GetMapping("list")
    @ApiOperation(value = "[Homepage] Query article list", notes = "[Homepage] Query article list", httpMethod = "GET")
    public GraceJSONResult list(@RequestParam String keyword,
                                @RequestParam(required = false) Integer category,
                                @ApiParam(name = "page", value = "page number", required = false)
                                @RequestParam Integer page,
                                @ApiParam(name = "pageSize", value = "Item number on each pages", required = false)
                                @RequestParam Integer pageSize);

    @GetMapping("hotList")
    @ApiOperation(value = "[Homepage] Query the list of hot news", notes = "[Homepage] Query the list of hot news", httpMethod = "GET")
    public GraceJSONResult hotList();


    @GetMapping("queryArticleListOfWriter")
    @ApiOperation(value = "Query the article list posted by single author", notes = "Query the article list posted by single author", httpMethod = "GET")
    public GraceJSONResult queryArticleListOfWriter(@RequestParam String writerId,
                                                    @ApiParam(name = "page", value = "page number", required = false)
                                                    @RequestParam Integer page,
                                                    @ApiParam(name = "pageSize", value = "Items on each page", required = false)
                                                    @RequestParam Integer pageSize);

    @GetMapping("queryGoodArticleListOfWriter")
    @ApiOperation(value = "Query hot articles of a single author", notes = "Query hot articles of a single author", httpMethod = "GET")
    public GraceJSONResult queryGoodArticleListOfWriter(@RequestParam String writerId);

    @GetMapping("detail")
    @ApiOperation(value = "Get article details", notes = "Get article details", httpMethod = "GET")
    public GraceJSONResult detail(@RequestParam String articleId);

    @GetMapping("readCounts")
    @ApiOperation(value = "Get article reads", notes = "Get article reads", httpMethod = "GET")
    public Integer readCounts(@RequestParam String articleId);

    @PostMapping("readArticle")
    @ApiOperation(value = "Article read counter", notes = "Article read counter", httpMethod = "POST")
    public GraceJSONResult readArticle(@RequestParam String articleId, HttpServletRequest request);
}
