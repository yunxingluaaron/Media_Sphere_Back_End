package com.site.admin.controller;

import com.site.admin.service.AdminUserService;
import com.site.api.controller.admin.AdminMngControllerApi;
import com.site.enums.FaceVerifyType;
import com.site.exception.GraceException;
import com.site.grace.result.GraceJSONResult;
import com.site.grace.result.ResponseStatusEnum;
import com.site.pojo.AdminUser;
import com.site.pojo.bo.AdminLoginBO;
import com.site.pojo.bo.NewAdminBO;
import com.site.utils.FaceVerifyUtils;
import com.site.utils.PagedGridResult;
import com.site.utils.RedisOperator;
import com.site.api.BaseController;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.UUID;


@RestController
public class AdminMngController extends BaseController implements AdminMngControllerApi {

//    final static Logger logger = LoggerFactory.getLogger(AdminMngController.class);

    @Autowired
    private RedisOperator redis;

    @Autowired
    private AdminUserService adminUserService;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private FaceVerifyUtils faceVerifyUtils;

    @Override
    public GraceJSONResult adminLogin(AdminLoginBO adminLoginBO, HttpServletRequest request, HttpServletResponse response) {

        // 0. Verify user name and password are not empty
        if (StringUtils.isBlank(adminLoginBO.getUsername())) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.ADMIN_USERNAME_NULL_ERROR);
        }
        if (StringUtils.isBlank(adminLoginBO.getPassword())) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.ADMIN_PASSWORD_NULL_ERROR);
        }

        // 1. Query admin info
        AdminUser admin = adminUserService.queryAdminByUsername(adminLoginBO.getUsername());

        // 2. Check if admin exist
        if (admin == null) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.ADMIN_NOT_EXIT_ERROR);
        }

        // 3. Check if password match
        boolean isPwdMatch = BCrypt.checkpw(adminLoginBO.getPassword(), admin.getPassword());
        if (isPwdMatch) {
            doLoginSettings(admin, request, response);
            return GraceJSONResult.ok();
        } else {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.ADMIN_NOT_EXIT_ERROR);
        }
    }


    /**
     * Set basic info for admin after successful login
     * @param admin
     * @param request
     * @param response
     */
    private void doLoginSettings (AdminUser admin,
                                  HttpServletRequest request,
                                  HttpServletResponse response) {
        // Generate token，Save it into redis
        String uniqueToken = UUID.randomUUID().toString().trim();
        redis.set(REDIS_ADMIN_TOKEN + ":" + admin.getId(), uniqueToken);

        // Write token and user info into cookie

        setCookie(request, response, "atoken", uniqueToken, COOKIE_MONTH);
        setCookie(request, response, "aid", admin.getId(), COOKIE_MONTH);
        setCookie(request, response, "aname", admin.getAdminName(), COOKIE_MONTH);

    }

    @Override
    public Object adminIsExist(String username) {

        checkAdminExist(username);
        return GraceJSONResult.ok();
    }

    private void checkAdminExist(String username) {
        AdminUser admin = adminUserService.queryAdminByUsername(username);

        if (admin != null) {
            GraceException.display(ResponseStatusEnum.ADMIN_USERNAME_EXIST_ERROR);
        }
    }

    @Override
    public Object addNewAdmin(NewAdminBO newAdminBO, HttpServletRequest request, HttpServletResponse response) {

        // 0. Verify user name and password are not empty
        if (StringUtils.isBlank(newAdminBO.getUsername())) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.ADMIN_USERNAME_NULL_ERROR);
        }
        if (StringUtils.isBlank(newAdminBO.getPassword())) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.ADMIN_PASSWORD_NULL_ERROR);
        }

        // 1. Check if base64 is empty. If empty, password cannot be empty.
        if (StringUtils.isBlank(newAdminBO.getImg64())) {
            if (StringUtils.isBlank(newAdminBO.getPassword()) || StringUtils.isBlank(newAdminBO.getConfirmPassword())) {
                return GraceJSONResult.errorCustom(ResponseStatusEnum.ADMIN_PASSWORD_NULL_ERROR);
            }
        }

        // 2. If password is not empty, password must match with confirmed password
        if (StringUtils.isNotBlank(newAdminBO.getPassword())) {
            if (!newAdminBO.getPassword().equals(newAdminBO.getConfirmPassword())) {
                return GraceJSONResult.errorCustom(ResponseStatusEnum.ADMIN_PASSWORD_ERROR);
            }
        }

        // 3. check the username has not be used
        checkAdminExist(newAdminBO.getUsername());

        // 4. Call service and save admin info
        adminUserService.createAdminUser(newAdminBO);
        // Base64 will be saved to gridFS.
        // Do not save this to FastDFS or cloudFS, because the url is public
        return GraceJSONResult.ok();
    }

    @Override
    public Object getAdminList(Integer page, Integer pageSize) {

        // Set a default value
        if (page == null) {
            page = COMMON_START_PAGE;
        }

        if (pageSize == null) {
            pageSize = COMMON_PAGE_SIZE;
        }

        PagedGridResult result = adminUserService.queryAdminList(page, pageSize);
        return GraceJSONResult.ok(result);
    }

    @Override
    public Object adminLogout(String adminId, HttpServletRequest request, HttpServletResponse response) {
        // 1. Delete admin session token from Redis
        redis.del(REDIS_ADMIN_TOKEN + ":" + adminId);

        // 2. Delete admin session info from cookie
        deleteCookie(request, response, "atoken");
        deleteCookie(request, response, "aid");
        deleteCookie(request, response, "aname");
        return GraceJSONResult.ok();
    }

    @Override
    public Object adminFaceLogin(AdminLoginBO adminLoginBO, HttpServletRequest request, HttpServletResponse response) {

        // 0. Check if username and faceId is empty
        if (StringUtils.isBlank(adminLoginBO.getUsername())) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.ADMIN_USERNAME_NULL_ERROR);
        }
        String tempFace64 = adminLoginBO.getImg64();
        if (StringUtils.isBlank(tempFace64)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.ADMIN_FACE_NULL_ERROR);
        }

        // 1. Obtain faceId form mongoDB gridFS
        AdminUser admin = adminUserService.queryAdminByUsername(adminLoginBO.getUsername());
        String adminFaceId = admin.getFaceId();

        if (StringUtils.isBlank(adminFaceId)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.ADMIN_FACE_LOGIN_ERROR);
        }

        // 2. Request file service, obtain the base64 of stored face
        // Call file service here
        // Restful API
        String fileServerUrlExecute = "http://files.inews.com:8004/fs/readFace64InGridFS?faceId=" + adminFaceId;
        ResponseEntity<GraceJSONResult> responseEntity = restTemplate.getForEntity(fileServerUrlExecute, GraceJSONResult.class);
        GraceJSONResult bodyResult = responseEntity.getBody();
        String base64DB = (String)bodyResult.getData();

        // 3. Call face recognition / compare API to get confidence level of the two faces
        boolean result = faceVerifyUtils.faceVerify(FaceVerifyType.BASE64.type, tempFace64, base64DB, 90);
        if (!result) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.ADMIN_FACE_LOGIN_ERROR);
        }

        // 4. Set redis and cookie after login succeed
        doLoginSettings(admin, request, response);

        return GraceJSONResult.ok();
    }
}
