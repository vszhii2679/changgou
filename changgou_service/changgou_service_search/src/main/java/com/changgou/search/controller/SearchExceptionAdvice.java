package com.changgou.search.controller;

import com.changgou.entity.Result;
import com.changgou.entity.StatusCode;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/*
    全局异常处理类
 */

@ResponseBody
@ControllerAdvice
public class SearchExceptionAdvice {


    @ExceptionHandler(Exception.class)
    public Result doException(Exception exception) {
        exception.printStackTrace();
        return new Result(false, StatusCode.ERROR, exception.getMessage());
    }
}
