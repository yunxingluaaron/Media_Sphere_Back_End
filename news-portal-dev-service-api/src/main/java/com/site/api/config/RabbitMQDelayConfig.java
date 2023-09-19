package com.site.api.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class of RabbitMQ
 */
@Configuration
public class RabbitMQDelayConfig {

    // Set switch name
    public static final String EXCHANGE_DELAY = "exchange_delay";

    // Set queue name
    public static final String QUEUE_DELAY = "queue_delay";

    // Create switcher
    @Bean(EXCHANGE_DELAY)
    public Exchange exchange() {

        return ExchangeBuilder.topicExchange(EXCHANGE_DELAY)
                              .delayed() // Enable delayed message
                              .durable(true) // 持久化
                              .build();
    }

    //Create queue
    @Bean(QUEUE_DELAY)
    public Queue queue() {

        return new Queue(QUEUE_DELAY);
    }

    // Combine queue with the switcher
    @Bean
    public Binding delayBinding(@Qualifier(QUEUE_DELAY) Queue queue,
                           @Qualifier(EXCHANGE_DELAY) Exchange exchange) {
        return BindingBuilder.bind(queue) // define binding relationship
                .to(exchange) // bind queue
                .with("publish.delay.#") // Set router rules (requestMapping)
                .noargs(); // Perform binding
    }

}
