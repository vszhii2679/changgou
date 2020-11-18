package com.changgou.order.task;

import com.alibaba.fastjson.JSON;
import com.changgou.order.config.RabbitmqConfig;
import com.changgou.order.dao.TaskMapper;
import com.changgou.order.pojo.Task;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
public class TaskPublish {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private TaskMapper taskMapper;

    //开启spring的定时任务注解，每五秒调用一次taskPublish方法
    @Scheduled(cron = "0/10 * * * * ?")
    public void taskPublish(){
        //通过spring task 任务查询task数据表中的信息，将查询出来的task集合发送至积分队列服务
        List<Task> taskList = taskMapper.findTaskBeforeCurrentTime(new Date());
        for (Task task : taskList) {
            rabbitTemplate.convertAndSend(RabbitmqConfig.EXCHANGE_POINT_TASK,RabbitmqConfig.KEY_POINT_UPDATE, JSON.toJSONString(task));
        }
    }
}
