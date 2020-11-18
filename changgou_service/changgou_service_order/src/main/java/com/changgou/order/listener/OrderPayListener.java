package com.changgou.order.listener;

import com.alibaba.fastjson.JSON;
import com.changgou.order.config.RabbitmqConfig;
import com.changgou.order.service.OrderService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


import java.util.Map;

@Component
public class OrderPayListener {

    @Autowired
    private OrderService orderService;


    @RabbitListener(queues = RabbitmqConfig.PAY_SUCCESS_QUEUE)
    public void updatePayStatus(String message) {
        System.out.println("【接收到支付成功队列的消息】");
        Map map = JSON.parseObject(message, Map.class);
        String orderId = (String) map.get("out_trade_no");
        String transactionId = (String) map.get("transaction_id");
        orderService.updatePayStatus(orderId,transactionId);
    }

    @RabbitListener(queues = "queue.ordertimeout")
    public void orderPayTimeout(String message){
        System.out.println("【接收到交易关闭队列的消息】");
        orderService.orderClose(message);
    }

    @RabbitListener(queues = RabbitmqConfig.AUTO_CONFIRM_QUEUE)
    public void autoConfirmOrder(String message){
        System.out.println("【接收到定时自动签收任务队列的消息】");
        orderService.autoConfirmOrder();
    }
}
