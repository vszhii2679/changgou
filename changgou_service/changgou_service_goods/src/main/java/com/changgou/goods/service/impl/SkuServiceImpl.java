package com.changgou.goods.service.impl;

import com.changgou.goods.mapper.SkuMapper;
import com.changgou.goods.pojo.Sku;
import com.changgou.goods.service.SkuService;
import com.changgou.order.pojo.OrderItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

@Service
public class SkuServiceImpl implements SkuService {

    private static final String CART = "cart_";

    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private RedisTemplate redisTemplate;


    @Override
    public List<Sku> getSkuListBySpuId(Map<String, Object> map) {
        Example example = getExample(map);
        return skuMapper.selectByExample(example);
    }

    @Override
    public Sku getSkuById(String skuId) {
        Sku sku = skuMapper.selectByPrimaryKey(skuId);
        return sku;
    }

    @Override
    @Transactional
    public void decStock(String username) {
        List<OrderItem> orderItems = redisTemplate.boundHashOps(CART + username).values();
        for (OrderItem orderItem : orderItems) {
            int count = skuMapper.decStock(orderItem);
            if (count <= 0) {
                throw new RuntimeException("商品库存不足");
            }
        }
    }

    @Override
    public void incrStock(String skuId, Integer num) {
        skuMapper.incrStock(skuId, num);
    }

    //条件对象
    private Example getExample(Map searchMap) {
        Example example = new Example(Sku.class);
        Example.Criteria criteria = example.createCriteria();
        if (searchMap != null) {
            // 商品id
            if (searchMap.get("id") != null && !"".equals(searchMap.get("id"))) {
                criteria.andEqualTo("id", searchMap.get("id"));
            }
            // 商品条码
            if (searchMap.get("sn") != null && !"".equals(searchMap.get("sn"))) {
                criteria.andEqualTo("sn", searchMap.get("sn"));
            }
            // SKU名称
            if (searchMap.get("name") != null && !"".equals(searchMap.get("name"))) {
                criteria.andLike("name", "%" + searchMap.get("name") + "%");
            }
            // 商品图片
            if (searchMap.get("image") != null && !"".equals(searchMap.get("image"))) {
                criteria.andLike("image", "%" + searchMap.get("image") + "%");
            }
            // 商品图片列表
            if (searchMap.get("images") != null && !"".equals(searchMap.get("images"))) {
                criteria.andLike("images", "%" + searchMap.get("images") + "%");
            }
            // SPUID
            if (searchMap.get("spuId") != null && !"".equals(searchMap.get("spuId"))) {
                criteria.andEqualTo("spuId", searchMap.get("spuId"));
            }
            // 类目名称
            if (searchMap.get("categoryName") != null && !"".equals(searchMap.get("categoryName"))) {
                criteria.andLike("categoryName", "%" + searchMap.get("categoryName") + "%");
            }
            // 品牌名称
            if (searchMap.get("brandName") != null && !"".equals(searchMap.get("brandName"))) {
                criteria.andLike("brandName", "%" + searchMap.get("brandName") + "%");
            }
            // 规格
            if (searchMap.get("spec") != null && !"".equals(searchMap.get("spec"))) {
                criteria.andLike("spec", "%" + searchMap.get("spec") + "%");
            }
            // 商品状态 1-正常，2-下架，3-删除
            if (searchMap.get("status") != null && !"".equals(searchMap.get("status"))) {
                criteria.andEqualTo("status", searchMap.get("status"));
            }

            // 价格（分）
            if (searchMap.get("price") != null) {
                criteria.andEqualTo("price", searchMap.get("price"));
            }
            // 库存数量
            if (searchMap.get("num") != null) {
                criteria.andEqualTo("num", searchMap.get("num"));
            }
            // 库存预警数量
            if (searchMap.get("alertNum") != null) {
                criteria.andEqualTo("alertNum", searchMap.get("alertNum"));
            }
            // 重量（克）
            if (searchMap.get("weight") != null) {
                criteria.andEqualTo("weight", searchMap.get("weight"));
            }
            // 类目ID
            if (searchMap.get("categoryId") != null) {
                criteria.andEqualTo("categoryId", searchMap.get("categoryId"));
            }
            // 销量
            if (searchMap.get("saleNum") != null) {
                criteria.andEqualTo("saleNum", searchMap.get("saleNum"));
            }
            // 评论数
            if (searchMap.get("commentNum") != null) {
                criteria.andEqualTo("commentNum", searchMap.get("commentNum"));
            }
        }
        return example;
    }
}
