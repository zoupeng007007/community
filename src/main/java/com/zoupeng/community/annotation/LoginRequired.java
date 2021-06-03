package com.zoupeng.community.annotation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//表示需要登录才能访问
@Deprecated
@Target(ElementType.METHOD)//表示可以标记在方法之上
@Retention(RetentionPolicy.RUNTIME)//表示程序运行时生效
public @interface LoginRequired {

}
