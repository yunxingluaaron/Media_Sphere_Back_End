package com.site.admin.service;

import com.site.pojo.AdminUser;
import com.site.pojo.bo.NewAdminBO;
import com.site.utils.PagedGridResult;

public interface AdminUserService {

    /**
     * Get admin user's info
     * @param username
     * @return
     */
    public AdminUser queryAdminByUsername(String username);

    /**
     * Add new admin
     * @param newAdminBO
     */
    public void createAdminUser(NewAdminBO newAdminBO);

    /**
     * Paging query admin list
     * @param page
     * @param pageSize
     */
    public PagedGridResult queryAdminList(Integer page, Integer pageSize);
}
