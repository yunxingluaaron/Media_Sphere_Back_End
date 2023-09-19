package com.site.api.controller.user;

import com.site.api.config.MyServiceList;
import com.site.api.controller.user.fallbacks.UserControllerFactoryFallback;
import com.site.grace.result.GraceJSONResult;
import com.site.pojo.bo.UpdateUserInfoBO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Api(value = "Controller for user information", tags = {"Controller for user information"})
@RequestMapping("user")
@FeignClient(value = MyServiceList.SERVICE_USER, fallbackFactory = UserControllerFactoryFallback.class)
public interface UserControllerApi {

    @ApiOperation(value = "Get user basic info", notes = "Get user basic info", httpMethod = "POST")
    @PostMapping("/getUserInfo")
    // HttpServletRequest: get ip address
    public GraceJSONResult getUserInfo(@RequestParam(name="userId") String userId);

    @ApiOperation(value = "Get user account info", notes = "Get user account info", httpMethod = "POST")
    @PostMapping("/getAccountInfo")
    public GraceJSONResult getAccountInfo(@RequestParam(name="userId") String userId);

//    @ApiOperation(value = "Update user info", notes = "Update user info", httpMethod = "POST")
//    @PostMapping("/updateUserInfo")
//    // BO: event from browser. Business Object.
//    // result is bind to result
//    // @RequestBody means the JSON passed from front end is match with the Object here
//    public GraceJSONResult updateUserInfo(@RequestBody @Valid UpdateUserInfoBO updateUserInfoBO, @RequestParam BindingResult result);

    @ApiOperation(value = "Update user info", notes = "Update user info", httpMethod = "POST")
    @PostMapping("/updateUserInfo")
    // BO: event from browser. Business Object.
    // result is bind to result
    // @RequestBody means the JSON passed from front end is match with the Object here
    public GraceJSONResult updateUserInfo(@RequestBody @Valid UpdateUserInfoBO updateUserInfoBO);


    @ApiOperation(value = "Query user list base on user ids", notes = "Query user list base on user ids", httpMethod = "GET")
    @GetMapping("/queryByIds")
    public GraceJSONResult queryByIds(@RequestParam(name="userIds") String userIds);
}
