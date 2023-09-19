package com.site.api.controller.admin;

import com.site.pojo.bo.AdminLoginBO;
import com.site.pojo.bo.NewAdminBO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Api(value = "Admin management API", tags = {"Admin management API"})
@RequestMapping("adminMng")
public interface AdminMngControllerApi {
    @ApiOperation(value = "API for hello method", notes = "API for hello method", httpMethod = "POST")
    @PostMapping("/adminLogin") // route of the method
    public Object adminLogin(@RequestBody AdminLoginBO adminLoginBO, HttpServletRequest request, HttpServletResponse response);

    @ApiOperation(value = "Query if admin name exist", notes = "Query if admin name exist", httpMethod = "POST")
    @PostMapping("/adminIsExist") // route of the method
    public Object adminIsExist(@RequestParam String username);

    @ApiOperation(value = "Create admin account", notes = "Create admin account", httpMethod = "POST")
    @PostMapping("/addNewAdmin") // route of the method
    public Object addNewAdmin(@RequestBody NewAdminBO newAdminBO, HttpServletRequest request, HttpServletResponse response);

    @ApiOperation(value = "Query admin list", notes = "Query admin list", httpMethod = "POST")
    @PostMapping("/getAdminList") // route of the method
    public Object getAdminList(@ApiParam(name = "page", value = "No. of page", required = false)
                               @RequestParam Integer page,
                               @ApiParam(name = "pageSize", value = "No. of items show in each page.", required = false)
                               @RequestParam Integer pageSize);

    @ApiOperation(value = "Admin logout", notes = "Admin logout", httpMethod = "POST")
    @PostMapping("/adminLogout") // route of the method
    public Object adminLogout(@RequestParam String adminId, HttpServletRequest request, HttpServletResponse response);

    @ApiOperation(value = "Admin faceId login", notes = "Admin faceId login", httpMethod = "POST")
    @PostMapping("/adminFaceLogin") // route of the method
    public Object adminFaceLogin(@RequestBody AdminLoginBO adminLoginBO, HttpServletRequest request, HttpServletResponse response);

}
