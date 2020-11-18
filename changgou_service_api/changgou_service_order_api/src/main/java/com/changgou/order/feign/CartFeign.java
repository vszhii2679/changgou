package com.changgou.order.feign;

import com.changgou.entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "changgou-service-order")
public interface CartFeign {

    @GetMapping("/cart/list")
    public Result list();

    @GetMapping("/cart/add/{skuId}/{number}")
    public Result add(@PathVariable("skuId") String skuId, @PathVariable("number") Integer number);

    @DeleteMapping("/cart/delete/{skuId}")
    public Result delete(@PathVariable("skuId") String skuId);
}
