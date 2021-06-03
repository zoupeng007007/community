package com.zoupeng.community.controller.interceptor;

import com.zoupeng.community.entity.User;
import com.zoupeng.community.service.UVDAUService;
import com.zoupeng.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class UVDAUInterceptor implements HandlerInterceptor {

    @Autowired
    private UVDAUService uvdauService;

    @Autowired
    private HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //统计UV
        String ip = request.getRemoteHost();
        uvdauService.recordUV(ip);

        //统计DAU
        User user = hostHolder.getUser();
        if (user != null){
            uvdauService.recordDAU(user.getId());
        }
        return true;
    }
}
