package com.site.user.controller;

import com.site.api.BaseController;
import com.site.api.controller.user.AppUserMngControllerApi;
import com.site.enums.UserStatus;
import com.site.grace.result.GraceJSONResult;
import com.site.grace.result.ResponseStatusEnum;
import com.site.user.service.AppUserMngService;
import com.site.user.service.UserService;
import com.site.utils.PagedGridResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController
public class AppUserMngController extends BaseController implements AppUserMngControllerApi {

    final static Logger logger = LoggerFactory.getLogger(AppUserMngController.class);

    @Autowired
    private AppUserMngService appUserMngService;

    @Autowired
    private UserService userService;

    @Override
    public GraceJSONResult queryAll(String nickname, Integer status, Date startDate,
                                    Date endDate, Integer page, Integer pageSize) {

        System.out.println(startDate);
        System.out.println(endDate);

        if (page == null) {
            page = COMMON_START_PAGE;
        }

        if (pageSize == null) {
            pageSize = COMMON_PAGE_SIZE;
        }

        PagedGridResult result = appUserMngService.queryAllUserList(nickname, status, startDate,
                                            endDate, page, pageSize);

        return GraceJSONResult.ok(result);
    }

    @Override
    public GraceJSONResult userDetail(String userId) {
        return GraceJSONResult.ok(userService.getUser(userId));
    }

    @Override
    public GraceJSONResult freezeUserOrNot(String userId, Integer doStatus) {

        if (!UserStatus.isUserStatusValid(doStatus)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.USER_STATUS_ERROR);
        }
        appUserMngService.freezeUserOrNot(userId, doStatus);

        // Update user status:
        // 1. Clear user session. User need to perform login and refresh the page to get status update.
        // 2. Query the newest user info and put into Redis. This is not recommend, because session should be created by user.

        redis.del(REDIS_USER_INFO + ":" + userId);

        return GraceJSONResult.ok();
    }
}
