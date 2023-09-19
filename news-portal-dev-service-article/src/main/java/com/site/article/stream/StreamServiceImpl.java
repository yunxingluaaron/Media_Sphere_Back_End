package com.site.article.stream;

import com.site.pojo.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
/*
* Enable binding
* Bind channel
* */
@Component
@EnableBinding(MyStreamChannel.class)
public class StreamServiceImpl implements StreamService {

    @Autowired
    private MyStreamChannel myStreamChannel;

    @Override
    public void sendMsg() {

        AppUser user = new AppUser();
        user.setId("10101");
        user.setNickname("news");

        // The binder send message to MQ
        myStreamChannel.output().send(MessageBuilder.withPayload(user).build());
    }

    @Override
    public void sendMsgToGroup(String str) {
        myStreamChannel.output().send(MessageBuilder.withPayload(str).build());
    }
}
