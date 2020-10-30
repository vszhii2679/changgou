package com.changgou.system.filter;

import com.changgou.system.util.JwtUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/*
    全局请求过滤器
 */
//可以通过在网关全局过滤器向redis中添加秘钥，在所有暴露接口的过滤器从redis中获取秘钥，从而实现网关为对外暴露唯一接口
@Component
public class AuthorizeFilter implements GlobalFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //1、获取请求和响应对象
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        //2、判断是否是登录请求
        boolean result = request.getURI().getPath().contains("/admin/login");
        if (result) {
            //放行登录请求
            return chain.filter(exchange);
        }
        //3、判断请求头中是否包含令牌，令牌是否有效
        HttpHeaders headers = request.getHeaders();
        String token = headers.getFirst("token");
        //3.1判断token是否为空或null
        if(StringUtils.isEmpty(token)){
            //为空返回未认证401
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }
        //3.2判断token是否有效
        try {
            JwtUtil.parseJWT(token);
        }catch (Exception e){
            //异常返回未认证401
            e.printStackTrace();
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }
        //未出现异常则放行
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
