package com.changgou.order.feign;

import com.changgou.entity.Result;
import com.changgou.order.pojo.Order;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "changgou-service-order")
public interface OrderFeign {

    @PostMapping("/order")
    public Result add(@RequestBody Order order);

    @GetMapping("/order/pay")
    Result<Order> order2pay(@RequestParam("orderId") String orderId);

    @GetMapping("/order/{id}")
    public Result<Order> findById(@PathVariable("id") String id);

}
