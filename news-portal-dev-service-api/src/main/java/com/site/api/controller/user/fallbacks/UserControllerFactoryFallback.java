package com.site.api.controller.user.fallbacks;

import com.site.api.controller.user.UserControllerApi;
import com.site.grace.result.GraceJSONResult;
import com.site.grace.result.ResponseStatusEnum;
import com.site.pojo.bo.UpdateUserInfoBO;
import com.site.pojo.vo.AppUserVO;
//import org.springframework.cloud.openfeign.FallbackFactory;
import feign.hystrix.FallbackFactory;
import org.springframework.stereotype.Component;


import java.util.ArrayList;
import java.util.List;

@Component
public class UserControllerFactoryFallback implements FallbackFactory<UserControllerApi> {

    @Override
    public UserControllerApi create(Throwable cause) {
        return new UserControllerApi() {
            @Override
            public GraceJSONResult getUserInfo(String userId) {
                return GraceJSONResult.errorCustom(ResponseStatusEnum.SYSTEM_RESPONSE_FEIGN);
            }

            @Override
            public GraceJSONResult getAccountInfo(String userId) {
                return GraceJSONResult.errorCustom(ResponseStatusEnum.SYSTEM_RESPONSE_FEIGN);
            }

            @Override
            public GraceJSONResult updateUserInfo(UpdateUserInfoBO updateUserInfoBO) {
                return GraceJSONResult.errorCustom(ResponseStatusEnum.SYSTEM_RESPONSE_FEIGN);
            }

            @Override
            public GraceJSONResult queryByIds(String userIds) {
                System.out.println("Circuit breaker for entering client");
                List<AppUserVO> publisherList = new ArrayList<>();
                return GraceJSONResult.ok(publisherList);
            }
        };
    }
}
