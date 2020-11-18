package com.changgou.order.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fescar.spring.annotation.GlobalTransactional;
import com.changgou.goods.feign.SkuFeign;
import com.changgou.order.config.RabbitmqConfig;
import com.changgou.order.dao.*;
import com.changgou.order.pojo.*;
import com.changgou.order.service.CartService;
import com.changgou.order.service.OrderService;
import com.changgou.pay.feign.PayFeign;
import com.changgou.utils.IdWorker;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderServiceImpl implements OrderService {

    private static final String CART = "cart_";

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private SkuFeign skuFeign;

    @Autowired
    private CartService cartService;

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private OrderLogMapper orderLogMapper;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private PayFeign payFeign;

    @Autowired
    private OrderConfigMapper orderConfigMapper;

    /**
     * 查询全部列表
     *
     * @return
     */
    @Override
    public List<Order> findAll() {
        return orderMapper.selectAll();
    }

    /**
     * 根据ID查询
     *
     * @param id
     * @return
     */
    @Override
    public Order findById(String id) {
        return orderMapper.selectByPrimaryKey(id);
    }


    /**
     * 操作tb_order表以及tb_order_item表，生成一条订单基本信息，和多条购物项信息
     *
     * @param order
     */
    @Override
    @GlobalTransactional(name = "order_add")//通过fescar注解开启分布式事务管理
    @Transactional
    public Map add(Order order) {
        //1、封装订单基本信息：前端封装了地址、收件人、电话三项信息
        String username = order.getUsername();
        //2、根据用户名调用cartService服务从redis中获取封装好的map数据
        Map map = cartService.list(username);
        //3、封装order的数据
        Integer totalNum = (Integer) map.get("totalNum");
        Integer totalMoney = (Integer) map.get("totalPrice");
        order.setTotalNum(totalNum);//商品总数量
        order.setTotalMoney(totalMoney);//商品总价格
        order.setPayMoney(totalMoney);//商品总支付价格
        order.setCreateTime(new Date());//订单创建时间
        order.setUpdateTime(new Date());//订单最后一次修改时间
        order.setBuyerRate("0");//评价状态：0未评价；1已评价
        order.setSourceType("1");//订单来源：1WEB端
        order.setOrderStatus("0");//订单状态：0未完成；1已完成；2已退货
        order.setPayStatus("0");//支付状态：0未支付；1已支付；2支付失败
        order.setConsignStatus("0");//收货状态：0未发货；1已发货；2已收货
        String orderId = idWorker.nextId() + "";
        order.setId(orderId);
        //4、向tb_order表中存入order对象
        orderMapper.insertSelective(order);
        //5、向tb_order_item表中通过遍历集合存入多个OrderItem对象
        List<OrderItem> orderItemList = (List<OrderItem>) map.get("orderItemList");
        for (OrderItem orderItem : orderItemList) {
            orderItem.setId(idWorker.nextId() + "");
            orderItem.setOrderId(orderId);
            orderItem.setIsReturn("0");//是否退货
            orderItemMapper.insertSelective(orderItem);
        }
        //采用异步队列通知的方式，在订单生成后完成对积分数据的分布式事务管理
        //在订单生成后，创建一个任务，并通过定时任务向rabbitmq发送task任务信息(包括username、)
        Task task = new Task();
        task.setCreateTime(new Date());
        task.setUpdateTime(new Date());
        //task.setRequestBody();
        task.setMqExchange(RabbitmqConfig.EXCHANGE_POINT_TASK);
        task.setMqRoutingkey(RabbitmqConfig.KEY_POINT_UPDATE);
        Map requestBody = new HashMap();
        requestBody.put("username", username);
        requestBody.put("orderId", orderId);
        requestBody.put("point", order.getPayMoney() / 10);
        task.setRequestBody(JSON.toJSONString(requestBody));
        taskMapper.insertSelective(task);
        //6、远程调用skuFeign的descStock方法
        skuFeign.decStock(username);
        //7、向rabbitmq中发送一条订单id，用于限制交易时间，如果超过该时间，则交易关闭
        //rabbitTemplate.convertAndSend("","queue.ordercreate",orderId);
        //8、根据用户名清空redis中的数据,测试阶段暂不删除//TODO
        redisTemplate.delete(CART + username);
        Map resultMap = new HashMap();
        resultMap.put("orderId", orderId);
        resultMap.put("payMoney", totalMoney);
        return resultMap;
    }


    /**
     * 修改
     *
     * @param order
     */
    @Override
    public void update(Order order) {
        orderMapper.updateByPrimaryKey(order);
    }

    /**
     * 删除
     *
     * @param id
     */
    @Override
    public void delete(String id) {
        orderMapper.deleteByPrimaryKey(id);
    }


    /**
     * 条件查询
     *
     * @param searchMap
     * @return
     */
    @Override
    public List<Order> findList(Map<String, Object> searchMap) {
        Example example = createExample(searchMap);
        return orderMapper.selectByExample(example);
    }

    /**
     * 分页查询
     *
     * @param page
     * @param size
     * @return
     */
    @Override
    public Page<Order> findPage(int page, int size) {
        PageHelper.startPage(page, size);
        return (Page<Order>) orderMapper.selectAll();
    }

    /**
     * 条件+分页查询
     *
     * @param searchMap 查询条件
     * @param page      页码
     * @param size      页大小
     * @return 分页结果
     */
    @Override
    public Page<Order> findPage(Map<String, Object> searchMap, int page, int size) {
        PageHelper.startPage(page, size);
        Example example = createExample(searchMap);
        return (Page<Order>) orderMapper.selectByExample(example);
    }

    @Override
    @Transactional
    public void updatePayStatus(String orderId, String transactionId) {
        Order order = orderMapper.selectByPrimaryKey(orderId);
        order.setPayStatus("1");//设置支付状态
        order.setOrderStatus("1");//设置订单状态
        order.setUpdateTime(new Date());//设置订单的最后修改时间
        order.setPayTime(new Date());//设置支付时间
        order.setTransactionId(transactionId);//微信返回的交易流水号
        //将修改后的订单更新到数据库
        orderMapper.updateByPrimaryKeySelective(order);
        //订单变动日志
        OrderLog orderLog = new OrderLog();
        orderLog.setId(idWorker.nextId() + "");
        orderLog.setOperater("system");// 系统
        orderLog.setOperateTime(new Date());//当前日期
        orderLog.setOrderStatus("1");
        orderLog.setPayStatus("1");
        orderLog.setRemarks("支付流水号" + transactionId);
        orderLog.setOrderId(order.getId());
        //往数据库中插入订单日志信息
        orderLogMapper.insertSelective(orderLog);
    }

    @Override
    @GlobalTransactional
    public void orderClose(String orderId) {
        //订单出现异常时，消息会拒收并重回队列，保证消息最终一定被消费

        //1、根据订单id查询订单
        Order order = orderMapper.selectByPrimaryKey(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        //2、如果订单的交易状态为非0(未付款)状态，抛出异常
        if (!"0".equals(order.getOrderStatus())) {
            throw new RuntimeException("当前订单不处于未付款状态，不能关闭");
        }
        //3、根据微信支付api的交易查询方法获取交易结果信息进行判断
        Map wxPayResultMap = (Map) payFeign.orderQuery(orderId).getData();
        //3.1、判断交易状态是否成功
        if ("SUCCESS".equals(wxPayResultMap.get("trade_state"))) {
            //该订单已经成功,更新订单支付状态并记录订单日志，通过已经定义的updatePayStatus方法实现
            updatePayStatus(orderId, (String) wxPayResultMap.get("transaction_id"));
            return;
        }
        if ("NOTPAY".equals(wxPayResultMap.get("trade_state"))) {
            System.out.println("【执行关闭订单操作】");
            //通过feign接口远程调用微信支付API的关闭交易的方法
            payFeign.closeOrder(orderId);
            //该订单为未支付的状态，关闭交易
            order.setCloseTime(new Date());//设置关闭事件
            order.setOrderStatus("4");//设置订单状态为已关闭
            //更新订单状态
            orderMapper.updateByPrimaryKeySelective(order);
            //插入订单关闭的日志
            OrderLog orderLog = new OrderLog();
            orderLog.setId(idWorker.nextId() + "");//设置订单日志id
            orderLog.setOperater("system");//设置操作人
            orderLog.setOperateTime(new Date());//设置操作事件
            orderLog.setOrderStatus("4");//设置订单状态
            orderLog.setOrderId(orderId);//设置订单id
            orderLogMapper.insertSelective(orderLog);
            //查询所有的购物项信息，用于还原库存和销量信息
            List<OrderItem> orderList = orderItemMapper.findOrderListByOrderId(orderId);
            for (OrderItem orderItem : orderList) {
                //远程调用skuFeign接口，用于还原库存和销量信息
                skuFeign.incrStock(orderItem.getSkuId(), orderItem.getNum());
            }
        }
    }

    @Override
    @Transactional
    //批量修改订单的状态为已发货状态
    public void bulkSend(List<Order> orderList) {
        //遍历三次是为了让所有数据都满足条件后再进行持久化操作
        for (Order order : orderList) {
            //遍历每一个订单，对前端发送的订单及订单状态进行判断
            if (order == null || order.getId() == null) {
                throw new RuntimeException("订单不存在或订单id不存在");
            }
            //对订单的物流信息进行判断
            if (order.getShippingCode() == null || order.getShippingName() == null) {
                throw new RuntimeException("请选择快递公司和填写快递单号");
            }
        }
        for (Order order : orderList) {
            //根据前台发送的id查询订单表，判断数据是否存在以及数据是否有效
            Order orderResult = orderMapper.selectByPrimaryKey(order.getId());
            if (orderResult == null) {
                throw new RuntimeException("订单不存在");
            }
            //判断订单状态是否是已支付或未发货状态
            if (!"0".equals(orderResult.getConsignStatus()) || !"1".equals(orderResult.getOrderStatus())) {
                throw new RuntimeException("订单状态有误！");
            }
        }
        //第三次遍历，修改数据库中的数据
        for (Order order : orderList) {
            //更新数据库中order状态
            order.setOrderStatus("2");//修改订单状态为已发货状态
            order.setConsignStatus("1");//修改发货状态为已发送状态
            order.setConsignTime(new Date());//设置发货时间
            order.setUpdateTime(new Date());//修改更新时间
            orderMapper.updateByPrimaryKeySelective(order);
            //插入orderLog数据
            OrderLog orderLog = new OrderLog();
            orderLog.setId(idWorker.nextId() + "");
            orderLog.setOrderId(order.getId());
            orderLog.setConsignStatus("1");//设置发货状态为已发货
            orderLog.setOperateTime(new Date());//设置操作事件
            orderLog.setOperater("admin");//设置操作人
            orderLog.setOrderStatus("2"); //设置订单状态已发货状态
            orderLogMapper.insertSelective(orderLog);
        }
    }

    @Override
    @Transactional
    public void confirmTake(String orderId, String operator) {
        Order order = orderMapper.selectByPrimaryKey(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (!"1".equals(order.getConsignStatus())) {
            throw new RuntimeException("订单未发货");
        }
        //修改order的状态信息
        order.setConsignStatus("2");//设置发货状态为已送达
        order.setOrderStatus("3");//设置订单状态为已完成
        order.setConsignTime(new Date());//设置送达时间
        order.setEndTime(new Date());//设置交易结束时间
        orderMapper.updateByPrimaryKeySelective(order);
        //生成订单日志信息
        OrderLog orderLog = new OrderLog();
        orderLog.setId(idWorker.nextId() + "");
        orderLog.setOperateTime(new Date());//设置操作时间
        orderLog.setOperater(operator);//设置操作人
        orderLog.setOrderId(orderId);
        orderLog.setOrderStatus("3");
        orderLogMapper.insertSelective(orderLog);
    }

    @Override
    @Transactional
    public void autoConfirmOrder() {
        //从数据库中获取自动确认收货的时长
        OrderConfig orderConfig = orderConfigMapper.selectByPrimaryKey(1);
        if (orderConfig == null) {
            throw new RuntimeException("OrderC配置为1的配置不存在");
        }
        //获取当前日期
        LocalDate localDate = LocalDate.now();
        //根据当前日期往前推自动收货的时间，正数负数左移右移
        LocalDate date = localDate.plusDays(-orderConfig.getTakeTimeout());
        Example example = new Example(Order.class);
        Example.Criteria criteria = example.createCriteria();
        //设置条件：签收时间小于当前日期的十五天前 则自动签收(签收时间过15天小于当前日期)
        criteria.andLessThan("consignTime",date);
        //设置条件：设置订单状态是否是已完成
        criteria.andEqualTo( "orderStatus","2" );
        List<Order> orderList = orderMapper.selectByExample(example);
        for (Order order : orderList) {
            //查询满足条件的订单信息，调用签收方法，传入订单id和签收人(系统签收)
            System.out.println("过期订单："+order.getId()+" "+order.getConsignStatus());
            confirmTake(order.getId(),"system" );
        }
    }

    /**
     * 构建查询对象
     *
     * @param searchMap
     * @return
     */
    private Example createExample(Map<String, Object> searchMap) {
        Example example = new Example(Order.class);
        Example.Criteria criteria = example.createCriteria();
        if (searchMap != null) {
            // 订单id
            if (searchMap.get("id") != null && !"".equals(searchMap.get("id"))) {
                criteria.andEqualTo("id", searchMap.get("id"));
            }
            // 支付类型，1、在线支付、0 货到付款
            if (searchMap.get("payType") != null && !"".equals(searchMap.get("payType"))) {
                criteria.andEqualTo("payType", searchMap.get("payType"));
            }
            // 物流名称
            if (searchMap.get("shippingName") != null && !"".equals(searchMap.get("shippingName"))) {
                criteria.andLike("shippingName", "%" + searchMap.get("shippingName") + "%");
            }
            // 物流单号
            if (searchMap.get("shippingCode") != null && !"".equals(searchMap.get("shippingCode"))) {
                criteria.andLike("shippingCode", "%" + searchMap.get("shippingCode") + "%");
            }
            // 用户名称
            if (searchMap.get("username") != null && !"".equals(searchMap.get("username"))) {
                criteria.andLike("username", "%" + searchMap.get("username") + "%");
            }
            // 买家留言
            if (searchMap.get("buyerMessage") != null && !"".equals(searchMap.get("buyerMessage"))) {
                criteria.andLike("buyerMessage", "%" + searchMap.get("buyerMessage") + "%");
            }
            // 是否评价
            if (searchMap.get("buyerRate") != null && !"".equals(searchMap.get("buyerRate"))) {
                criteria.andLike("buyerRate", "%" + searchMap.get("buyerRate") + "%");
            }
            // 收货人
            if (searchMap.get("receiverContact") != null && !"".equals(searchMap.get("receiverContact"))) {
                criteria.andLike("receiverContact", "%" + searchMap.get("receiverContact") + "%");
            }
            // 收货人手机
            if (searchMap.get("receiverMobile") != null && !"".equals(searchMap.get("receiverMobile"))) {
                criteria.andLike("receiverMobile", "%" + searchMap.get("receiverMobile") + "%");
            }
            // 收货人地址
            if (searchMap.get("receiverAddress") != null && !"".equals(searchMap.get("receiverAddress"))) {
                criteria.andLike("receiverAddress", "%" + searchMap.get("receiverAddress") + "%");
            }
            // 订单来源：1:web，2：app，3：微信公众号，4：微信小程序  5 H5手机页面
            if (searchMap.get("sourceType") != null && !"".equals(searchMap.get("sourceType"))) {
                criteria.andEqualTo("sourceType", searchMap.get("sourceType"));
            }
            // 交易流水号
            if (searchMap.get("transactionId") != null && !"".equals(searchMap.get("transactionId"))) {
                criteria.andLike("transactionId", "%" + searchMap.get("transactionId") + "%");
            }
            // 订单状态
            if (searchMap.get("orderStatus") != null && !"".equals(searchMap.get("orderStatus"))) {
                criteria.andEqualTo("orderStatus", searchMap.get("orderStatus"));
            }
            // 支付状态
            if (searchMap.get("payStatus") != null && !"".equals(searchMap.get("payStatus"))) {
                criteria.andEqualTo("payStatus", searchMap.get("payStatus"));
            }
            // 发货状态
            if (searchMap.get("consignStatus") != null && !"".equals(searchMap.get("consignStatus"))) {
                criteria.andEqualTo("consignStatus", searchMap.get("consignStatus"));
            }
            // 是否删除
            if (searchMap.get("isDelete") != null && !"".equals(searchMap.get("isDelete"))) {
                criteria.andEqualTo("isDelete", searchMap.get("isDelete"));
            }

            // 数量合计
            if (searchMap.get("totalNum") != null) {
                criteria.andEqualTo("totalNum", searchMap.get("totalNum"));
            }
            // 金额合计
            if (searchMap.get("totalMoney") != null) {
                criteria.andEqualTo("totalMoney", searchMap.get("totalMoney"));
            }
            // 优惠金额
            if (searchMap.get("preMoney") != null) {
                criteria.andEqualTo("preMoney", searchMap.get("preMoney"));
            }
            // 邮费
            if (searchMap.get("postFee") != null) {
                criteria.andEqualTo("postFee", searchMap.get("postFee"));
            }
            // 实付金额
            if (searchMap.get("payMoney") != null) {
                criteria.andEqualTo("payMoney", searchMap.get("payMoney"));
            }

        }
        return example;
    }

}
