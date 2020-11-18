package com.changgou.pay.service.impl;

import com.changgou.pay.service.WxPayService;
import com.github.wxpay.sdk.WXPay;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class WxPayServiceImpl implements WxPayService {

    @Autowired
    private WXPay wxPay;

    @Value("${wxpay.notify_url}")
    private String code_url;


    @Override
    public Map nativePay(String orderId, Integer payMoney) {
        try {
            //1、封装请求参数
            Map map = new HashMap();
            map.put("body","畅购商城微信付款");//商品描述
            map.put("out_trade_no",orderId);//订单号
            map.put("total_fee","1");//金额
            map.put("spbill_create_ip","127.0.0.1");//终端IP
            map.put("notify_url",code_url);//终端IP
            map.put("trade_type","NATIVE");//交易类型
            //结果
            Map resultMap = wxPay.unifiedOrder(map);
            return resultMap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Map queryOrder(String orderId) {
        try {
            Map map = new HashMap();
            map.put("out_trade_no",orderId);
            Map orderMap = wxPay.orderQuery(map);
            return orderMap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Map closeOrder(String orderId) {
        //
        try{
            Map map = new HashMap();
            map.put("out_trade_no",orderId);
            Map resultMap = wxPay.closeOrder(map);
            return resultMap;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
