package com.zoupeng.community;


import com.zoupeng.community.entity.DiscussPost;
import com.zoupeng.community.service.DiscussPostService;
import lombok.extern.slf4j.Slf4j;

import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
@Slf4j
public class SpringBootTests {


    @Autowired
    private DiscussPostService discussPostService;

    private DiscussPost data;

    @BeforeClass
    public static void beforeClass(){
        System.out.println("beforeClass");
    }

    @AfterClass
    public static void afterClass(){
        System.out.println("afterClass");
    }

    @Before
    public void before(){
        System.out.println("before");
        //初始化测试数据
        data = new DiscussPost();
        data.setUserId(111);
        data.setTitle("Test title");
        data.setContent("test content");
        data.setCreateTime(new Date());

        discussPostService.addDiscussPost(data);
    }

    @After
    public void after(){
        System.out.println("after");
        //删除数据
        discussPostService.updateStatus(data.getId(),2);
    }

    @Test
    public void test1(){

        System.out.println("test1");
    }

    @Test
    public void test2(){

        System.out.println("test2");
    }

    @Test
    public void testFindById(){
        DiscussPost post = discussPostService.findDiscussPostById(data.getId());

        //是否非空
        Assert.assertNotNull(post);

        //是否相等
        Assert.assertEquals(data.getTitle(),post.getTitle());
        Assert.assertEquals(data.getContent(),post.getContent());
    }

    @Test
    public void testUpdateScore(){
        int rows = discussPostService.updateScore(2000, data.getId());

        Assert.assertEquals(1,rows);

        DiscussPost post = discussPostService.findDiscussPostById(data.getId());
        Assert.assertEquals(2000.00,post.getScore(),2);

    }
}
