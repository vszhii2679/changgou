package com.changgou.canal.listener;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.changgou.canal.config.RabbitmqConfig;
import com.xpand.starter.canal.annotation.CanalEventListener;
import com.xpand.starter.canal.annotation.ListenPoint;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
/*
    canal事件监听类：用于首页广告redis及OpenResty缓存预热
    消息生产端：监听tb_ad数据表状态，生产发生变动的行position的值
 */
//@CanalEventListener:声明当前类是一个Canal的事件监听类
@CanalEventListener
public class BusinessListener {

    @Autowired
    private RabbitTemplate rabbitTemplate;
    /*
        当数据库n行发生变动一次提交时，此方法会被执行n次
     */
    //@ListenPoint 监听点->数据库及数据表
    @ListenPoint(schema = "changgou_business",table = "tb_ad")
    public void adUpdate(CanalEntry.EventType eventType,CanalEntry.RowData rowData){
        System.out.println("canal监听到数据预热信息");
        //获取数据库表变化前行信息
        List<CanalEntry.Column> beforeColumnsList = rowData.getBeforeColumnsList();
        //获取数据库表变化后行信息,List对应的是数据库中一行的信息
        List<CanalEntry.Column> afterColumnsList = rowData.getAfterColumnsList();
        //System.out.println("【更改前数据】:");
        //beforeColumnsList.forEach(c-> System.out.println(c.getName()+":"+ c.getValue()));
        //System.out.println("【更改后数据】:");
        //afterColumnsList.forEach(c-> System.out.println(c.getName()+":"+ c.getValue()));
        for (CanalEntry.Column column : afterColumnsList) {
                if("position".equals(column.getName())){
                    //System.out.println(column.getValue());
                    //向队列中发送被修改的position列的值
                    rabbitTemplate.convertAndSend("",RabbitmqConfig.AD_UPDATE_QUEUE,column.getValue());
                }
        }
    }
}
