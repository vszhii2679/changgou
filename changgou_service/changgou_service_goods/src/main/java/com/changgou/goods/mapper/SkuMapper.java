package com.changgou.goods.mapper;

import com.changgou.goods.pojo.Sku;
import com.changgou.order.pojo.OrderItem;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import tk.mybatis.mapper.common.Mapper;

public interface SkuMapper extends Mapper<Sku> {

    @Update("update tb_sku set num=num-#{num},sale_num=sale_num+#{num} where id=#{skuId} and num>=#{num}")
    int decStock(OrderItem orderItem);

    @Update("update tb_sku set num=num+#{num},sale_num=sale_num-#{num} where id=#{skuId}")
    void incrStock(@Param("skuId") String skuId, @Param("num") Integer num);
}
