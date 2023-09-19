package com.site.api.interceptors;

import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.site.api.BaseController.REDIS_ADMIN_TOKEN;

public class AdminCookieTokenInterceptor extends BaseInterceptor implements HandlerInterceptor {

    /**
     * Intercept request，before call controller
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    // If client request the faceId directly from the gridFS, we need to check cookie for user authorization.

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String adminUserId = getCookie(request, "aid");
        String adminUserToken = getCookie(request, "atoken");

        boolean run = verifyUserIdToken(adminUserId, adminUserToken, REDIS_ADMIN_TOKEN);
        return run;
    }


}
