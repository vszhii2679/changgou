package com.changgou.canal.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitmqConfig {

    //缓存预热队列名
    public static final String AD_UPDATE_QUEUE = "ad_update_queue";
    //商品上架队列名
    public static final String GOODS_UP_QUEUE = "goods_up_queue";
    //商品下架队列名
    public static final String GOODS_DOWN_QUEUE = "goods_down_queue";


    //商品上架交换机
    public static final String GOODS_UP_EXCHANGE = "goods_up_exchange";
    //商品下架交换机
    public static final String GOODS_DOWN_EXCHANGE = "goods_down_exchange";

    //缓存预热队列
    @Bean("adUpdateQueue")
    public Queue adUpdateQueue(){
        return new Queue(AD_UPDATE_QUEUE);
    }

    //商品上架队列
    @Bean("goodsUpQueue")
    public Queue goodsUpQueue(){
        return new Queue(GOODS_UP_QUEUE);
    }

    //商品下架队列
    @Bean("goodsDownQueue")
    public Queue goodsDownQueue(){
        return new Queue(GOODS_DOWN_QUEUE);
    }


    //商品上架交换机
    @Bean("goodsUpExchange")
    public Exchange goodsUpExchange(){
        //使用广播模式交换机
        return ExchangeBuilder.fanoutExchange(GOODS_UP_EXCHANGE).durable(true).build();
    }

    //商品下架交换机
    @Bean("goodsDownExchange")
    public Exchange goodsDownExchange(){
        //使用广播模式交换机
        return ExchangeBuilder.fanoutExchange(GOODS_DOWN_EXCHANGE).durable(true).build();
    }



    //商品上架交换机绑定上架队列
    @Bean
    public Binding goodsUp(@Qualifier("goodsUpExchange")Exchange exchange,@Qualifier("goodsUpQueue")Queue queue){
        //广播模式不使用路由key
        return BindingBuilder.bind(queue).to(exchange).with("").noargs();
    }

    //商品下架交换机绑定下架队列
    @Bean
    public Binding goodsDown(@Qualifier("goodsDownExchange")Exchange exchange,@Qualifier("goodsDownQueue")Queue queue){
        //广播模式不使用路由key
        return BindingBuilder.bind(queue).to(exchange).with("").noargs();
    }


}
