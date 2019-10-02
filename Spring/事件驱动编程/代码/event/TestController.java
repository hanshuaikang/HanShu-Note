package com.jdkcb.demo.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class TestController {

    @Autowired
    private NotifyPublisher notifyPublisher;

    @GetMapping("/sayHello")
    public String sayHello(){
        notifyPublisher.publishEvent(1, "我发布了一个事件");
        return "Hello Word";

    }

}
