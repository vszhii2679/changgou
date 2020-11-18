package com.changgou.order.controller;

import com.changgou.entity.Result;
import com.changgou.entity.StatusCode;
import com.changgou.order.config.TokenDecode;
import com.changgou.order.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private TokenDecode tokenDecode;

    @GetMapping("/add/{skuId}/{number}")
    public Result add(@PathVariable("skuId") String skuId, @PathVariable("number") Integer number) {
        //String username ="itheima";
        //通过security上下文对象解析用户名,设置用户名数据在user-oauth模块CustomUserAuthenticationConverter中
        String username = tokenDecode.getUserInfo().get("username");
        cartService.add(skuId, number, username);
        return new Result(true, StatusCode.OK, "添加购物车成功");
    }

    @GetMapping("/list")
    public Result list() {
        //String username ="itheima";
        //通过security上下文对象解析用户名,设置用户名数据在user-oauth模块CustomUserAuthenticationConverter中
        String username = tokenDecode.getUserInfo().get("username");
        Map map = cartService.list(username);
        return new Result(true, StatusCode.OK, "查询购物车成功", map);
    }

    @DeleteMapping("/delete/{skuId}")
    public Result delete(@PathVariable("skuId") String skuId) {
        //String username ="itheima";
        //通过security上下文对象解析用户名,设置用户名数据在user-oauth模块CustomUserAuthenticationConverter中
        String username = tokenDecode.getUserInfo().get("username");
        cartService.delete(skuId,username);
        return new Result(true, StatusCode.OK, "删除商品成功");
    }
}
