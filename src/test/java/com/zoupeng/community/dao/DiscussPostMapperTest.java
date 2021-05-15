package com.zoupeng.community.dao;

import com.zoupeng.community.CommunityApplication;
import com.zoupeng.community.entity.DiscussPost;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
class DiscussPostMapperTest {
    @Autowired
    private DiscussPostMapper discussPostMapper;
    @Test
    void selectDiscussPost() {
        List<DiscussPost> discussPosts = discussPostMapper.selectDiscussPost(104, 0, 10);
        for (DiscussPost discussPost:discussPosts){
            System.out.println(discussPost);
        }
    }

    @Test
    void selectDiscussPostRows() {
        int rows= discussPostMapper.selectDiscussPostRows(0);
        System.out.println(rows);
    }
}