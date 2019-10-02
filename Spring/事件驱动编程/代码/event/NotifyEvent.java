package com.jdkcb.demo.event;

import org.springframework.context.ApplicationEvent;

public class NotifyEvent extends ApplicationEvent {

    private String msg;

    public NotifyEvent(Object source, String msg) {
        super(source);
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }

}

