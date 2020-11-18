package com.changgou.task.config;


import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitmqConfig {

    public static final String AUTO_CONFIRM_QUEUE="auto_confirm_queue";

    @Bean
    public Queue autoConfirmQueue(){
        return new Queue(AUTO_CONFIRM_QUEUE);
    }
}
