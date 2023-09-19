package com.site.admin.service;

import com.site.pojo.AdminUser;
import com.site.pojo.bo.NewAdminBO;
import com.site.pojo.mo.FriendLinkMO;
import com.site.utils.PagedGridResult;

import java.util.List;

public interface FriendLinkService {

    /**
     * Add / modify friend link
     * @param friendLinkMO
     */
    public void saveOrUpdateFriendLink(FriendLinkMO friendLinkMO);

    /**
     * Add / query the list of friend link
     */
    public List<FriendLinkMO> queryAllFriendList ();

    /**
     * Add / delete friend link
     */
    public void deleteFriendList (String linkId);

    /**
     * Homepage: query the list of friend link
     * @return
     */
    public List<FriendLinkMO> queryPortalAllFriendLinkList();

}
