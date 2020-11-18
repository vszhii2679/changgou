package com.changgou.search.listener;

import com.changgou.search.config.RabbitmqConfig;
import com.changgou.search.service.Es5Service;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GoodsStatusListener {


    @Autowired
    private Es5Service es5Service;

    @RabbitListener(queues = RabbitmqConfig.GOODS_UP_QUEUE)
    public void goodsUp(String spuId){
        System.out.println("【监听到rabbitmq中商品上架队列的信息】");
        System.out.println("spuId = " + spuId);
        es5Service.importBySpuId(spuId);
    }

    @RabbitListener(queues = RabbitmqConfig.GOODS_DOWN_QUEUE)
    public void goodsDown(String spuId){
        System.out.println("【监听到rabbitmq中商品下架队列的信息】");
        System.out.println("spuId = " + spuId);
        es5Service.downBySpuId(spuId);
    }
}
