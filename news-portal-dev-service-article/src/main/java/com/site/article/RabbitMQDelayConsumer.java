package com.site.article;

import com.site.api.config.RabbitMQConfig;
import com.site.api.config.RabbitMQDelayConfig;
import com.site.article.controller.ArticleController;
import com.site.article.service.ArticleService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class RabbitMQDelayConsumer {

    @Autowired
    private ArticleService articleService;




    @RabbitListener(queues = {RabbitMQDelayConfig.QUEUE_DELAY})
    public void watchQueue(String payload, Message message) {
        System.out.println(payload);

        String routingKey = message.getMessageProperties().getReceivedRoutingKey();
        System.out.println(routingKey);

        System.out.println("Consumer time: " + new Date());

        // When consumer received the delayed message from MQ, it changes the article status to "published"
        String articleId = payload;
        articleService.updateArticleToPublish(articleId);


    }


}
