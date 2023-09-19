package com.site.zuul.controller;

import com.site.api.controller.user.HelloControllerApi;
import com.site.grace.result.GraceJSONResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class HelloController implements HelloControllerApi {

    public Object hello() {

        return GraceJSONResult.ok();
    }

}
