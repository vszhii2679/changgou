package com.changgou.order.service;


import java.util.Map;

public interface CartService {


    /**
     * 添加购物车
     * @param skuId sku id
     * @param number 商品数量
     * @param username 从cookie中获取username
     */
    void add(String skuId,Integer number ,String username);

    /**
     * 根据用户查询购物车信息
     * @param username
     * @return
     */
    Map list(String username);

    /**
     * 根据skuID删除用户购物车内商品
     * @param skuId
     * @param username
     */
    void delete(String skuId, String username);
}
