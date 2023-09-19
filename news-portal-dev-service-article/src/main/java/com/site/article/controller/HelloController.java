package com.site.article.controller;

import com.site.api.config.RabbitMQConfig;
import com.site.api.config.RabbitMQDelayConfig;
import com.site.api.controller.user.HelloControllerApi;
import com.site.article.stream.StreamService;
import com.site.grace.result.GraceJSONResult;
import com.site.utils.RedisOperator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController
@RequestMapping("producer")
public class HelloController implements HelloControllerApi {

    final static Logger logger = LoggerFactory.getLogger(HelloController.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private StreamService streamService;

//    @GetMapping("/hello")
//    public Object hello() {
//        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_ARTICLE, "article.hello", "Test message");
//        return GraceJSONResult.ok();
//    }

    @GetMapping("/hello")
    public Object hello() {
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_ARTICLE, "article.aaa.bbb.do", "Test message2");
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_ARTICLE, "article.success.do", "Test message3");
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_ARTICLE, "article.play", "Test message4");
        return GraceJSONResult.ok();
    }


    @GetMapping("/delay")
    public Object delay() {

        MessagePostProcessor messagePostProcessor = new MessagePostProcessor() {
            @Override
            public Message postProcessMessage(Message message) throws AmqpException {
                // Set persistence mode for messages
                message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                // Set the delayed time for messages(ms)
                message.getMessageProperties().setDelay(5000);
                return message;
            }
        };


        rabbitTemplate.convertAndSend(RabbitMQDelayConfig.EXCHANGE_DELAY, "delay.demo", "This is a delayed message", messagePostProcessor);
        System.out.println("Producer time: " + new Date());
        return "OK";
    }

    @GetMapping("/stream")
    public Object stream() {
//        streamService.sendMsg();

        for (int i = 0; i < 10; i ++) {
            streamService.sendMsgToGroup("No.:" + ( i + 1 ));
        }
        return "ok!!";
    }
}
