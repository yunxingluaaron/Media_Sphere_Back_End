package com.site.zuul;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.context.annotation.ComponentScan;


@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class,
        MongoAutoConfiguration.class, RabbitAutoConfiguration.class})
@ComponentScan(basePackages = {"com.site", "org.n3r.idworker"})
@EnableEurekaClient
//@EnableZuulServer
@EnableZuulProxy // Use proxy when collaborate zuul with Eureka, Ribbon and other component. It can be take as a strengthened version of zuulServer.
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
