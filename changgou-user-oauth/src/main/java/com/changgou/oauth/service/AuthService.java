package com.changgou.oauth.service;

import com.changgou.oauth.util.AuthToken;

public interface AuthService {
    /**
     * 密码模式登录
     * @param username 用户名
     * @param password 密码
     * @param clientId http Basic 加密的客户端id
     * @param clientSecret http Basic 加密的客户端密码
     * @return
     */
    AuthToken login(String username,String password,String clientId,String clientSecret);

    void logout(String jti);

}
