package com.site.api.controller.admin;

import com.site.grace.result.GraceJSONResult;
import com.site.pojo.bo.SaveCategoryBO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.Valid;

@Api(value = "Article category manager", tags = {"Article cataloger manager controller"})
@RequestMapping("categoryMng")
public interface CategoryMngControllerApi {

    @PostMapping("saveOrUpdateCategory")
    @ApiOperation(value = "Add / modify category", notes = "Add / modify category", httpMethod = "POST")
    public GraceJSONResult saveOrUpdateCategory(@RequestBody @Valid SaveCategoryBO newCategoryBO);
    @PostMapping("getCatList")
    @ApiOperation(value = "Query category list", notes = "Query category list", httpMethod = "POST")
    public GraceJSONResult getCatList();

    @GetMapping("getCats")
    @ApiOperation(value = "Query category list by client", notes = "Query category list by client", httpMethod = "GET")
    public GraceJSONResult getCats();

}
