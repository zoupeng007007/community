package com.zoupeng.community.dao;

import com.zoupeng.community.CommunityApplication;
import com.zoupeng.community.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;

import javax.annotation.Resource;
import java.util.Date;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
class UserMapperTest  {

    @Autowired
    private UserMapper userMapper;
    @Test
    void selectById() {
        User user = userMapper.selectById(102);
        System.out.println(user);
    }

    @Test
    void selectByName() {
        User user = userMapper.selectByName("zoupeng");
        System.out.println(user);

    }

    @Test
    void selectByEmail() {
        User user = userMapper.selectByEmail("zoupeng@997.com");
        System.out.println(user);
    }

    @Test
    void insertUser() {
        User user = new User();
        user.setUsername("z1das111");
        user.setPassword("d11dsa1");
        user.setSalt("dadas11s");
        user.setEmail("1sadasds");
        user.setHeaderUrl("ds1addasasdas");
        user.setCreateTime(new Date());
        int i = userMapper.insertUser(user);
        System.out.println(i);
        System.out.println(user.getId());
    }

    @Test
    void updateStatus() {
        int i = userMapper.updateStatus(105, 1);
        System.out.println(i);
    }

    @Test
    void updateHeader() {
        int i = userMapper.updateHeader(105, "kkk.cc.com");
        System.out.println(1);
    }

    @Test
    void updatePassword() {
        int ziyoebgsad = userMapper.updatePassword(105, "ziyoebgsad");
        System.out.println(ziyoebgsad);
    }
}