package com.zoupeng.community.controller.advice;

import com.zoupeng.community.util.CommunityUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

//ControllerAdvice全局配置,扫描所有嗲有Controller的组件
@ControllerAdvice(annotations = Controller.class)
@Slf4j
public class ExceptionAdvice {

    //当Controller发生异常时，会把异常 传过来
//    @ExceptionHandler({Exception.class})
    public void handlerException(Exception e, HttpServletRequest request, HttpServletResponse response) throws IOException {
        log.error("服务器发生异常"  + e.getMessage());
        //获取异常的异常栈，一条信息就是一个Element
        for (StackTraceElement element:e.getStackTrace()){
            log.error(element.toString());
        }
//        Map<String,Object> map = new HashMap<>();
        //判断是普通请求还是异步请求
        String xRequestedWith = request.getHeader("x-requested-with");
        //只有异步请求才是XML
        if ("XMLHttpRequest".equals(xRequestedWith)){
//            //响应一个json
            response.setContentType("application/plain;charset=utf-8");
            PrintWriter writer = response.getWriter();
            writer.write(CommunityUtil.getJSONString(1,"你还没有登录"));
        } else {
            response.sendRedirect(request.getContextPath() + "/error");
        }

    }
}
