package com.changgou.order.service;

import com.changgou.order.pojo.Order;
import com.github.pagehelper.Page;

import java.util.List;
import java.util.Map;

public interface OrderService {

    /***
     * 查询所有
     * @return
     */
    List<Order> findAll();

    /**
     * 根据ID查询
     * @param id
     * @return
     */
    Order findById(String id);

    /***
     * 新增
     * @param order
     */
    Map add(Order order);

    /***
     * 修改
     * @param order
     */
    void update(Order order);

    /***
     * 删除
     * @param id
     */
    void delete(String id);

    /***
     * 多条件搜索
     * @param searchMap
     * @return
     */
    List<Order> findList(Map<String, Object> searchMap);

    /***
     * 分页查询
     * @param page
     * @param size
     * @return
     */
    Page<Order> findPage(int page, int size);

    /***
     * 多条件分页查询
     * @param searchMap
     * @param page
     * @param size
     * @return
     */
    Page<Order> findPage(Map<String, Object> searchMap, int page, int size);

    /**
     * 根据orderId和微信返回的交易流水号修改订单
     * @param orderId
     * @param transactionId
     */
    void updatePayStatus(String orderId, String transactionId);

    /**
     * 根据orderId关闭微信交易订单并还原库存以及销量数据
     * @param orderId
     */
    void orderClose(String orderId);

    /**
     * 批量发货：修改发货状态、生成订单日志信息
     * @param orderList
     */
    void bulkSend(List<Order> orderList);

    /**
     * 手动确认收货
     * @param orderId
     */
    void confirmTake(String orderId, String operator);

    /**
     * 自动签收
     */
    void autoConfirmOrder();


}
