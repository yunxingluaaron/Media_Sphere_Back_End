package com.site.article.stream;

import com.site.pojo.AppUser;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.stereotype.Component;

/*
* Build consumer
* */
@Component
@EnableBinding(MyStreamChannel.class)
public class MyStreamConsumer {

    /*
    * Listen from MQ for consuming messages. Implement methods for message
    * */
//    @StreamListener(MyStreamChannel.INPUT)
//    public void receiveMsg(AppUser user) {
//        System.out.println(user.toString());
//    }

    @StreamListener(MyStreamChannel.INPUT)
    public void receiveMsg(String testStr) {
        System.out.println(testStr);
    }
}
