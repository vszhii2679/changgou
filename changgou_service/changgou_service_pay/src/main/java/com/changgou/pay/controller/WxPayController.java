package com.changgou.pay.controller;

import com.alibaba.fastjson.JSON;
import com.changgou.entity.Result;
import com.changgou.entity.StatusCode;
import com.changgou.pay.config.RabbitmqConfig;
import com.changgou.pay.service.WxPayService;
import com.changgou.utils.ConvertUtils;
import com.github.wxpay.sdk.WXPayUtil;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

//微信支付请求的外部接口
@RestController
@RequestMapping("/wxpay")
public class WxPayController {


    @Autowired
    private WxPayService wxPayService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    //支付第一阶段：获取支付二维码数据
    @GetMapping("/nativePay")
    public Result nativePay(@RequestParam("orderId") String orderId, @RequestParam("payMoney") Integer payMoney) {
        //调用微信支付的api，发送支付请求，通过map支付相关参数，获得code_url等数据
        Map map = wxPayService.nativePay(orderId, payMoney);
        return new Result(true, StatusCode.OK, "微信支付二维码结果数据", map);
    }

    //支付第二阶段：在微信发送支付结果后，向微信发送一个反馈，并处理自身的业务代码
    @PostMapping("/notify")
    public void notify4PayResult(HttpServletRequest request, HttpServletResponse response) {
        try {
            System.out.println("【接收到微信发送的支付结果】:");
            String xml = ConvertUtils.convertToString(request.getInputStream());
            System.out.println(xml);
            //支付成功需要处理的业务：修改数据库订单的状态及相应数据
            //----业务区
            Map<String, String> resultMap = WXPayUtil.xmlToMap(xml);
            //当微信返回的result_code为SUCCESS时，通过微信wxPay.unifiedOrder方法发送请求获取支付结果信息进行确认
            if ("SUCCESS".equals(resultMap.get("result_code"))) {
                Map result = wxPayService.queryOrder(resultMap.get("out_trade_no"));
                System.out.println("查询订单返回结果：" + result);
                //根据最终确认结果进行判断，对返回状态码和交易状态码进行判断
                if ("SUCCESS".equals(result.get("result_code")) && "SUCCESS".equals(result.get("trade_state"))) {
                    //最终确认的结果result_code和trade_state都成功时
                    Map message = new HashMap<>();
                    message.put("out_trade_no", result.get("out_trade_no"));
                    message.put("transaction_id", result.get("transaction_id"));
                    //向rabbitmq的order_payCompleted_queue
                    rabbitTemplate.convertAndSend("", RabbitmqConfig.PAY_SUCCESS_QUEUE, JSON.toJSONString(message));
                    //以上程序都正常完成后，向rabbitmq的paynotify交换机发送支付成功的消息
                    rabbitTemplate.convertAndSend("paynotify", "", result.get("out_trade_no"));
                    //如果成功，给微信支付一个成功的回执
                    response.setContentType("text/xml");
                    String data = "<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml>";
                    response.getWriter().write(data);
                } else {
                    System.out.println(result.get("err_code_des"));//错误信息描述
                }
            } else {
                System.out.println(resultMap.get("err_code_des"));//错误信息描述
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @GetMapping("/orderQuery/{orderId}")
    public Result orderQuery(@PathVariable("orderId") String orderId) {
        Map map = wxPayService.queryOrder(orderId);
        return new Result(true, StatusCode.OK, "查询订单成功", map);
    }

    @PutMapping("/close/{orderId}")
    public Result closeOrder(@PathVariable("orderId") String orderId){
        Map map = wxPayService.closeOrder(orderId);
        return new Result(true,StatusCode.OK,"关闭订单成功",map);
    }

}
