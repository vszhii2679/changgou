package com.changgou.user.dao;

import com.changgou.user.pojo.PointLog;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import tk.mybatis.mapper.common.Mapper;

public interface PrintLogMapper extends Mapper<PointLog> {

    //添加用户积分
    @Update("update tb_user set points=points+#{point} where username=#{username}")
    int incrPoint(@Param("username") String username,@Param("point") int point);
}
