package com.site.user.controller;

import com.site.api.BaseController;
import com.site.api.controller.user.PassportControllerApi;
import com.site.enums.UserStatus;
import com.site.grace.result.GraceJSONResult;
import com.site.grace.result.ResponseStatusEnum;
import com.site.pojo.AppUser;
import com.site.pojo.bo.RegisterLoginBO;
import com.site.user.service.UserService;
import com.site.utils.IPUtil;
import com.site.utils.JsonUtils;
import com.site.utils.SMSUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
public class PassportController extends BaseController implements PassportControllerApi {

    final static Logger logger = LoggerFactory.getLogger(PassportController.class);

    @Autowired
    private SMSUtils smsUtils;
    @Autowired
    private UserService userService;


    @Override
    public GraceJSONResult getSMSCode(String mobile, HttpServletRequest request) throws Exception {
        // obtain user ip address
        String userIp = IPUtil.getRequestIp(request);


        // 根据用户的ip进行限制，限制用户在一定时间内只能获得一次验证码
        redis.setnx120s(MOBILE_SMSCODE + ":" + userIp, userIp);

        // Generate random verification code and send SMS
        String random = (int)((Math.random() * 9 + 1) * 100000) + "";
        //smsUtils.sendSMS("18903423733", random);

        // Store the verification code into redis for verification
        redis.set(MOBILE_SMSCODE + ":" + mobile, random, 30 * 60); // 30 min for timeout
        return GraceJSONResult.ok();
    }

    @Override
    public GraceJSONResult doLogin(RegisterLoginBO registerLoginBO,
                                   HttpServletRequest request, HttpServletResponse response) {
        // ***** The errors are handled by com.site.exception.GraceExceptionHandler.java
        // 0. Check if BindingResult has wrong verification info, if true, return;
//        if (result.hasErrors()) {
//            Map<String, String> map = getErrors(result);
//            return GraceJSONResult.errorMap(map);
//        }

        String mobile = registerLoginBO.getMobile();
        String smsCode = registerLoginBO.getSmsCode();
        // 1. check if verification code matches.
        String redisSMSCode = redis.get(MOBILE_SMSCODE + ":" + mobile);
        if (StringUtils.isBlank(redisSMSCode) || !redisSMSCode.equals(smsCode)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.SMS_CODE_ERROR);
        }

        // 2. Query database to find out if the user has already signed up.
        AppUser user = userService.queryMobileIsExist(mobile);
        if (user != null && user.getActiveStatus() == UserStatus.FROZEN.type) {
            // If user is not null and has been blocked, throw exception, login rejected.
            return GraceJSONResult.errorCustom(ResponseStatusEnum.USER_FROZEN);
        }
        // if user is new, add user info to database.
        else if (user == null) {
            user = userService.createUser(mobile);
        }

        // 3. Store user session in distributed system
        int userActiveStatus =  user.getActiveStatus();
        if (userActiveStatus != UserStatus.FROZEN.type) {
            // Save token to Redis
            String uToken = UUID.randomUUID().toString();
            redis.set(REDIS_USER_TOKEN + ":" + user.getId(), uToken);
            redis.set(REDIS_USER_INFO + ":" + user.getId(), JsonUtils.objectToJson(user));

            // Save user id and token to cookie
            setCookie(request, response, "utoken", uToken, COOKIE_MONTH);
            setCookie(request, response, "uid", user.getId(), COOKIE_MONTH);
        }

        // 4. After user successfully signed up or logged in, SMS Code in redis needs to be deleted.
        // This is to make sure SMS Code can only be used once.
        redis.del(MOBILE_SMSCODE + ":" + mobile);

        // 5. return user status
        return GraceJSONResult.ok(userActiveStatus);
    }

    @Override
    public GraceJSONResult logout(String userId, HttpServletRequest request, HttpServletResponse response) {

        redis.del(REDIS_USER_TOKEN + ":" + userId);

        setCookie(request, response, "utoken", "", COOKIE_DELETE);
        setCookie(request, response, "uid", "", COOKIE_DELETE);

        return GraceJSONResult.ok();
    }
}
