package com.zoupeng.community.service;

import com.zoupeng.community.dao.AlphaDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Slf4j
@Service //表示一个业务组件，装配进容器，默认是单例的
//@Scope("prototype")//可实例化多次
public class AlphaService {
    @Autowired //service调用dao层时，直接采用依赖注入的方式，更方便
    private AlphaDao alphaDao;
    public String find(){
        return alphaDao.select();
    }
    public AlphaService(){
        System.out.println("实例化方法");
    }
    @PostConstruct //在构造之后调用
    public void init(){
        System.out.println("初始化方法");
    }

    @PreDestroy  //在销毁之前调用
    public void destory(){
        System.out.println("销毁之前的方法");
    }
}
