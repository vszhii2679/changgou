package com.changgou.system.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Configuration
public class AuthorizeConfig {


    @Bean
    public KeyResolver ipKeyResolver() {
        //实现IP限流
        //return new KeyResolver() {
        //     @Override
        //     public Mono<String> resolve(ServerWebExchange exchange) {
        //         return  Mono.just(exchange.getRequest().getRemoteAddress().getHostName());
        //     }
        // };
        //每秒令牌桶中会生成1个令牌，桶容量为1，未拿到令牌的ip返回429页面 Too Many Requests
        return exchange -> Mono.just(exchange.getRequest().getRemoteAddress().getHostName());
    }
}
