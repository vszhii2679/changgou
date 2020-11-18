package com.changgou.goods.service;

import com.changgou.goods.pojo.Sku;

import java.util.List;
import java.util.Map;

public interface SkuService {
    /**
     * 根据spuId查询相应的sku集合
     * @param map
     * @return
     */
    List<Sku> getSkuListBySpuId(Map<String,Object> map);


    /**
     * 根据skuId查询实体
     * @param skuId
     * @return
     */
    Sku getSkuById(String skuId);

    /**
     * 根据用户名查询redis对数据库中的tb_sku中的商品数量以及商品销量进行修改
     * @param username
     * @return
     */
    void decStock(String username);


    /**
     * 当订单交易关闭时，根据skuId，回滚tb_sku表的商品数量和销售数量
     * @param skuId
     * @param num
     */
    void incrStock(String skuId,Integer num);
}