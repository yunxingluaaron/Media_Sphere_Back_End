package com.site.article.mapper;

import com.site.my.mapper.MyMapper;
import com.site.pojo.Article;
import org.springframework.stereotype.Repository;

@Repository
public interface ArticleMapper extends MyMapper<Article> {
}