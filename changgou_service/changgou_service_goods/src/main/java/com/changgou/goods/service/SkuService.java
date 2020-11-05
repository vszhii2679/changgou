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


}