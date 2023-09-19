package com.site.article.mapper;

import com.site.my.mapper.MyMapper;
import com.site.pojo.Comments;
import com.site.pojo.vo.CommentsVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface CommentsMapperCustom {

    /**
     * Query article review
     */
    public List<CommentsVO> queryArticleCommentList(@Param("paramMap") Map<String, Object> map);


}