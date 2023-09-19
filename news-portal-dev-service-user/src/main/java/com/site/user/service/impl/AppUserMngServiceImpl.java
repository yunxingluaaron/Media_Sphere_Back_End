package com.site.user.service.impl;

import com.github.pagehelper.PageHelper;
import com.site.api.service.BaseService;
import com.site.enums.UserStatus;
import com.site.exception.GraceException;
import com.site.grace.result.ResponseStatusEnum;
import com.site.pojo.AppUser;
import com.site.user.mapper.AppUserMapper;
import com.site.user.service.AppUserMngService;
import com.site.utils.PagedGridResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import javax.jws.Oneway;
import java.util.Date;
import java.util.List;

@Service
public class AppUserMngServiceImpl extends BaseService implements AppUserMngService {

    @Autowired
    public AppUserMapper appUserMapper;

    @Override
    public PagedGridResult queryAllUserList(String nickname, Integer status, Date startDate, Date endDate, Integer page, Integer pageSize) {

        Example example = new Example(AppUser.class);
        example.orderBy("createdTime").desc();
        Example.Criteria criteria = example.createCriteria();

        if (StringUtils.isNotBlank(nickname)) {
            criteria.andLike("nickname", "%" + nickname + "%"); // Fuzzy query, need "%" on both side of nickname
        }

        if (UserStatus.isUserStatusValid(status)) {
            criteria.andEqualTo("activeStatus", status);
        }

        if (startDate != null) {
            criteria.andGreaterThanOrEqualTo("createdTime", startDate);
        }
        if (startDate != null) {
            criteria.andLessThanOrEqualTo("createdTime", endDate);
        }

        PageHelper.startPage(page, pageSize);
        List<AppUser> list = appUserMapper.selectByExample(example);

        return setterPagedGrid(list, page);
    }

    @Transactional
    @Override
    public void freezeUserOrNot(String userId, Integer doStatus) {
        AppUser user = new AppUser();
        user.setId(userId);
        user.setActiveStatus(doStatus);
        try {
            appUserMapper.updateByPrimaryKeySelective(user);
        } catch (Exception e) {
            GraceException.display(ResponseStatusEnum.USER_STATUS_ERROR);
        }
    }


}
