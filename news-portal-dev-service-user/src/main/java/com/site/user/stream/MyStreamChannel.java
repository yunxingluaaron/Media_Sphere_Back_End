package com.site.user.stream;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.stereotype.Component;


/*
* Set up channels
*
* */
@Component
public interface MyStreamChannel {

    String OUTPUT = "myOutput";
    String INPUT = "myInput";

    @Output(MyStreamChannel.OUTPUT)
    MessageChannel output();
    @Input(MyStreamChannel.INPUT)
    SubscribableChannel input();

}
