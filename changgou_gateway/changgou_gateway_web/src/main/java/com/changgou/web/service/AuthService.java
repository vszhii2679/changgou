package com.changgou.web.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

@Service
public class AuthService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    public String getJtiFromCookie(ServerHttpRequest request) {
        MultiValueMap<String, HttpCookie> cookies = request.getCookies();
        HttpCookie jti = cookies.getFirst("uid");
        if (jti != null) {
            return jti.getValue();
        }
        return null;
    }


    public String getTokenFromRedis(String jti) {
        String token = stringRedisTemplate.boundValueOps(jti).get();
        if (token!=null){
            return token;
        }
        return null;
    }
}
