package com.changgou.goods.controller;

import com.changgou.entity.Result;
import com.changgou.entity.StatusCode;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/*
    全局异常处理类：
    使用fescar方案解决分布式事务时出现的异常
    不需要通过全局异常类进行处理，通过TC调用全局GlobalTransactional注解配置的分布式事务回滚事务
 */

@ResponseBody
@ControllerAdvice
public class GoodsExceptionAdvice {


    @ExceptionHandler(Exception.class)
    public Result doException(Exception exception) {
        exception.printStackTrace();
        return new Result(false, StatusCode.ERROR, exception.getMessage());
    }
}
