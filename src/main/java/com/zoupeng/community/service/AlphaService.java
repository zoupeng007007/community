package com.zoupeng.community.service;

import com.zoupeng.community.dao.AlphaDao;
import com.zoupeng.community.dao.DiscussPostMapper;
import com.zoupeng.community.dao.UserMapper;
import com.zoupeng.community.entity.DiscussPost;
import com.zoupeng.community.entity.User;
import com.zoupeng.community.util.CommunityUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Date;

@Slf4j
@Service //表示一个业务组件，装配进容器，默认是单例的
//@Scope("prototype")//可实例化多次
public class AlphaService {
    @Autowired //service调用dao层时，直接采用依赖注入的方式，更方便
    private AlphaDao alphaDao;

    public String find() {
        return alphaDao.select();
    }

    public AlphaService() {
        System.out.println("实例化方法");
    }

    @PostConstruct //在构造之后调用
    public void init() {
        System.out.println("初始化方法");
    }

    @PreDestroy  //在销毁之前调用
    public void destory() {
        System.out.println("销毁之前的方法");
    }


    @Autowired
    UserMapper userMapper;

    @Autowired
    DiscussPostMapper discussPostMapper;

    public String find1() {
        return alphaDao.select();
    }

    //REQUIRED :支持当前事务，如果不存在则创建新事务
    //REQUIRES_NEW：创建一个新的事务，并且暂停当前事务（外部事务）
    //NESTED：如果当前存在事务（外部事务），则嵌套在该事务中执行（独立的提交和回滚），否则和REQUIRED一样
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public Object save1() {
        //新增用户
        User user = new User();
        user.setUsername("alpha");
        user.setSalt(CommunityUtil.generateUUID());
        user.setPassword(CommunityUtil.md5("123" + user.getSalt()));
        user.setEmail("alpha@qq.com");
        user.setHeaderUrl("http://wap.baidu.com/static/img/r/image/2014-04-18/a250be0f0af4d9dd116b4bf1f37dc5c6.jpg");
        user.setCreateTime(new Date());
        userMapper.insertUser(user);

        //新增帖子
        DiscussPost post = new DiscussPost();
        post.setUserId(user.getId());
        post.setTitle("hello");
        post.setContent("新人报道");
        post.setCreateTime(new Date());
        discussPostMapper.insertDiscussPost(post);
        Integer.valueOf("ddasd");
        return "/ok";
    }

    @Autowired
    TransactionTemplate template;
    public Object save2() {
        template.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        //新增用户
        User user = new User();
        user.setUsername("alpha");
        user.setSalt(CommunityUtil.generateUUID());
        user.setPassword(CommunityUtil.md5("123" + user.getSalt()));
        user.setEmail("alpha@qq.com");
        user.setHeaderUrl("http://wap.baidu.com/static/img/r/image/2014-04-18/a250be0f0af4d9dd116b4bf1f37dc5c6.jpg");
        user.setCreateTime(new Date());
        userMapper.insertUser(user);

        //新增帖子
        DiscussPost post = new DiscussPost();
        post.setUserId(user.getId());
        post.setTitle("hello");
        post.setContent("新人报道");
        post.setCreateTime(new Date());
        discussPostMapper.insertDiscussPost(post);
        Integer.valueOf("ddasd");
        return template.execute(new TransactionCallback<Object>() {
            @Override
            public Object doInTransaction(TransactionStatus status) {

                //新增用户
                User user = new User();
                user.setUsername("alpha");
                user.setSalt(CommunityUtil.generateUUID());
                user.setPassword(CommunityUtil.md5("123" + user.getSalt()));
                user.setEmail("alpha@qq.com");
                user.setHeaderUrl("http://wap.baidu.com/static/img/r/image/2014-04-18/a250be0f0af4d9dd116b4bf1f37dc5c6.jpg");
                user.setCreateTime(new Date());
                userMapper.insertUser(user);

                //新增帖子
                DiscussPost post = new DiscussPost();
                post.setUserId(user.getId());
                post.setTitle("hello");
                post.setContent("新人报道");
                post.setCreateTime(new Date());
                discussPostMapper.insertDiscussPost(post);
                Integer.valueOf("ddasd");
                return "/ok";
            }
        });
    }

    //让该方法在多线程的环境下，被异步调用
    @Async
    public void execute1(){
        log.debug("execute1");
    }

//    @Scheduled(initialDelay = 10000,fixedRate = 1000)
    public void execute2(){
        log.debug("execute2");
    }

}
