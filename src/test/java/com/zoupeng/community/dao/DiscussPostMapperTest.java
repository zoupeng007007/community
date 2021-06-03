package com.zoupeng.community.dao;

import com.zoupeng.community.CommunityApplication;
import com.zoupeng.community.entity.DiscussPost;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.Date;
import java.util.List;


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

    @Test
    void  insertDiscussPost(){
        DiscussPost discussPost = new DiscussPost();
        discussPost.setCommentCount(1);
        discussPost.setContent("test");
        discussPost.setCreateTime(new Date());
        discussPost.setScore(10);
        discussPost.setTitle("test");
        discussPost.setStatus(1);
        discussPost.setUserId(1);
        int rows = discussPostMapper.insertDiscussPost(discussPost);
        System.out.println(rows);
    }
}