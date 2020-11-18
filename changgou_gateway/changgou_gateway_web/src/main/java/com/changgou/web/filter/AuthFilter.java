package com.changgou.web.filter;

import com.changgou.web.service.AuthService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;


@Component
public class AuthFilter implements GlobalFilter, Ordered {

    private static final String LOGIN_URL = "http://127.0.0.1:8001/api/oauth/toLogin";

    @Autowired
    private AuthService authService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        //1、判断是否是登录以及登录相关的请求，如果是，放行
        String path = request.getURI().getPath();
        if (path.contains("/api/oauth/login") || path.contains("/api/oauth/toLogin") || !UrlFilter.hasAuthorize(path)) {
            return chain.filter(exchange);
        }
        //2、非登录请求获取cookie
        String jti = authService.getJtiFromCookie(request);
        //如果cookie不存在，返回401信息
        if (StringUtils.isEmpty(jti)){
            //当cookie不存在时，跳转至首页,携带跳转前的路径信息
            return skip2LoginPage(exchange,LOGIN_URL+"?FROM="+path);
/*            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();*/
        }
        //3、如果redis中无token，返回401
        String token = authService.getTokenFromRedis(jti);
        if(StringUtils.isEmpty(token)){
            //当cookie不存在时，跳转至首页,携带跳转前的路径信息
            return skip2LoginPage(exchange,LOGIN_URL+"?FROM="+path);
/*            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();*/
        }
        //所有设置OAuth2的资源模块都需要http basic 生成的请求头才能访问，否则401
        //4、校验通过，将token储存至请求头中，增强请求
        request.mutate().header("Authorization","Bearer "+token);
        return chain.filter(exchange);
    }

    private Mono<Void> skip2LoginPage(ServerWebExchange exchange, String url) {
        ServerHttpResponse response = exchange.getResponse();
        //设置请求头Location以及状态码303，请求会重定向到Location的url地址
        response.setStatusCode(HttpStatus.SEE_OTHER);
        response.getHeaders().set("Location",url);
        return response.setComplete();
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
