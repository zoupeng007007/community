package com.zoupeng.community;

import com.zoupeng.community.dao.AlphaDao;
import com.zoupeng.community.service.AlphaService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;

import java.text.SimpleDateFormat;
import java.util.Date;
@Slf4j
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
class CommunityApplicationTests implements ApplicationContextAware {//想获取容器需要实现ApplicationContextAware


    private ApplicationContext applicationContext;//用来记录容器

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;//记录

    }

    @Test
    void testSpringContext() {
        System.out.println(applicationContext);

        AlphaDao alphaDao = applicationContext.getBean(AlphaDao.class);
        System.out.println(alphaDao);
        AlphaDao alphaDao2 = applicationContext.getBean("aaaa", AlphaDao.class);
        System.out.println(alphaDao2);
        log.info("test");
        log.warn("test");
        log.error("test");
    }

    @Test
    public void testBeanManagement() {
        AlphaService bean = applicationContext.getBean(AlphaService.class);
        AlphaService bean1 = applicationContext.getBean(AlphaService.class);
        System.out.println(bean);
        System.out.println(bean1);
    }

    @Test
    public void testBeanConfig() {
        SimpleDateFormat simpleDateFormat = applicationContext.getBean(SimpleDateFormat.class);
        System.out.println(simpleDateFormat.format(new Date()));
    }

    @Autowired //自动注入
    private AlphaDao alphaDao;

    @Autowired
    private SimpleDateFormat simpleDateFormat;

    @Autowired
    @Qualifier("aaaa")//当有多个Alpha的实现类在容器中时，可通过@Qualifier筛选
    private AlphaDao alphaDao1;
    @Test
    public void testDI(){//测试依赖注入
        System.out.println(alphaDao);
        System.out.println(alphaDao1);
        System.out.println(simpleDateFormat);
    }
}
