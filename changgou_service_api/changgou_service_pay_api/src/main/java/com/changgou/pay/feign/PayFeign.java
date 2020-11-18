package com.changgou.pay.feign;

import com.changgou.entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "changgou-service-pay")
public interface PayFeign {

    @GetMapping("/wxpay/nativePay")
    public Result nativePay(@RequestParam("orderId") String orderId, @RequestParam("payMoney") Integer payMoney);

    @GetMapping("/wxpay/orderQuery/{orderId}")
    public Result orderQuery(@PathVariable("orderId") String orderId);

    @PutMapping("/wxpay/close/{orderId}")
    public Result closeOrder(@PathVariable("orderId") String orderId);
}
