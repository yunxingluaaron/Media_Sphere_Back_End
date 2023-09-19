package com.site.config.controller;

import com.site.api.controller.user.HelloControllerApi;
import com.site.grace.result.GraceJSONResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class HelloController implements HelloControllerApi {

    @GetMapping("/hello")
    public Object hello() {

        return GraceJSONResult.ok();
    }

}
