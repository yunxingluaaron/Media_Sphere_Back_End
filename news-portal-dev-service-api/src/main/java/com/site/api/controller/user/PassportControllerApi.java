package com.site.api.controller.user;

import com.site.grace.result.GraceJSONResult;
import com.site.pojo.bo.RegisterLoginBO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@Api(value = "User Login / Sign-up", tags = {"controller for user Login / Sign-up"})
@RequestMapping("passport")
public interface PassportControllerApi {

    @ApiOperation(value = "Get SMS verification code", notes = "Get SMS verification code", httpMethod = "GET")
    @GetMapping("/getSMSCode")
    // HttpServletRequest 获取 ip地址
    public GraceJSONResult getSMSCode(@RequestParam String mobile, HttpServletRequest request) throws Exception;

    @ApiOperation(value = "Login / Sign-up API", notes = "Login / Sign-up API", httpMethod = "POST")
    @PostMapping("/doLogin")
    // BO: event from browser. Business Object.
    // result is bind to result
    // @RequestBody means the JSON passed from front end is match with the Object here
    public GraceJSONResult doLogin(@RequestBody @Valid RegisterLoginBO registerLoginBO,
                                   HttpServletRequest request, HttpServletResponse response);

    @ApiOperation(value = "User logout API", notes = "User logout API", httpMethod = "POST")
    @PostMapping("/logout")
    public GraceJSONResult logout(@RequestParam String userId,
                                   HttpServletRequest request, HttpServletResponse response);
}
