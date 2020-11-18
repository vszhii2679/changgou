package com.changgou.web.controller;

import com.changgou.entity.Result;
import com.changgou.entity.StatusCode;
import com.changgou.goods.feign.SkuFeign;
import com.changgou.order.feign.CartFeign;
import com.changgou.order.feign.OrderFeign;
import com.changgou.order.pojo.Order;
import com.changgou.pay.feign.PayFeign;
import com.changgou.user.feign.AddressFeign;
import com.changgou.user.feign.UserFeign;
import com.changgou.user.pojo.Address;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@CrossOrigin
@RequestMapping("/worder")
public class OrderController {


    @Autowired
    private AddressFeign addressFeign;

    @Autowired
    private OrderFeign orderFeign;

    @Autowired
    private CartFeign cartFeign;

    @Autowired
    private SkuFeign skuFeign;


    //用户预订单数据展示页面
    @GetMapping("/ready/order")
    public String list(Model model) {
        //1、通过feign远程调用获得用户地址信息
        List<Address> addressList = addressFeign.list().getData();
        for (Address address : addressList) {
            if ("1".equals(address.getIsDefault())) {
                model.addAttribute("defaultAddr", address);
                break;
            }
        }
        //2、通过feign远程调用获得购物车信息
        Map map = (Map) cartFeign.list().getData();
        //3、将数据封装到model中
        model.addAttribute("addressList", addressList);
        model.addAttribute("items", map);
        return "order";
    }


    //用户根据预订单信息生成订单
    @PostMapping("/add")
    @ResponseBody
    public Result<Map> add(@RequestBody Order order) {
        /*
        业务：
            1、操作tb_order表，根据用户发送的订单信息生成订单基本信息，
            2、操作tb_order_item表，根据查询订单项集合OrderItemList，遍历集合对生成具体sku的订单项
            3、操作tb_sku表，根据订单项中的数量信息，对表中响应sku的num以及sale_num进行修改
         */
        Map map = (Map) orderFeign.add(order).getData();
        String orderId = (String) map.get("orderId");
        //model.addAttribute("payMoney",map.get("payMoney"));
        return new Result<>(true, StatusCode.OK, "下单成功", orderId);
    }

    @GetMapping("/pay")
    public String order2pay(@RequestParam("orderId") String orderId, Model model) {
        Order order = orderFeign.order2pay(orderId).getData();
        Integer payMoney = order.getPayMoney();
        model.addAttribute("orderId", orderId);
        model.addAttribute("payMoney", payMoney);
        return "pay";
    }



}
