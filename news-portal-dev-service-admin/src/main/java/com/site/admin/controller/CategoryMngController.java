package com.site.admin.controller;

import com.site.admin.service.CategoryService;
import com.site.api.BaseController;
import com.site.api.controller.admin.CategoryMngControllerApi;
import com.site.grace.result.GraceJSONResult;
import com.site.grace.result.ResponseStatusEnum;
import com.site.pojo.Category;
import com.site.pojo.bo.SaveCategoryBO;
import com.site.utils.JsonUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
public class CategoryMngController extends BaseController implements CategoryMngControllerApi {

    final static Logger logger = LoggerFactory.getLogger(CategoryMngController.class);

    @Autowired
    private CategoryService categoryService;

    @Override
    public GraceJSONResult saveOrUpdateCategory(@Valid SaveCategoryBO saveCategoryBO) {

        // ***** The errors are handled by com.site.exception.GraceExceptionHandler.java
        // Validate BindingResult, if false, return
//        if (result.hasErrors()) {
//            Map<String, String> errorMap = getErrors(result);
//            return GraceJSONResult.errorMap(errorMap);
//        }

        Category newCat = new Category();
        BeanUtils.copyProperties(saveCategoryBO, newCat);
        // id为空新增，不为空修改
        if (saveCategoryBO.getId() == null) {
            // 查询新增的分类名称不能重复存在
            boolean isExist = categoryService.queryCatIsExist(newCat.getName(), null);
            if (!isExist) {
                // 新增到数据库
                categoryService.createCategory(newCat);
            } else {
                return GraceJSONResult.errorCustom(ResponseStatusEnum.CATEGORY_EXIST_ERROR);
            }
        } else {
            // 查询修改的分类名称不能重复存在
            boolean isExist = categoryService.queryCatIsExist(newCat.getName(), saveCategoryBO.getOldName());
            if (!isExist) {
                // 修改到数据库
                categoryService.modifyCategory(newCat);
            } else {
                return GraceJSONResult.errorCustom(ResponseStatusEnum.CATEGORY_EXIST_ERROR);
            }
        }

        return GraceJSONResult.ok();
    }

    @Override
    public GraceJSONResult getCatList() {
        List<Category> categoryList = categoryService.queryCategoryList();
        return GraceJSONResult.ok(categoryList);
    }

    @Override
    public GraceJSONResult getCats() {
        // Query data from Redis. If found, return the data; if not found, search database and save to redis then return
        String allCatJson = redis.get(REDIS_ALL_CATEGORY);

        List<Category> categoryList = null;
        if (StringUtils.isBlank(allCatJson)) {
            categoryList = categoryService.queryCategoryList();
            redis.set(REDIS_ALL_CATEGORY, JsonUtils.objectToJson(categoryList));
        } else {
            categoryList = JsonUtils.jsonToList(allCatJson, Category.class);
        }

        return GraceJSONResult.ok(categoryList);
    }
}
