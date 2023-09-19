package com.site.api.interceptors;

import com.site.exception.GraceException;
import com.site.grace.result.ResponseStatusEnum;
import com.site.utils.IPUtil;
import com.site.utils.RedisOperator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class UserTokenInterceptor extends BaseInterceptor implements HandlerInterceptor {

    /*
    * Interception request; before visit controller
    * */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // Here we use header by considering app w/o cookie
        String userId = request.getHeader("headerUserId");
        String userToken = request.getHeader("headerUserToken");

        // Decide if request can be passed
        boolean run = verifyUserIdToken(userId, userToken, REDIS_USER_TOKEN);
        return run;
    }

    /*
     * Interception request; after visited controller, before render the view
     * */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    /*
     * Interception request;  after visited controller, after render the view
     * */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }
}
