package com.site.user.controller;

import com.netflix.hystrix.contrib.javanica.annotation.DefaultProperties;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.site.api.BaseController;
import com.site.api.controller.user.HelloControllerApi;
import com.site.api.controller.user.UserControllerApi;
import com.site.grace.result.GraceJSONResult;
import com.site.grace.result.ResponseStatusEnum;
import com.site.pojo.AppUser;
import com.site.pojo.bo.UpdateUserInfoBO;
import com.site.pojo.vo.AppUserVO;
import com.site.pojo.vo.UserAccountInfoVO;
import com.site.user.service.UserService;
import com.site.utils.JsonUtils;
import com.site.utils.RedisOperator;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.site.utils.JsonUtils.jsonToList;

@RestController
@DefaultProperties(defaultFallback = "defaultFallback")
public class UserController extends BaseController implements UserControllerApi {

    @Autowired
    private UserService userService;

    @Override
    public GraceJSONResult getUserInfo(String userId) {
        // 0. parameter cannot be null
        if (StringUtils.isBlank(userId)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.UN_LOGIN);
        }

        // 1. Query user info by userId
        AppUser user = getUser(userId);

        // 2. return user info
        AppUserVO userVO = new AppUserVO();
        BeanUtils.copyProperties(user, userVO);

        // 3. Query redis for user's following number and fans number.
        // Put the info to userVO for front end to render
        userVO.setMyFansCounts(getCountsFromRedis(REDIS_WRITER_FANS_COUNTS + ":" + userId));
        userVO.setMyFollowCounts(getCountsFromRedis(REDIS_MY_FOLLOW_COUNTS + ":" + userId));

        return GraceJSONResult.ok(userVO);
    }

    @Override
    public GraceJSONResult getAccountInfo(String userId)  {

        // 0. parameter cannot be null
        if (StringUtils.isBlank(userId)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.UN_LOGIN);
        }

        // 1. Query user info by userId
        AppUser user = getUser(userId);

        // 2. return user info
        UserAccountInfoVO accountInfoVO = new UserAccountInfoVO();
        BeanUtils.copyProperties(user, accountInfoVO);

        return GraceJSONResult.ok(accountInfoVO);
    }

    private AppUser getUser(String userId) {
        // Check if Redis includes user info, if true, return. (skip database query)
        String userJson = redis.get(REDIS_USER_INFO + ":" + userId);
        AppUser user = null;
        if (StringUtils.isNotBlank(userJson)) {
            user = JsonUtils.jsonToPojo(userJson, AppUser.class);
        } else {
            user = userService.getUser(userId);
            // Since user info will not be changed too often, for website with large access, we need to avoid accessing
            // database each time. Instead, we save those info(info after the first query) into Redis.
            redis.set(REDIS_USER_INFO + ":" + userId, JsonUtils.objectToJson(user));
        }

        return user;
    }

    @Override
    public GraceJSONResult updateUserInfo(UpdateUserInfoBO updateUserInfoBO){
// ****** The errors are now obtained by MethodArgumentNotValidException in com.site.exception.GraceExceptionHandler.java
//            , BindingResult result) {
//        // 0. Check BO
//        if (result.hasErrors()) {
//            Map<String, String> map = getErrors(result);
//            return GraceJSONResult.errorMap(map);
//        }

        // 1. update user info
        userService.updateUserInfo(updateUserInfoBO);

        return GraceJSONResult.ok();
    }

    @Value("${server.port}")
    private String myPort;

    @HystrixCommand//(fallbackMethod = "queryByIdsFallback")
    @Override
    public GraceJSONResult queryByIds(String userIds) {

//        int a = 1 / 0;
//        try {
//            Thread.sleep(5000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

        System.out.println("myPort= " + myPort);

        if (StringUtils.isBlank(userIds)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.USER_NOT_EXIST_ERROR);
        }

        List<AppUserVO> publisherList = new ArrayList<>();
        List<String> userIdList = JsonUtils.jsonToList(userIds, String.class);

        //FIXME:for dev use only
        if (userIdList.size() > 1) {
            System.out.println("Exception!!");
            throw new RuntimeException("Exception!!");
        }



        for (String userId : userIdList) {
            // Obtain user's basic info
            AppUserVO userVO = getBasicUserInfo(userId);

            // add user info to publisherList
            publisherList.add(userVO);

        }
        return GraceJSONResult.ok(publisherList);
    }

    public GraceJSONResult queryByIdsFallback(String userIds) {

        System.out.println("Hystrix callback method: queryByIdsFallback");


        List<AppUserVO> publisherList = new ArrayList<>();
        List<String> userIdList = JsonUtils.jsonToList(userIds, String.class);
        for (String userId : userIdList) {
            // Build an empty object
            // The user info in detailed page is not necessary
            AppUserVO userVO = new AppUserVO();
            publisherList.add(userVO);

        }
        return GraceJSONResult.ok(publisherList);
    }

    public GraceJSONResult defaultFallback() {
        System.out.println("Global circuit break!");
        return GraceJSONResult.errorCustom(ResponseStatusEnum.SYSTEM_RESPONSE_GLOBAL);
    }


    private AppUserVO getBasicUserInfo(String userId) {
        // 1. Query user info by userId
        AppUser user = getUser(userId);

        // 2. return user info
        AppUserVO userVO = new AppUserVO();
        BeanUtils.copyProperties(user, userVO);

        return userVO;
    }
}
