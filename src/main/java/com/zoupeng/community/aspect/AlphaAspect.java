package com.zoupeng.community.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

//@Component
//@Aspect
public class AlphaAspect {
    //切入点,第一个*代表任意返回值，空格接包名，后面的.*表示所有组件，后.*表示所有方法，（..）表示所有的参数
    @Pointcut("execution(* com.zoupeng.community.service.*.*(..))")
    public void pointcut(){

    }

    //表示以pointcut()方法作为连接点，在方法开始前
    @Before("pointcut()")
    public void before(){
        System.out.println("before");
    }

    //在方法结束后
    @After("pointcut()")
    public void after(){
        System.out.println("after");
    }

    //在返回之前
    @AfterReturning("pointcut()")
    public void afterReturning(){
        System.out.println("afterReturning");
    }

    //在抛异常之后
    @AfterThrowing("pointcut()")
    public void afterThrowing(){
        System.out.println("afterThrowing");
    }

    //可以在原始对象方法的前后进行处理
    @Around("pointcut()")
    public Object around(ProceedingJoinPoint joinPoint)throws Throwable{
        System.out.println("around before");
        //调用原始对象的方法
        Object obj = joinPoint.proceed();
        System.out.println("around after");
        return obj;
    }
}
