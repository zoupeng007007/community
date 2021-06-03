package com.zoupeng.community.dao;

import com.zoupeng.community.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

//@Repository
@Mapper
@Component
public interface UserMapper {
    User selectById(int id);

    User selectByName(String name);

    User selectByEmail(String email);

    int insertUser(User user);

    int updateStatus(int id,int status);

    int updateHeader(int id, String headerUrl);

    int updatePassword(int id,String password);
}
