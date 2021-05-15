package com.zoupeng.community.controller.interceptor;

import com.zoupeng.community.entity.LoginTicket;
import com.zoupeng.community.entity.User;
import com.zoupeng.community.service.UserService;
import com.zoupeng.community.util.CookieUtil;
import com.zoupeng.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

@Component
public class LoginTicketInterceptor implements HandlerInterceptor {

    @Autowired
    UserService userService;

    @Autowired
    HostHolder holder;
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String ticket = CookieUtil.getValue(request, "ticket");
        if (ticket != null){//表示已登录
            //查询凭证
            LoginTicket loginTicket = userService.findLoginTicket(ticket);
            //检查凭证是否有效
            if (loginTicket != null && loginTicket.getStatus() ==0 && loginTicket.getExpired().after(new Date()) ){
                //根据凭证查询用户
                User user = userService.findUserById(loginTicket.getUserId());
                // 在本次请求中持有用户
                holder.setUser(user);
            }
        }
        return true;
    }

    //在controller之后，模板提交给前端处理器之前，将user存入module中
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        User user = holder.getUser();
        if (user !=null && modelAndView!=null){
            modelAndView.addObject("loginUser",user);
        }
    }

    //模板提交之后
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //模板提交之后，前端已经拿到了数据，将内存清理。
        holder.clear();
    }
}
