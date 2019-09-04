package com.jdkcb.mybatisstuday.controller;

import com.jdkcb.mybatisstuday.mapper.one.PrimaryUserMapper;
import com.jdkcb.mybatisstuday.mapper.two.SecondaryUserMapper;
import com.jdkcb.mybatisstuday.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class UserController {

    @Autowired
    private PrimaryUserMapper primaryUserMapper;
    @Autowired
    private SecondaryUserMapper secondaryUserMapper;
    @RequestMapping("primary")
    public Object primary(){
        List<User> list = primaryUserMapper.findAll();
        return list;
    }
    @RequestMapping("secondary")
    public Object secondary  (){
        List<User> list = secondaryUserMapper.findAll();
        return list;
    }


}
