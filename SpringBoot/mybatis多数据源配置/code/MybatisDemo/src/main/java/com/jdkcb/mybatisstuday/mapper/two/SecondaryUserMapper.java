package com.jdkcb.mybatisstuday.mapper.two;

import com.jdkcb.mybatisstuday.pojo.User;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
@Mapper
public interface SecondaryUserMapper {

    //    @Select("select * from sys_user;")
    List<User> findAll();
}
