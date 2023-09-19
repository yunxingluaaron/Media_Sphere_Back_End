package com.site.user.service;

import com.site.pojo.AppUser;
import com.site.pojo.bo.UpdateUserInfoBO;

public interface UserService {

    /*
    * Check if user is exist. If user is exist, return user info
    * */
    public AppUser queryMobileIsExist (String mobile);

    /*
    * Create new user, add the user to database.
    * */
    public AppUser createUser(String mobile);

    /*
    * Query user info by user id
    * */
    public AppUser getUser(String userId);

    /*
    * Update user info and activate user account
    * */
    public void updateUserInfo(UpdateUserInfoBO updateUserInfoBO);

}
