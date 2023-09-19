package com.site.admin.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.site.admin.mapper.AdminUserMapper;
import com.site.admin.service.AdminUserService;
import com.site.api.service.BaseService;
import com.site.exception.GraceException;
import com.site.grace.result.ResponseStatusEnum;
import com.site.pojo.AdminUser;
import com.site.pojo.bo.NewAdminBO;
import com.site.utils.PagedGridResult;
import org.apache.commons.lang3.StringUtils;
import org.n3r.idworker.Sid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.List;

@Service
public class AdminUserServiceImpl extends BaseService implements AdminUserService {

    @Autowired
    public AdminUserMapper adminUserMapper;

    @Autowired
    private Sid sid;

    @Override
    public AdminUser queryAdminByUsername(String username) {

        Example adminExample = new Example(AdminUser.class);
        Example.Criteria criteria = adminExample.createCriteria();
        criteria.andEqualTo("username", username);

        AdminUser admin = adminUserMapper.selectOneByExample(adminExample);
        return admin;
    }

    @Transactional
    @Override
    public void createAdminUser(NewAdminBO newAdminBO) {
        String adminId = sid.nextShort();
        AdminUser adminUser = new AdminUser();
        adminUser.setId(adminId);
        adminUser.setUsername(newAdminBO.getUsername());
        adminUser.setAdminName(newAdminBO.getAdminName());

        // If got inputted password, encrypt the password and save to the database
        if (StringUtils.isNotBlank(newAdminBO.getPassword())) {
            String pwd = BCrypt.hashpw(newAdminBO.getPassword(), BCrypt.gensalt());
            adminUser.setPassword(pwd);
        }

        // For faceId, if user uploads faceId, bind it with admin info and save to the database
        if (StringUtils.isNotBlank(newAdminBO.getFaceId())) {
            adminUser.setFaceId(newAdminBO.getFaceId());
        }

        adminUser.setCreatedTime(new Date());
        adminUser.setUpdatedTime(new Date());

        int result = adminUserMapper.insert(adminUser);
        if (result != 1) { // not success
            GraceException.display(ResponseStatusEnum.ADMIN_CREATE_ERROR);
        }
    }

    @Override
    public PagedGridResult queryAdminList(Integer page, Integer pageSize) {

        Example adminExample = new Example(AdminUser.class);
        adminExample.orderBy("createdTime").desc();

        PageHelper.startPage(page, pageSize);// use pageHelper for set pages (kind of adding an interceptor)
        List<AdminUser> adminUserList = adminUserMapper.selectByExample(adminExample);

        return setterPagedGrid(adminUserList, page);
    }

//    private PagedGridResult setterPagedGrid(List<?> adminUserList, Integer page) {
//
//        PageInfo<?> pageList = new PageInfo<>(adminUserList);
//        PagedGridResult gridResult = new PagedGridResult();
//        gridResult.setRows(adminUserList);
//        gridResult.setPage(page);
//        gridResult.setRecords(pageList.getPages()); // No. of pages
//        gridResult.setTotal(pageList.getTotal()); // No. of total record
//
//        return gridResult;
//    }
}
