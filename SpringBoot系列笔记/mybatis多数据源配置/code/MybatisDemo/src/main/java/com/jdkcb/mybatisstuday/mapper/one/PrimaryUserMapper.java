package com.jdkcb.mybatisstuday.mapper.one;

import com.jdkcb.mybatisstuday.pojo.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Mapper
public interface PrimaryUserMapper {

//    @Select("select * from sys_user;")
    List<User> findAll();
}
