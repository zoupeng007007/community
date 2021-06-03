package com.zoupeng.community.config;

import com.zoupeng.community.controller.interceptor.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

//采用配置类配置拦截器
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Autowired
    private AlphaInterceptor interceptor;
    @Autowired
    LoginTicketInterceptor loginTicketInterceptor;

    @Autowired
    UVDAUInterceptor uvdauInterceptor;
//    @Autowired
//    LoginRequiredInterceptor loginRequiredInterceptor;

    @Autowired
    MessageInterceptor messageInterceptor;
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //  /**/*.css" 排除所有css静态资源拦截，相当于localhost:8080/css/.*css
        registry.addInterceptor(interceptor)
                .excludePathPatterns("/**/*.css","/**/*.js","/**/*.jpg","/**/*.png","/**/*.jpg")
                .addPathPatterns("/register","/login");
        //所有的非静态页面都要进行处理，然后再去处理model,由于个人资料都在header中，且都是复用的index中的header，所以直接改header即可
        registry.addInterceptor(loginTicketInterceptor)
                .excludePathPatterns("/**/*.css","/**/*.js","/**/*.jpg","/**/*.png","/**/*.jpg");

//        registry.addInterceptor(loginRequiredInterceptor)
//                .excludePathPatterns("/**/*.css","/**/*.js","/**/*.jpg","/**/*.png","/**/*.jpg");
        registry.addInterceptor(messageInterceptor)
                .excludePathPatterns("/**/*.css","/**/*.js","/**/*.jpg","/**/*.png","/**/*.jpg");

        registry.addInterceptor(uvdauInterceptor)
                .excludePathPatterns("/**/*.css","/**/*.js","/**/*.jpg","/**/*.png","/**/*.jpg");
    }


}
