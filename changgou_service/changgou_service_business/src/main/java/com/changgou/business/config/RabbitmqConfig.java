package com.changgou.business.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitmqConfig {
    //缓存预热队列名
    public static final String AD_UPDATE_QUEUE = "ad_update_queue";

    @Bean("adUpdateQueue")
    public Queue adUpdateQueue(){
        return new Queue(AD_UPDATE_QUEUE);
    }

}
