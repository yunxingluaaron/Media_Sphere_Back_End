package com.site.article.task;

import com.site.article.service.ArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

// Spring Boot Scheduling is not good for database that containing huge data
// FIXME: Use MQ for pending posts
//@Configuration // 1. mark configuration class, make sure Spring Boot container can obtain this class
//@EnableScheduling // 2. enable task scheduling
public class TaskPublishArticles {

    @Autowired
    private ArticleService articleService;

    // 3. add schedule expression for Spring
    @Scheduled(cron = "0/10 * * * * ?")
    private void publishArticles() {
        // 4. Call article service, change articles that need to be published to immediate status.
        articleService.updateAppointToPublish();
    }
}
