package com.changgou.seckill.task;

import com.changgou.seckill.dao.SecKillGoodsMapper;
import com.changgou.seckill.pojo.SeckillGoods;
import com.changgou.utils.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.entity.Example;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Component
public class SecKillGoodsPublish {

    public static final String SECKILL_GOODS_KEY = "seckill_goods_";

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private SecKillGoodsMapper secKillGoodsMapper;

    /**
     * 1、查询所有符合条件的秒杀商品
     *      1)获取时间菜单并遍历每一个时间段
     *      2)获取每一个时间段名称，用于为后续redis中的key赋值
     *      3)设置查询条件：商品处于已审核状态(status=1);商品库存>0;秒杀商品开始时间>=当前时段起点;秒杀商品结束时间<当前时段终点(起点+2)
     *      4)增加查询条件：排除已经加载到redis中缓存的商品，通过field设置商品的唯一id来排除
     *      5)执行查询获得已经加载到redis中的结果集
     * 2、将秒杀商品存入缓存
     */
    @Scheduled(cron = "0/30 * * * * ?")
    public void publishSecGoods2Redis() {
        /**
         * 使用redis的hash储存秒杀商品数据，秒杀商品的实体实现序列化接口
         * key--->时段字符串，例如2020111708，表示2020年11月17日08:00:00--2020年11月17日09:59:00时段
         * field--->秒杀商品的id
         * value--->秒杀商品的实例对象
         * 最终sql：SELECT * FROM tb_seckill_goods WHERE ( status = ? and stock_count > ? and start_time >= ? and end_time < ? )
         */

        //1、获取时间菜单：获取当前时段以及往后的4个时段，共5个时段
        List<Date> dateMenus = DateUtil.getDateMenus();
        for (Date dateMenu : dateMenus) {
            //SimpleDateFormat的format和parse方法都是线程不安全的方法，建议每次重新new，
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String redisTimeKey = DateUtil.date2Str(dateMenu);

            //3、构建查询对象，设置条件
            Example example = new Example(SeckillGoods.class);
            Example.Criteria criteria = example.createCriteria();
            criteria.andEqualTo("status", "1");//商品状态为已上架的状态
            criteria.andGreaterThan("stockCount", 0);//商品库存大于0
            criteria.andGreaterThanOrEqualTo("startTime", simpleDateFormat.format(dateMenu));//表中的startTime大于当前秒杀时段
            criteria.andLessThan("endTime", simpleDateFormat1.format(DateUtil.addDateHour(dateMenu, 2)));//表中的endTime小于当前秒杀结束时段
//最终sql：SELECT * FROM tb_seckill_goods WHERE ( status = ? and stock_count > ? and start_time >= ? and end_time < ? )
            //start_time >= ?代表数据库商品开始时间大于时间起点；end_time < ?代表数据库结束时间小于秒杀时段终点
            // 例如当前时段为10点-12点段：开始时间2019-08-14 11:00 大于 本日10点 且 结束时间2019-08-14 11:59小于12点即满足条件
            Set keys = redisTemplate.boundHashOps(SECKILL_GOODS_KEY + redisTimeKey).keys();
            if (keys != null && keys.size() > 0) {
                criteria.andNotIn("id", keys);//表中的id不在redis的当前时段hash的field中
            }
            //2、查询tb_seckill_goods表中所有满足条件的SecKillGoods对象
            List<SeckillGoods> seckillGoodsList = secKillGoodsMapper.selectByExample(example);
            //4、将查询结果储存到redis中
            if (seckillGoodsList != null) {
                for (SeckillGoods seckillGoods : seckillGoodsList) {
                    redisTemplate.boundHashOps(SECKILL_GOODS_KEY + redisTimeKey).put(seckillGoods.getId(),seckillGoods);
                }
            }
        }
    }
}
