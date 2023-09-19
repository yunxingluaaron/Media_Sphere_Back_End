package com.site.article.service.impl;

import com.github.pagehelper.PageHelper;
import com.site.api.service.BaseService;
import com.site.article.mapper.ArticleMapper;
import com.site.article.service.ArticlePortalService;
import com.site.enums.ArticleReviewStatus;
import com.site.enums.YesOrNo;
import com.site.pojo.Article;
import com.site.pojo.vo.ArticleDetailVO;
import com.site.utils.PagedGridResult;
//import com.site.utils.extend.AliTextReviewUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class ArticlePortalServiceImpl extends BaseService implements ArticlePortalService {

    @Autowired
    private ArticleMapper articleMapper;

    @Override
    public PagedGridResult queryIndexArticleList(String keyword,
                                                 Integer category,
                                                 Integer page,
                                                 Integer pageSize) {

        Example articleExample = new Example(Article.class);
        Example.Criteria criteria = setDefaultArticleExample(articleExample);

        if (StringUtils.isNotBlank(keyword)) {
            criteria.andLike("title", "%" + keyword + "%");
        }
        if (category != null) {
            criteria.andEqualTo("categoryId", category);
        }

        PageHelper.startPage(page, pageSize);
        List<Article> list = articleMapper.selectByExample(articleExample);

        return setterPagedGrid(list, page);
    }

    @Override
    public List<Article> queryHotList() {
        Example articleExample = new Example(Article.class);
        Example.Criteria criteria = setDefaultArticleExample(articleExample);

        PageHelper.startPage(1, 5);
        List<Article> list  = articleMapper.selectByExample(articleExample);
        return list;
    }

    private Example.Criteria setDefaultArticleExample(Example articleExample) {

        articleExample.orderBy("publishTime").desc();
        Example.Criteria criteria = articleExample.createCriteria();

        /**
         * Implicit requirements for querying articles on homepage.
         * isAppoint=published，Already published articles, including those published after pending
         * isDelete=no，Only non-deleted articles will be demonstrated
         * articleStatus=review passed，Only the articles that passed review can be demonstrated
         */
        criteria.andEqualTo("isAppoint", YesOrNo.NO.type);
        criteria.andEqualTo("isDelete", YesOrNo.NO.type);
        criteria.andEqualTo("articleStatus", ArticleReviewStatus.SUCCESS.type);

        return criteria;
    }

    @Override
    public PagedGridResult queryArticleListOfWriter(String writerId, Integer page, Integer pageSize) {
        Example articleExample = new Example(Article.class);

        Example.Criteria criteria = setDefaultArticleExample(articleExample);
        criteria.andEqualTo("publishUserId", writerId);

        PageHelper.startPage(page, pageSize);
        List<Article> list = articleMapper.selectByExample(articleExample);
        return setterPagedGrid(list, page);
    }

    @Override
    public PagedGridResult queryGoodArticleListOfWriter(String writerId) {
        Example articleExample = new Example(Article.class);
        articleExample.orderBy("publishTime").desc();

        Example.Criteria criteria = setDefaultArticleExample(articleExample);
        criteria.andEqualTo("publishUserId", writerId);

        PageHelper.startPage(1, 5);
        List<Article> list = articleMapper.selectByExample(articleExample);
        return setterPagedGrid(list, 1);
    }

    @Override
    public ArticleDetailVO queryDetail(String articleId) {

        Article article = new Article();
        article.setId(articleId);
        article.setIsAppoint(YesOrNo.NO.type);
        article.setIsDelete(YesOrNo.NO.type);
        article.setArticleStatus(ArticleReviewStatus.SUCCESS.type);

        Article result = articleMapper.selectOne(article);

        ArticleDetailVO detailVO = new ArticleDetailVO();
        BeanUtils.copyProperties(result, detailVO);

        // in detailVO: cover; in Article: article_cover
        detailVO.setCover(result.getArticleCover());

        return detailVO;
    }
}
