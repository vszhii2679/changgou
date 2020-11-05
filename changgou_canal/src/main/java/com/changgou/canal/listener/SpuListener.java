package com.changgou.canal.listener;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.changgou.canal.config.RabbitmqConfig;
import com.xpand.starter.canal.annotation.CanalEventListener;
import com.xpand.starter.canal.annotation.ListenPoint;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
    canal事件监听类：用于商品的上架和下架 传递spuId
    消息生产端，监听tb_spu数据表，发送发生变动的Id信息的消息生产端
 */
@CanalEventListener
public class SpuListener {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @ListenPoint(schema = "changgou_goods", table = "tb_spu")
    public void updateGoodsStatus(CanalEntry.EntryType entryType, CanalEntry.RowData rowData) {
        //1、获取数据库表某行数据变动前后的具体数据(信息)
        List<CanalEntry.Column> beforeColumnsList = rowData.getBeforeColumnsList();
        List<CanalEntry.Column> afterColumnsList = rowData.getAfterColumnsList();
        //2、将数据分别封装进两个map中，方便后续操作
        Map<String,String> beforeMap = new HashMap<>();
        Map<String,String> afterMap = new HashMap<>();
        beforeColumnsList.forEach(c-> beforeMap.put(c.getName(),c.getValue()));
        afterColumnsList.forEach(c->afterMap.put(c.getName(),c.getValue()));
        //3.1、对上架信息进行判断，检查is_marketable字段是否前后状态是否由0->1
        if("0".equals(beforeMap.get("is_marketable")) && "1".equals(afterMap.get("is_marketable"))){
            rabbitTemplate.convertAndSend(RabbitmqConfig.GOODS_UP_EXCHANGE,"",afterMap.get("id"));
        }

        //3.2、对上架信息进行判断，检查is_marketable字段是否前后状态是否由1->0
        if("1".equals(beforeMap.get("is_marketable")) && "0".equals(afterMap.get("is_marketable"))){
            rabbitTemplate.convertAndSend(RabbitmqConfig.GOODS_DOWN_EXCHANGE,"",afterMap.get("id"));
        }
    }
}
