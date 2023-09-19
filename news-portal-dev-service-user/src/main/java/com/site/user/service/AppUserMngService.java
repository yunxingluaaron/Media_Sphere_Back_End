package com.site.user.service;

import com.site.pojo.AppUser;
import com.site.pojo.bo.UpdateUserInfoBO;
import com.site.utils.PagedGridResult;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Date;

public interface AppUserMngService {

    /**
     * Query all user list
     * @param nickname
     * @param status
     * @param startDate
     * @param endDate
     * @param page
     * @param pageSize
     * @return
     */
    public PagedGridResult queryAllUserList(String nickname, Integer status, Date startDate,
                                            Date endDate, Integer page, Integer pageSize);

    /**
     * Block / unblock user account
     * @param userId
     * @param doStatus
     */
    public void freezeUserOrNot(String userId, Integer doStatus);

}
