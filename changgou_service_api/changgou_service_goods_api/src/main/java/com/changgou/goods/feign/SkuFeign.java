package com.changgou.goods.feign;

import com.changgou.goods.pojo.Sku;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

//feign从注册中心中调用changgou-service-goods服务
@FeignClient(name = "changgou-service-goods")
public interface SkuFeign {

    //调用goods中skuController的getSkuListBySpuId方法
    @GetMapping("/sku/spu/getSkuListBySpuId/{spuId}")
    public List<Sku> getSkuListBySpuId(@PathVariable("spuId") String spuId);
}
