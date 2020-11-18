package com.changgou.user.listener;

import com.alibaba.fastjson.JSON;
import com.changgou.user.config.RabbitmqConfig;
import com.changgou.order.pojo.Task;
import com.changgou.user.service.UserService;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
/*
    监听spring-task的任务
 */
@Component
public class TaskListener {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private UserService userService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    //注册监听器，监听积分任务开启队列：point_update_queue
    @RabbitListener(queues = {RabbitmqConfig.QUEUE_TASK_POINT_UPDATE})
    public void taskListen(String message){
        //1、解析mq中传递过来的消息，如果为null或者任务内容为空则结束方法
        System.out.println("监听到rabbitmq的添加积分队列的信息");
        Task task = JSON.parseObject(message, Task.class);
        if (task ==null || StringUtils.isEmpty(task.getRequestBody())){
            return;
        }
        //2、查询redis，key为task的id，结果仅是一个作为判断的标记，如果标记存在在redis中，则说明已经有线程正在操作此任务，结束方法
        Object flag = redisTemplate.boundValueOps(task.getId()).get();
        if(flag!=null){
            return;
        }
        //3、由于要对多个数据表进行操作，调用服务并添加本地事务注解，需要将事务成功的消息回执传递给order服务
        int result = userService.updatePoint(task);
        if(result==1){
            rabbitTemplate.convertAndSend(RabbitmqConfig.EXCHANGE_POINT_TASK,RabbitmqConfig.KEY_POINT_COMPLETE,message);
        }
    }
}
