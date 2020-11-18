package com.changgou.order.dao;

import com.changgou.order.pojo.Task;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.Date;
import java.util.List;

public interface TaskMapper extends Mapper<Task> {

    @Select("select * from tb_task where update_time<=#{date}")
    List<Task> findTaskBeforeCurrentTime(Date date);
}
