package com.zoupeng.community.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;


//配置允许定时任务
@Configuration
@EnableScheduling
@EnableAsync
public class ThreadPoolConfig {

}
