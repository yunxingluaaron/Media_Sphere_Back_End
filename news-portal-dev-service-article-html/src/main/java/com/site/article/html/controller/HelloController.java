package com.site.article.html.controller;

import com.site.api.controller.user.HelloControllerApi;
import com.site.grace.result.GraceJSONResult;
import com.site.utils.RedisOperator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController implements HelloControllerApi {

    final static Logger logger = LoggerFactory.getLogger(HelloController.class);

    @Autowired
    private RedisOperator redis;
    public Object hello() {

        return GraceJSONResult.ok();
    }

}
