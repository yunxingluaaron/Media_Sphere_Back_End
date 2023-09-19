package com.site.user.mapper;

import com.site.my.mapper.MyMapper;
import com.site.pojo.AppUser;
import org.springframework.stereotype.Repository;

@Repository
public interface AppUserMapper extends MyMapper<AppUser> {
}