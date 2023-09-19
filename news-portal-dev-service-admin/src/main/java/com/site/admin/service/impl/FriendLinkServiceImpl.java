package com.site.admin.service.impl;

import com.site.admin.repository.FriendLinkRepository;
import com.site.admin.service.FriendLinkService;
import com.site.enums.YesOrNo;
import com.site.pojo.mo.FriendLinkMO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FriendLinkServiceImpl implements FriendLinkService {

    @Autowired
    private FriendLinkRepository friendLinkRepository;

    @Override
    public void saveOrUpdateFriendLink(FriendLinkMO friendLinkMO) {
        friendLinkRepository.save(friendLinkMO);
    }

    @Override
    public List<FriendLinkMO> queryAllFriendList() {

//        Pageable pageable = PageRequest.of(1,10);
//        friendLinkRepository.findAll(pageable);

        return friendLinkRepository.findAll();
    }

    @Override
    public void deleteFriendList(String linkId) {
        friendLinkRepository.deleteById(linkId);
    }

    @Override
    public List<FriendLinkMO> queryPortalAllFriendLinkList() {
        return friendLinkRepository.getAllByIsDelete(YesOrNo.NO.type);
    }
}
