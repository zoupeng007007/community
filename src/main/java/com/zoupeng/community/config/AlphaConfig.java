package com.zoupeng.community.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.text.SimpleDateFormat;

/**
 * 当需要装配第三方的Jar的组件时，不可能在上方添加@service @Component等注解，需要用到一个config类
 */
@Configuration(proxyBeanMethods = true)
public class AlphaConfig {
    @Bean //将返回值装载至容器,方法名就是Bean的名字
    public SimpleDateFormat simpleDateFormat(){
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }
}
