package com.changgou.pay;

import com.github.wxpay.sdk.ChanggouConfig;
import com.github.wxpay.sdk.WXPay;
import org.checkerframework.checker.units.qual.C;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableEurekaClient
@EnableFeignClients(basePackages = {})
public class PayApplication {
    public static void main(String[] args) {
        SpringApplication.run(PayApplication.class, args);
    }

    //向ioc容器中注入微信核心支付核心类：WXpay
    @Bean
    public WXPay wxPay(){
        try {
            return new WXPay(new ChanggouConfig());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
