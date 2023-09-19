package com.site.admin.repository;

import com.site.pojo.mo.FriendLinkMO;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface FriendLinkRepository extends MongoRepository<FriendLinkMO, String> {

    public List<FriendLinkMO> getAllByIsDelete(Integer isDelete);



}