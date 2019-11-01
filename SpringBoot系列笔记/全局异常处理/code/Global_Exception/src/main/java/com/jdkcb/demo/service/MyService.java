package com.jdkcb.demo.service;

import org.springframework.stereotype.Service;

@Service
public class MyService {

    public String getUserName(){
        int num = 1/0;
        return "Helloword";
    }


}
