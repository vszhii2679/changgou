package com.changgou.search.service;

import java.util.Map;

public interface Es5Service {
    /**
     * 导入全部sku
     */
    void importAll();

    /**
     * 删除全部sku
     */
    void deleteAll();
    /**
     * 创建索引
     */
    void createIndex();

    /**
     * 根据spuId导入skuList
     * @param spuId
     */
    void importBySpuId(String spuId);

    /**
     * 根据spuId删除skuList
     * @param spuId
     */
    void downBySpuId(String spuId);

    /**
     * 多条件查询elasticsearch 索引为skuinfo的文档
     * @param queryMap
     * @return
     */
    Map searchByQueryMap(Map<String,String> queryMap);
}
