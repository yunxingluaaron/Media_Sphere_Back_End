package com.site.user.controller;

import com.site.api.BaseController;
import com.site.api.controller.user.HelloControllerApi;
import com.site.api.controller.user.MyFansControllerApi;
import com.site.enums.Sex;
import com.site.grace.result.GraceJSONResult;
import com.site.grace.result.ResponseStatusEnum;
import com.site.pojo.vo.FansCountsVO;
import com.site.user.service.MyFanService;
import com.site.utils.RedisOperator;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MyFansController extends BaseController implements MyFansControllerApi {

    final static Logger logger = LoggerFactory.getLogger(MyFansController.class);

    @Autowired
    private MyFanService myFanService;

    @Override
    public GraceJSONResult isFollowThisWriter(String writerId,
                                                String fanId) {
        if (StringUtils.isBlank(fanId) || StringUtils.isBlank(writerId)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.USER_STATUS_ERROR);
        }

        boolean res = myFanService.isFollowThisWriter(writerId, fanId);
        return GraceJSONResult.ok(res);
    }

    @Override
    public GraceJSONResult follow(String writerId, String fanId) {
        myFanService.follow(writerId, fanId);
        return GraceJSONResult.ok();
    }

    @Override
    public GraceJSONResult unfollow(String writerId, String fanId) {
        myFanService.unfollow(writerId, fanId);
        return GraceJSONResult.ok();
    }

    @Override
    public GraceJSONResult queryAll(String writerId,
                                    Integer page,
                                    Integer pageSize) {

        if (StringUtils.isBlank(writerId)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.USER_NOT_EXIST_ERROR);
        }

        if (page == null) {
            page = COMMON_START_PAGE;
        }
        if (pageSize == null) {
            pageSize = COMMON_PAGE_SIZE;
        }

        // query info from database
//        return GraceJSONResult.ok(myFanService.queryMyFansList(writerId,
//                                    page,
//                                    pageSize));
        // query info from es
        return GraceJSONResult.ok(myFanService.queryMyFansESList(writerId,
                page,
                pageSize));
    }

    @Override
    public GraceJSONResult queryRatio(String writerId) {

//        int manCounts = myFanService.queryFansCounts(writerId, Sex.man);
//        int womanCounts = myFanService.queryFansCounts(writerId, Sex.woman);
//
//        FansCountsVO fansCountsVO = new FansCountsVO();
//        fansCountsVO.setManCounts(manCounts);
//        fansCountsVO.setWomanCounts(womanCounts);

        FansCountsVO fansCountsVO = myFanService.queryFansESCounts(writerId);
        return GraceJSONResult.ok(fansCountsVO);
    }

    @Override
    public GraceJSONResult queryRatioByRegion(String writerId) {
        return GraceJSONResult.ok(myFanService
                .queryRegionRatioESCounts(writerId));
    }

    @Override
    public GraceJSONResult forceUpdateFanInfo(String relationId, String fanId) {
        myFanService.forceUpdateFanInfo(relationId, fanId);
        return GraceJSONResult.ok();
    }
}
