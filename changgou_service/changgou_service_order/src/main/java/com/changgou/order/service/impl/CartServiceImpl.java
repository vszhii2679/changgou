package com.changgou.order.service.impl;

import com.changgou.goods.feign.SkuFeign;
import com.changgou.goods.feign.SpuFeign;
import com.changgou.goods.pojo.Sku;
import com.changgou.goods.pojo.Spu;
import com.changgou.order.pojo.OrderItem;
import com.changgou.order.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CartServiceImpl implements CartService {

    private static final String CART = "cart_";

    @Autowired
    private SkuFeign skuFeign;

    @Autowired
    private SpuFeign spuFeign;

    @Autowired
    private RedisTemplate redisTemplate;


    @Transactional
    @Override
    public void add(String skuId, Integer number, String username) {
        //1、首先从redis中查询是否含有此商品的信息
        OrderItem orderItem = (OrderItem) redisTemplate.boundHashOps(CART + username).get(skuId);
        Sku sku = skuFeign.getById(skuId).getData();
        //2.1、当redis中包含此信息时，更新orderItem中数据
        if (null != orderItem) {
            //更新数量
            orderItem.setNum(orderItem.getNum() + number);
            //如果数量小于等于0，则从redis中删除数据
            if (orderItem.getNum()<=0){
                redisTemplate.boundHashOps(CART + username).delete(skuId);
                return;
            }
            //更新总价
            orderItem.setMoney(orderItem.getNum() * orderItem.getPrice());
            //更新支付价格
            orderItem.setPayMoney(orderItem.getNum() * orderItem.getPrice());
            //更新总重量
            orderItem.setWeight(orderItem.getWeight() + sku.getWeight() * number);
        } else {
            //2.2、当redis中不包含此信息时，更新orderItem中数据
            //通过feign接口获取数据
            Spu spu = spuFeign.findSpuById(sku.getSpuId()).getData();
            //将信息封装至orderItem中
            orderItem = setOrderItem(sku, spu, number);
        }
        //3、将数据封装进redis中，'cart_'+username 作为唯一id，skuId作为field，number设置为value
        redisTemplate.boundHashOps(CART + username).put(skuId, orderItem);
    }

    @Transactional
    @Override
    public void delete(String skuId, String username) {
        redisTemplate.boundHashOps(CART+username).delete(skuId);
    }

    @Override
    public Map list(String username) {
        Map map = new HashMap<>();
        //1、从redis中获取购物车全部怇
        List<OrderItem> orderItemList = redisTemplate.boundHashOps(CART + username).values();
        //2、处理总价、数量
        Integer totalNum = 0;
        Integer totalPrice = 0;
        for (OrderItem orderItem : orderItemList) {
            //获得购物车商品总数量和总价格
            totalNum = orderItem.getNum() + totalNum;
            totalPrice = orderItem.getMoney() + totalPrice;
        }
        //3、将数据封装至map中
        map.put("totalNum",totalNum);
        map.put("totalPrice",totalPrice);
        map.put("orderItemList", orderItemList);
        map.put("username",username);
        return map;
    }

    private OrderItem setOrderItem(Sku sku, Spu spu, Integer number) {
        OrderItem orderItem = new OrderItem();
        orderItem.setSpuId(spu.getId());
        orderItem.setSkuId(sku.getId());
        orderItem.setPrice(sku.getPrice());
        orderItem.setName(sku.getName());
        orderItem.setNum(number);
        orderItem.setImage(sku.getImage());
        orderItem.setPayMoney(sku.getPrice() * number);
        orderItem.setMoney(sku.getPrice() * number);
        orderItem.setWeight(sku.getWeight() * number);
        orderItem.setCategoryId1(spu.getCategory1Id());
        orderItem.setCategoryId2(spu.getCategory2Id());
        orderItem.setCategoryId3(spu.getCategory3Id());
        return orderItem;
    }
}
