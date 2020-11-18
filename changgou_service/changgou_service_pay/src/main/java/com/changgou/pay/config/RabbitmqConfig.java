package com.changgou.pay.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitmqConfig {

    public static final String PAY_SUCCESS_QUEUE="pay_success_queue";


    @Bean("paySuccessQueue")
    public Queue paySuccessQueue(){
        return new Queue(PAY_SUCCESS_QUEUE);
    }
}
