package com.changgou.pay.service;

import java.util.Map;

public interface WxPayService {
    /**
     * 根据订单id和订单金额向微信发送请求获取支付二维码
     * @param orderId
     * @param payMoney
     * @return
     */
    Map nativePay(String orderId, Integer payMoney);

    /**
     * 根据微信返回的支付结果的商户订单id向微信查询具体结果信息
     * @param orderId
     * @return
     */
    Map queryOrder(String orderId);

    /**
     * 根据订单id关闭订单
     * @param orderId
     */
    Map closeOrder(String orderId);
}
