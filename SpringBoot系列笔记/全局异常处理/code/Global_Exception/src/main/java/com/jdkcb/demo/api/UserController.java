package com.jdkcb.demo.api;

import com.jdkcb.demo.exception.MyException;
import com.jdkcb.demo.service.MyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class UserController {



    @GetMapping("/hello")
    public String HelloWord(){
        throw new MyException("1001", "根据ID查询用户异常");
    }


    @GetMapping("/test")
    public String test(){
        int num = 1/0;
        return "Hello Word";
    }


    @GetMapping("/test1")
    public String test1(){
        String name = null;
        if(name == null){
            throw new MyException("101","用户名为空");
        }
        return "Hello Word";
    }

}
