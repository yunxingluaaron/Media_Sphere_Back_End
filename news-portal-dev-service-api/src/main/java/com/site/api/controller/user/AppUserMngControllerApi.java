package com.site.api.controller.user;

import com.site.grace.result.GraceJSONResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Date;

@Api(value = "User management API", tags = {"User management API"})
@RequestMapping("appUser")
public interface AppUserMngControllerApi {


    @PostMapping("queryAll")
    @ApiOperation(value = "Query all users", notes = "Query all users", httpMethod = "POST")
    public GraceJSONResult queryAll(@RequestParam String nickname,
                                    @RequestParam Integer status,
                                    @RequestParam(required = false) Date startDate,
                                    @RequestParam(required = false) Date endDate,
                                    @RequestParam Integer page,
                                    @RequestParam Integer pageSize);

    @PostMapping("userDetail")
    @ApiOperation(value = "Get user's detailed information", notes = "Get user's detailed information", httpMethod = "POST")
    public GraceJSONResult userDetail(@RequestParam String userId);

    @PostMapping("freezeUserOrNot")
    @ApiOperation(value = "Block or unblock user", notes = "Block or unblock user", httpMethod = "POST")
    public GraceJSONResult freezeUserOrNot(@RequestParam String userId, @RequestParam Integer doStatus);
}