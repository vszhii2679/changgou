package com.changgou.oauth.controller;

import com.changgou.entity.Result;
import com.changgou.entity.StatusCode;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
@ResponseBody
public class OauthExceptionAdvice {

    @ExceptionHandler(value = {RuntimeException.class})
    public Result dealException(Exception e){
        return new Result(false, StatusCode.ERROR,e.getMessage());
    }
}
