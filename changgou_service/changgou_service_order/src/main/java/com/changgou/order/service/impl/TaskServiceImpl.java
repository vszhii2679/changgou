package com.changgou.order.service.impl;

import com.changgou.order.dao.TaskHisMapper;
import com.changgou.order.dao.TaskMapper;
import com.changgou.order.pojo.Task;
import com.changgou.order.pojo.TaskHis;
import com.changgou.order.service.TaskService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class TaskServiceImpl implements TaskService {

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private TaskHisMapper taskHisMapper;

    @Override
    @Transactional
    public void completeTask(Task task) {
        //设置删除时间
        task.setDeleteTime(new Date());
        //bean拷贝，基于内省
        TaskHis taskHis = new TaskHis();
        BeanUtils.copyProperties(task,taskHis);
        //重置taskHit的id为null，表使用自增主键
        taskHis.setId(null);
        //在任务历史表中插入当前taskHit
        taskHisMapper.insertSelective(taskHis);
        //历史记录添加完成后，删除任务
        taskMapper.deleteByPrimaryKey(task.getId());
    }
}
