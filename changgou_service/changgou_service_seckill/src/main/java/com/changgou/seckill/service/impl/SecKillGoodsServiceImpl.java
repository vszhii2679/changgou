package com.changgou.seckill.service.impl;

import com.changgou.seckill.pojo.SeckillGoods;
import com.changgou.seckill.service.SecKillGoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SecKillGoodsServiceImpl implements SecKillGoodsService {


    public static final String SECKILL_GOODS_KEY = "seckill_goods_";

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public List<SeckillGoods> list(String time) {
        List<SeckillGoods> SeckillGoodsList = redisTemplate.boundHashOps(SECKILL_GOODS_KEY + time).values();
        return SeckillGoodsList;
    }
}
