package com.changgou.goods.service;

import com.changgou.goods.pojo.Goods;

public interface SpuService {
    /**
     * 添加商品：包括spu和sku
     * @param goods
     */
    void add(Goods goods);

    /**
     * 查询商品：
     * @return
     */
    Goods findById(String id);

    /**
     * 编辑商品
     * @param goods
     */
    void editById(Goods goods);

    /**
     * 根据id、状态码修改商品审核状态
     * @param id
     * @param status
     */
    void audit(String id,String status);


    /**
     * 根据id下架商品
     * @param id
     */
    void pull(String id);

    /**
     * 根据id上架商品
     * @param id
     */
    void push(String id);

    /**
     * 根据id修改商品的删除标记状态(商品进入逻辑"删除"状态)
     * @param id
     */
    void deleteById(String id);

    /**
     * 根据id修改商品的删除标记状态(商品去除逻辑"删除"标记)
     * @param id
     */
    void restoreById(String id);

    /**
     * 根据id从数据库中删除商品信息，包括spu和sku两部分
     * @param id
     */
    void realDel(String id);
}
