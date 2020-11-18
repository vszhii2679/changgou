package com.changgou.user.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitmqConfig {

    //积分任务开启队列名
    public static final String QUEUE_TASK_POINT_UPDATE = "point_update_queue";
    //积分任务完成队列名
    public static final String QUEUE_TASK_POINT_COMPLETE = "point_complete_queue";
    //积分任务开启路由key名
    public static final String KEY_POINT_UPDATE = "key_point_update";
    //积分任务完成路由key名
    public static final String KEY_POINT_COMPLETE = "key_point_complete";
    //积分任务交换机名
    public static final String EXCHANGE_POINT_TASK = "exchange_point_task";


    //积分任务开启队列
    @Bean("taskPointUpdate")
    public Queue taskPointUpdateQueue(){
        return new Queue(QUEUE_TASK_POINT_UPDATE);
    }

    //积分任务开启队列
    @Bean("taskPointComplete")
    public Queue taskPointCompleteQueue(){
        return new Queue(QUEUE_TASK_POINT_COMPLETE);
    }


    //积分任务交换机
    @Bean("pointTaskExchange")
    public Exchange pointTaskExchange(){
        return ExchangeBuilder.directExchange(EXCHANGE_POINT_TASK).durable(true).build();
    }

    //积分任务交换机绑定积分任务开启队列
    @Bean
    public Binding pointUpdateBinding(@Qualifier("pointTaskExchange")Exchange exchange,@Qualifier("taskPointUpdate")Queue queue){
        return BindingBuilder.bind(queue).to(exchange).with(KEY_POINT_UPDATE).noargs();
    }

    //积分任务交换机绑定积分任务完成队列
    @Bean
    public Binding pointCompleteBinding(@Qualifier("pointTaskExchange")Exchange exchange,@Qualifier("taskPointComplete")Queue queue){
        return BindingBuilder.bind(queue).to(exchange).with(KEY_POINT_COMPLETE).noargs();
    }

}
