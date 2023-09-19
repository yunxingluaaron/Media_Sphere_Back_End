package com.site.user.service.impl;

import com.site.enums.Sex;
import com.site.enums.UserStatus;
import com.site.exception.GraceException;
import com.site.grace.result.ResponseStatusEnum;
import com.site.pojo.AppUser;
import com.site.pojo.bo.UpdateUserInfoBO;
import com.site.user.mapper.AppUserMapper;
import com.site.user.service.UserService;
import com.site.utils.DateUtil;
import com.site.utils.DesensitizationUtil;
import com.site.utils.JsonUtils;
import com.site.utils.RedisOperator;
import org.n3r.idworker.Sid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    public AppUserMapper appUserMapper;

    @Autowired
    public Sid sid;

    @Autowired
    public RedisOperator redis;

    public static final String REDIS_USER_INFO = "redis_user_info";

    private static final String USER_FACE0 = "http://122.152.205.72:88/group1/M00/00/05/CpoxxFw_8_qAIlFXAAAcIhVPdSg994.png";
    private static final String USER_FACE1 = "http://122.152.205.72:88/group1/M00/00/05/CpoxxF6ZUySASMbOAABBAXhjY0Y649.png";
    private static final String USER_FACE2 = "http://122.152.205.72:88/group1/M00/00/05/CpoxxF6ZUx6ANoEMAABTntpyjOo395.png";

    @Override
    public AppUser queryMobileIsExist(String mobile) {
        // 查询的实例
        Example userExample = new Example(AppUser.class);
        Example.Criteria userCriteria = userExample.createCriteria();
        userCriteria.andEqualTo("mobile", mobile);
        AppUser user = appUserMapper.selectOneByExample(userExample);

        return user;
    }

    @Transactional // 事务
    @Override
    public AppUser createUser(String mobile) {

        /*
        *  考虑可扩展性，
        * 如果未来业务发展，需要分库分表，
        * 那么数据库表主键id必须保证全局（全库）唯一，不得重复
        * */
        String userId = sid.nextShort();
        AppUser user = new AppUser();

        // Global ID
        user.setId(userId);
        user.setMobile(mobile);
        user.setNickname("User:" + DesensitizationUtil.commonDisplay(mobile));
        user.setFace(USER_FACE0);

        user.setBirthday(DateUtil.stringToDate("1900-01-01"));
        user.setSex(Sex.secret.type);
        user.setActiveStatus(UserStatus.INACTIVE.type);

        user.setTotalIncome(0);
        user.setCreatedTime(new Date());
        user.setUpdatedTime(new Date());

        appUserMapper.insert(user);
        return user;
    }

    @Override
    public AppUser getUser(String userId) {
        return appUserMapper.selectByPrimaryKey(userId);
    }

    @Override
    public void updateUserInfo(UpdateUserInfoBO updateUserInfoBO) {

        String userId = updateUserInfoBO.getId();

        // Make sure data in database and Redis is same.
        // Delete data in Redis first, then update database
        redis.del(REDIS_USER_INFO + ":" + userId);

        AppUser userInfo = new AppUser();
        BeanUtils.copyProperties(updateUserInfoBO, userInfo);

        userInfo.setUpdatedTime(new Date());
        userInfo.setActiveStatus(UserStatus.ACTIVE.type);

        int result = appUserMapper.updateByPrimaryKeySelective(userInfo);
        if (result != 1) {
            GraceException.display(ResponseStatusEnum.USER_UPDATE_ERROR);
        }

        // check user info again. Put the info into Redis
        AppUser user = getUser(userId);
        redis.set(REDIS_USER_INFO + ":" + userId, JsonUtils.objectToJson(user));

        // Cache and delete twice
        try {
            Thread.sleep(200);
            redis.del(REDIS_USER_INFO + ":" + userId);
            redis.set(REDIS_USER_INFO + ":" + userId, JsonUtils.objectToJson(user), 7);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
