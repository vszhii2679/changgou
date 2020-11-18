package com.changgou;

import com.changgou.interceptor.FeignInterceptor;
import com.changgou.order.config.TokenDecode;
import com.changgou.utils.IdWorker;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import tk.mybatis.spring.annotation.MapperScan;

import java.time.LocalDate;

@SpringBootApplication
@EnableEurekaClient
@MapperScan(basePackages = {"com.changgou.order.dao"})
@EnableFeignClients(basePackages = {"com.changgou.goods.feign","com.changgou.pay.feign"})
@EnableScheduling
public class OrderApplication {
    public static void main(String[] args) {
        SpringApplication.run( OrderApplication.class);
    }

    @Bean
    public TokenDecode tokenDecode(){
        return new TokenDecode();
    }

    @Bean
    public IdWorker idWorker(){
        //机器id，信息中心id
        return new IdWorker(1,1);
    }

    //RequestContextHolder.getRequestAttributes()该方法是从ThreadLocal变量里面取得请求头信息
    @Bean
    public FeignInterceptor feignInterceptor(){
        return new FeignInterceptor();
    }
}
