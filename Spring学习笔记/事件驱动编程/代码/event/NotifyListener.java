package com.jdkcb.demo.event;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class NotifyListener {


    @Async
    @EventListener
    public void sayHello(NotifyEvent notifyEvent){
        System.out.println("收到事件:"+notifyEvent.getMsg());
    }


}
