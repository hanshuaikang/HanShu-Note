package com.jdkcb.demo.config;

import com.jdkcb.demo.exception.CommonResult;
import com.jdkcb.demo.exception.MyException;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Configuration
public class ExceptionConfig {

    @RestControllerAdvice("com.jdkcb.demo.api")
    static class UnifiedExceptionHandler{
        @ExceptionHandler(MyException.class)
        public CommonResult<Void> handleBusinessException(MyException be){
            return CommonResult.errorResult(be.getErrorCode(), be.getErrorMsg());
        }
    }
}
