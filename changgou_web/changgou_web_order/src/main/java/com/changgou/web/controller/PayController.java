package com.changgou.web.controller;

import com.changgou.entity.Result;
import com.changgou.entity.StatusCode;
import com.changgou.order.feign.OrderFeign;
import com.changgou.order.pojo.Order;
import com.changgou.pay.feign.PayFeign;
import com.changgou.utils.ConvertUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/pay")
public class PayController {

    @Autowired
    private OrderFeign orderFeign;

    @Autowired
    private PayFeign payFeign;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    //支付第一阶段：调用微信的api，传入订单号以及金额，获得二维码及相关数据
    @GetMapping("/toWxPay")
    public String toWxPay(@RequestParam("orderId") String orderId, Model model) {
        //1、通过远程调用order服务获得订单对象
        Order order = orderFeign.findById(orderId).getData();
        //2、因为支付服务是特别敏感的服务，尽量增加程序的健壮性
        //如果订单的支付状态时不是未支付的状态时，返回一个错误页面
        //当查询订单不存在时，返回一个错误页面
        if (order == null || !"0".equals(order.getOrderStatus())) {
            return "fail";
        }
        Integer payMoney = order.getPayMoney();
        Map map = (Map) payFeign.nativePay(orderId, payMoney).getData();
        //当远程调用无结果时，返回一个错误页面
        if (map == null) {
            return "fail";
        }
        Map payData = new HashMap();
        payData.put("orderId", orderId);
        payData.put("payMoney", payMoney);
        payData.put("code_url", (String) map.get("code_url"));
        model.addAllAttributes(payData);
        //跳转页面前，向rabbitmq中发送一条消息，用于限制交易时间，如果超过该时间，则交易关闭
        rabbitTemplate.convertAndSend("", "queue.ordercreate", orderId);
        return "wxpay";
    }

    //支付第二阶段：用户支付成功之后，微信会通过notify_url进行回调，发送一个支付结果的信息，商户需要发送回执并完成业务操作
    //此阶段不需要页面渲染，直接在回调的支付微服务中进行

    //支付第三阶段：浏览器通过webSocket接收到队列的支付成功的信息后，跳转页面
    @GetMapping("/toPaySuccess")
    public String toPaySuccess(@RequestParam("payMoney") Integer payMoney, Model model) {
        model.addAttribute("payMoney", payMoney);
        return "paysuccess";
    }
}
