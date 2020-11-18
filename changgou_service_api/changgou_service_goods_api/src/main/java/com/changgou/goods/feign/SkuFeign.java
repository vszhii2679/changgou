package com.changgou.goods.feign;

import com.changgou.entity.Result;
import com.changgou.goods.pojo.Sku;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

//feign从注册中心中调用changgou-service-goods服务
@FeignClient(name = "changgou-service-goods")
public interface SkuFeign {

    //调用goods中skuController的getSkuListBySpuId方法
    @GetMapping("/sku/spu/getSkuListBySpuId/{spuId}")
    public List<Sku> getSkuListBySpuId(@PathVariable("spuId") String spuId);

    //调用goods中skuController的getById方法
    @GetMapping("/sku/{skuId}")
    public Result<Sku> getById(@PathVariable("skuId") String skuId);

    //调用goods中的库存以及销售量修改方法
    @PostMapping("/sku/decStock")
    public Result decStock(@RequestParam("username") String username);

    //调用goods中的sku
    @PostMapping("/sku/incrStock/{skuId}/{num}")
    public Result incrStock(@PathVariable("skuId") String skuId, @PathVariable("num") Integer num);
}
