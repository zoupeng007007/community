package com.zoupeng.community;


import com.zoupeng.community.entity.DiscussPost;
import com.zoupeng.community.service.DiscussPostService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.Date;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class CaffeineTests {

    @Autowired
    private DiscussPostService service;

    @Test
    public void initDataForTest() {
        for (int i = 0; i < 300000; i++) {
            DiscussPost post = new DiscussPost();
            post.setUserId(111);
            post.setTitle("压力测试------二级缓存");
            post.setContent("热帖采用本地缓存 + DB 两级缓存，当用户访问某一页数据的时，先访问本地缓存，若没有相关的信息则再访问DB并且将数据缓存到本地。");
            post.setCreateTime(new Date());
            post.setScore(Math.random() * 2000);
            service.addDiscussPost(post);
        }

    }
    @Test
    public void testCache(){
        System.out.println(service.findDiscussPosts(0,0,10,1));
        System.out.println(service.findDiscussPosts(0,0,10,1));
        System.out.println(service.findDiscussPosts(0,0,10,0));
    }
}
