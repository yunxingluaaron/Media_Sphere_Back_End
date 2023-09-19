package com.site.api.controller.article;

import com.site.grace.result.GraceJSONResult;
import com.site.pojo.bo.NewArticleBO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Date;

@Api(value = "Static article controller", tags = {"Static article controller"})
@RequestMapping("article/html")
public interface ArticleHTMLControllerApi {

    @ApiOperation(value = "Download static html", notes = "Download static html", httpMethod = "GET")
    @GetMapping("download")
    public Integer download(String articleId,
                                       String articleMongoId) throws Exception ;

    @ApiOperation(value = "Delete static html", notes = "Delete static html", httpMethod = "GET")
    @GetMapping("delete")
    public Integer delete(String articleId) throws Exception ;
}
