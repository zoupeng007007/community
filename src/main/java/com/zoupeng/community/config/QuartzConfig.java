package com.zoupeng.community.config;


import com.zoupeng.community.quartz.AlphaJob;
import com.zoupeng.community.quartz.PostScoreRefreshJob;
import org.omg.CORBA.TRANSACTION_MODE;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;

//quartz配置
//配置 -》 初始化数据库（如果有相关配置，否则读取内存） -》调用一次，以后直接访问的是数据库
@Configuration
public class QuartzConfig {

    // 1.FactoryBean 可简化Bean的实例化过程：封装了Bean的实例化过程
    // 2.将FactoryBean装配到Spring容器中
    // 3.将FactoryBean注入给其他的Bean
    // 4. 该Bean得到的是FactoryBean所管理的对象实例

    //    @Bean
    public JobDetailFactoryBean alphaJobDetail() {

        JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
        factoryBean.setJobClass(AlphaJob.class);
        factoryBean.setName("alphaJob");
        factoryBean.setGroup("alphaJobGroup");
        factoryBean.setDurability(true);//是否长久保存
        factoryBean.setRequestsRecovery(true);//是否可恢复
        return factoryBean;
    }

    //配置Trigger (SimpleTriggerFactoryBean,CronTriggerFactoryBean)
//    @Bean
    public SimpleTriggerFactoryBean alphaTrigger(JobDetail alphaJobDetail) {//变量名和实际JobDetail对应
        SimpleTriggerFactoryBean factoryBean = new SimpleTriggerFactoryBean();
        factoryBean.setJobDetail(alphaJobDetail);
        factoryBean.setName("alphaTrigger");
        factoryBean.setGroup("alphaTriggerGroup");
        factoryBean.setRepeatInterval(3000);//执行频率
        factoryBean.setJobDataMap(new JobDataMap());//数据存储
        return factoryBean;
    }


    //刷新帖子分数任务
    @Bean
    public JobDetailFactoryBean postScoreRefreshJobDetail() {

        JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
        factoryBean.setJobClass(PostScoreRefreshJob.class);
        factoryBean.setName("postScoreRefreshJob");
        factoryBean.setGroup("communityJobGroup");
        factoryBean.setDurability(true);//是否长久保存
        factoryBean.setRequestsRecovery(true);//是否可恢复
        return factoryBean;
    }

    //配置Trigger
    @Bean
    public SimpleTriggerFactoryBean postScoreRefreshTrigger(JobDetail postScoreRefreshJobDetail) {//变量名和实际JobDetail对应
        SimpleTriggerFactoryBean factoryBean = new SimpleTriggerFactoryBean();
        factoryBean.setJobDetail(postScoreRefreshJobDetail);
        factoryBean.setName("postScoreRefreshTrigger");
        factoryBean.setGroup("communityTriggerGroup");
        factoryBean.setRepeatInterval(1000 * 60 * 5);//执行频率5min执行一次
        factoryBean.setJobDataMap(new JobDataMap());//数据存储
        return factoryBean;
    }
}
