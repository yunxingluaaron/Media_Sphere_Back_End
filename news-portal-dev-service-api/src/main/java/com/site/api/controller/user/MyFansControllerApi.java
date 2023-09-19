package com.site.api.controller.user;

import com.site.grace.result.GraceJSONResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Api(value = "Follower management", tags = {"Follower management controller"})
@RequestMapping("fans")
public interface MyFansControllerApi {


    @ApiOperation(value = "Passive update of fan's information", notes = "Passive update of fan's information", httpMethod = "POST")
    @PostMapping("/forceUpdateFanInfo")
    public GraceJSONResult forceUpdateFanInfo(@RequestParam String relationId,
                                              @RequestParam String fanId);

    @ApiOperation(value = "Check if current user is followed the author", notes = "Check if current user is followed the author", httpMethod = "POST")
    @PostMapping("/isMeFollowThisWriter")
    public GraceJSONResult isFollowThisWriter(@RequestParam String writerId,
                                                @RequestParam String fanId);


    @ApiOperation(value = "Follow the author", notes = "Follow the author", httpMethod = "POST")
    @PostMapping("/follow")
    public GraceJSONResult follow(@RequestParam String writerId,
                                @RequestParam String fanId);

    @ApiOperation(value = "Unfollow the author", notes = "Unfollow the author", httpMethod = "POST")
    @PostMapping("/unfollow")
    public GraceJSONResult unfollow(@RequestParam String writerId,
                                  @RequestParam String fanId);

    @ApiOperation(value = "Query all followers of a certain user", notes = "Query all followers of a certain user", httpMethod = "POST")
    @PostMapping("/queryAll")
    public GraceJSONResult queryAll(
            @RequestParam String writerId,
            @ApiParam(name = "page", value = "page number", required = false)
            @RequestParam Integer page,
            @ApiParam(name = "pageSize", value = "Items on each pages", required = false)
            @RequestParam Integer pageSize);

    @ApiOperation(value = "Get fans number by gender", notes = "Get fans number by gender", httpMethod = "POST")
    @PostMapping("/queryRatio")
    public GraceJSONResult queryRatio(@RequestParam String writerId);

    @ApiOperation(value = "Get fans number by region", notes = "Get fans number by region", httpMethod = "POST")
    @PostMapping("/queryRatioByRegion")
    public GraceJSONResult queryRatioByRegion(@RequestParam String writerId);
}
