package com.jdkcb.demo.advice;


import com.jdkcb.demo.exception.MyException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class MyControllerAdvice {


    @ResponseBody
    @ExceptionHandler(value = Exception.class)
    public Map<String,Object> exceptionHandler(Exception ex){
        Map<String,Object> map  = new HashMap<String,Object>();
        map.put("code",100);
        map.put("msg",ex.getMessage());
        //发生异常进行日志记录，写入数据库或者其他处理，此处省略
        return map;
    }


    @ResponseBody
    @ExceptionHandler(value = MyException.class)
    public Map<String,Object> myExceptionHandler(MyException mex){
        Map<String,Object> map  = new HashMap<String,Object>();
        map.put("code",mex.getErrorCode());
        map.put("msg",mex.getErrorMsg());
        //发生异常进行日志记录，写入数据库或者其他处理，此处省略
        return map;
    }

}
