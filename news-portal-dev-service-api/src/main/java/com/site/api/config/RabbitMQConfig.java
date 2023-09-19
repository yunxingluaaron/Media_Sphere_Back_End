package com.site.api.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class of RabbitMQ
 */
@Configuration
public class RabbitMQConfig {

    // Set switch name
    public static final String EXCHANGE_ARTICLE = "exchange_article";

    // Set queue name
    public static final String QUEUE_DOWNLOAD_HTML = "queue_download_html";

    // Create switcher
    @Bean(EXCHANGE_ARTICLE)
    public Exchange exchange() {

        return ExchangeBuilder.topicExchange(EXCHANGE_ARTICLE)
                              .durable(true) // 持久化
                              .build();
    }

    //Create queue
    @Bean(QUEUE_DOWNLOAD_HTML)
    public Queue queue() {

        return new Queue(QUEUE_DOWNLOAD_HTML);
    }

    // Combine queue with the switcher
    @Bean
    public Binding binding(@Qualifier(QUEUE_DOWNLOAD_HTML) Queue queue,
                           @Qualifier(EXCHANGE_ARTICLE) Exchange exchange) {
        return BindingBuilder.bind(queue) // define binding relationship
                .to(exchange) // bind queue
                .with("article.#.do") // Set router rules (requestMapping)
                .noargs(); // Perform binding
    }

}
