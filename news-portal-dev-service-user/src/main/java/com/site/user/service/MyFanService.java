package com.site.user.service;

import com.site.enums.Sex;
import com.site.pojo.vo.FansCountsVO;
import com.site.pojo.vo.RegionRatioVO;
import com.site.utils.PagedGridResult;

import java.util.List;

public interface MyFanService {

    /**
     * Query if current user is following the author
     */
    public boolean isFollowThisWriter(String writerId, String fanId);


    /**
     * follow
     */
    public void follow(String writerId, String fanId);

    /**
     * unfollow
     */
    public void unfollow(String writerId, String fanId);

    /**
     * Query the list of followers from database
     */
    public PagedGridResult queryMyFansList(String writerId,
                                           Integer page,
                                           Integer pageSize);

    /**
     * Query the list of followers from ES
     */
    public PagedGridResult queryMyFansESList(String writerId,
                                           Integer page,
                                           Integer pageSize);

    /**
     * get fans number by gender
     */
    public Integer queryFansCounts(String writerId, Sex sex);
    public FansCountsVO queryFansESCounts(String writerId);

    /**
     * get fans number by region
     */
    public List<RegionRatioVO> queryRegionRatioCounts(String writerId);
    public List<RegionRatioVO> queryRegionRatioESCounts(String writerId);

    /**
     * passive update user's fan info
     */
    public void forceUpdateFanInfo(String relationId, String fanId);
}
