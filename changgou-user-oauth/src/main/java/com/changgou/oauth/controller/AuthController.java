package com.changgou.oauth.controller;

import com.changgou.entity.Result;
import com.changgou.entity.StatusCode;
import com.changgou.oauth.service.AuthService;
import com.changgou.oauth.util.AuthToken;
import com.changgou.oauth.util.CookieUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
@RequestMapping("/oauth")
public class AuthController {

    public static final String LOGIN_URL="http://127.0.0.1:8001/api/oauth/toLogin";

    @Autowired
    private AuthService authService;

    @Value("${auth.clientId}")
    private String clientId;

    @Value("${auth.clientSecret}")
    private String clientSecret;

    @Value("${auth.cookieDomain}")
    private String cookieDomain;

    @Value("${auth.cookieMaxAge}")
    private int cookieMaxAge;

    @RequestMapping("/toLogin")
    public String toLogin(@RequestParam(name = "FROM",required = false,defaultValue = "") String fromUrl, Model model){
        model.addAttribute("FROM",fromUrl);
        return "login";
    }

    @GetMapping("/logout")
    public void logout(HttpServletRequest request,HttpServletResponse response) throws IOException {
        //1、删除本地jti：cookie
        String jti = deleteCookie4Jti("uid", request, response);
        //2、删除redis中的token
        authService.logout(jti);
        //2、返回数据给浏览器
        response.sendRedirect(LOGIN_URL);
    }

    @PostMapping("/login")
    @ResponseBody
    public Result login(String username, String password, HttpServletResponse response){
        try {
            //1、调用authService，登录生成令牌
            AuthToken token = authService.login(username, password, clientId, clientSecret);
            //2、在本地生成cookie
            saveJtiToCookie(token.getJti(),response);
            //3、返回数据给浏览器
            return new Result(true,StatusCode.OK,"登录成功");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("账号或者密码错误");
        }
    }





    //将令牌的断标识jti存入到cookie中
    private void saveJtiToCookie(String jti, HttpServletResponse response) {
        Cookie cookie = new Cookie("uid",jti);
        /*
        //设置cookie跨域共享的域名
        cookie.setDomain(cookieDomain);
        //设置cookie有效期
        cookie.setMaxAge(cookieMaxAge);
        //设置cookie的有效路径，所有应用共享cookie
        cookie.setPath("/");
        response.addCookie(cookie);
        //是否只能使用http请求
        cookie.setHttpOnly(false);
        */
        //使用工具类
        CookieUtil.addCookie(response,cookieDomain,"/","uid",jti,cookieMaxAge,false);
    }

    private String deleteCookie4Jti(String uid, HttpServletRequest request, HttpServletResponse response) {
        //从请求中获取cookie
        String jti = CookieUtil.readCookie(request, "uid").get("uid");
        //将cookie有效期置为零
        Cookie cookie = new Cookie("uid",jti);
        CookieUtil.addCookie(response,cookieDomain,"/","uid",jti,0,false);
        //返回cookie
        return jti;
    }

}
