package com.site.zuul.filters;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import com.site.grace.result.GraceJSONResult;
import com.site.grace.result.ResponseStatusEnum;
import com.site.utils.IPUtil;
import com.site.utils.JsonUtils;
import com.site.utils.RedisOperator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

// Set custom zuul filters
@Component
@RefreshScope
public class BlackIpFilter extends ZuulFilter {

    @Value("${blackIp.continueCounts}")
    private Integer continueCounts;

    @Value("${blackIp.timeInterval}")
    private Integer timeInterval;

    @Value("${blackIp.limitTimes}")
    private Integer limitTimes;

    @Autowired
    private RedisOperator redis;

    @Override
    public String filterType() {
        return "pre";
    }

    @Override
    public int filterOrder() {
        return 2;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() throws ZuulException {
        System.out.println("Execute ip filter for black list...");
        System.out.println("continueCounts: " + continueCounts);
        System.out.println("timeInterval: " + timeInterval);
        System.out.println("limitTimes: " + limitTimes);

        // Obtain context
        RequestContext context = RequestContext.getCurrentContext();
        HttpServletRequest request = context.getRequest();

        // obtain ip address
        String ip = IPUtil.getRequestIp(request);

        /*
        * Check if the number of requests from certain ip address excess the limit.
        * */
        final String ipRedisKey = "zuul-ip:" + ip;
        final String ipRedisLimitKey = "zuul-ip-limit:" + ip;

        // obtain the remaining time of current ip
        long limitLeftTime = redis.ttl(ipRedisLimitKey);
        // if current ip's remaining time is not null, block this ip
        if (limitLeftTime > 0) {
            stopRequest(context);
            return null;
        }

        // count number of requests from one ip in redis
        long requestCounts = redis.increment(ipRedisKey, 1);
        // Count number of requests form 0. Initialized with 1. Set request time interval
        // The user ip will be preserved in redis for certain time interval
        if (requestCounts == 1) {
            redis.expire(ipRedisKey, timeInterval);
        }

        // If the number of request is larger than 1, it means that before the ip record expire in
        // redis, more accesses were made.
        // Set a threshold for the number of request.
        if (requestCounts > continueCounts) {
            // Block the ip for <limitTimes> seconds
            redis.set(ipRedisLimitKey, ipRedisLimitKey, limitTimes);
            stopRequest(context);
        }

        return null;
    }

    private void stopRequest(RequestContext context) {
        // Stop zuul routing.
        context.setSendZuulResponse(false);
        context.setResponseStatusCode(200);
        String result = JsonUtils.objectToJson(
                GraceJSONResult.errorCustom(ResponseStatusEnum.SYSTEM_RESPONSE_ZUUL));
        context.setResponseBody(result);
//        context.getResponse().setCharacterEncoding("utf-8");
        context.getResponse().setContentType(MediaType.APPLICATION_JSON_VALUE);
    }
}
