package com.changgou.order.listener;

import com.alibaba.fastjson.JSON;
import com.changgou.order.config.RabbitmqConfig;
import com.changgou.order.dao.TaskMapper;
import com.changgou.order.pojo.Task;
import com.changgou.order.service.TaskService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TaskResultListener {

    @Autowired
    private TaskService taskService;

    @RabbitListener(queues = {RabbitmqConfig.QUEUE_TASK_POINT_COMPLETE})
    public void getTaskResult(String message){
        System.out.println("【监听到rabbitmq积分完成队列的信息】");
        Task task = JSON.parseObject(message, Task.class);
        taskService.completeTask(task);
    }
}
