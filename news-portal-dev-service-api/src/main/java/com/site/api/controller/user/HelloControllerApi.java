package com.site.api.controller.user;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
@Api(value = "controllerTittle", tags = {"function a"})
public interface HelloControllerApi {
    @ApiOperation(value = "API for hello method", notes = "API for hello method", httpMethod = "GET")
    @GetMapping("/hello") // route of the method
    public Object hello();
}
