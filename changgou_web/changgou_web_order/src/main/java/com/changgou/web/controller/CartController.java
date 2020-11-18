package com.changgou.web.controller;


import com.changgou.entity.Result;
import com.changgou.entity.StatusCode;
import com.changgou.order.feign.CartFeign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping("/wcart")
public class CartController {

    @Autowired
    private CartFeign cartFeign;

    @GetMapping("/list")
    public String cartList(Model model) {
        Map map = (Map) cartFeign.list().getData();
        model.addAttribute("items", map);
        return "cart";
    }

    @GetMapping("/add")
    @ResponseBody //方法为异步请求返回数据
    public Result<Map> add(@RequestParam("id") String skuId, @RequestParam("num") Integer number, Model model) {
        cartFeign.add(skuId, number);
        Map map = (Map) cartFeign.list().getData();
        return new Result<>(true, StatusCode.OK, "添加成功", map);
    }
}
