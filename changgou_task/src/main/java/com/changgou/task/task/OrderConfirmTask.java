package com.changgou.task.task;

import com.changgou.task.config.RabbitmqConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OrderConfirmTask {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    //定时任务：一天一次，用来查询数据库中订单数据，用来自动确认
    @Scheduled(cron = "0 0 0 * * ?")
    public void take4AutoConfirmOrder(){
        rabbitTemplate.convertAndSend("", RabbitmqConfig.AUTO_CONFIRM_QUEUE,"1");
    }
}
