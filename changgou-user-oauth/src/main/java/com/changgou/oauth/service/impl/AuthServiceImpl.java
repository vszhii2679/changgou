package com.changgou.oauth.service.impl;

import com.changgou.oauth.service.AuthService;
import com.changgou.oauth.util.AuthToken;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private LoadBalancerClient loadBalancerClient;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Value("${auth.ttl}")
    private long ttl;

    @Override
    public AuthToken login(String username, String password, String clientId, String clientSecret) {
        //1、获得changgou-user-auth服务的实例
        ServiceInstance instance = loadBalancerClient.choose("changgou-user-auth");
        URI uri = instance.getUri();
        String url = uri + "/oauth/token";

        //3、封装请求参数
        //3.1、封装请求体参数：模式、 数据库账号、密码
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        //设置OAuth2为密码模式
        body.add("grant_type", "password");
        body.add("username", username);
        body.add("password", password);
        //3.2、封装请求头参数：Http basic base64加密的秘钥帐密的认证头
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("Authorization", getHttpBasic(clientId, clientSecret));
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);

        //设置友好设置，当出现400或者401错误时，不向前台返回错误
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
            @Override
            public void handleError(ClientHttpResponse response) throws IOException {
                if (response.getRawStatusCode() != 400 || response.getRawStatusCode() != 401) {
                    super.handleError(response);
                }
            }
        });
        //2、发送请求
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);
        Map map = response.getBody();
        if(map==null || map.get("access_token")==null || map.get("jti")==null || map.get("refresh_token")==null){
            throw new RuntimeException("申请令牌失败");
        }
        //封装OAuth2返回的token数据
        AuthToken authToken = new AuthToken();
        authToken.setAccessToken((String)map.get("access_token"));
        authToken.setJti((String)map.get("jti"));
        authToken.setRefreshToken((String)map.get("refresh_token"));
        //将token储存到redis中并设置有效期1h ，cookie在handler类中处理
        stringRedisTemplate.boundValueOps(authToken.getJti()).set(authToken.getAccessToken(),ttl, TimeUnit.SECONDS);
        return authToken;
    }

    @Override
    public void logout(String jti) {
        String value = stringRedisTemplate.boundValueOps(jti).get();
        if (StringUtils.isEmpty(value)){

        }
        //登出：删除redis中的jti以及登录数据即可
        stringRedisTemplate.delete(jti);

    }

    private String getHttpBasic(String clientId, String clientSecret) {
        String basic = clientId + ":" + clientSecret;
        byte[] encode = Base64.getEncoder().encode(basic.getBytes());
        return "Basic " + new String(encode);
    }
}
