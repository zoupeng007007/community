package com.zoupeng.community.controller;

import com.zoupeng.community.entity.DiscussPost;
import com.zoupeng.community.entity.Page;
import com.zoupeng.community.entity.User;
import com.zoupeng.community.service.DiscussPostService;
import com.zoupeng.community.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController {

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserService userService;
    //返回一个页面不需要@ResponseBody
    @RequestMapping(path = "/index",method = RequestMethod.GET)
    /*获取请求后，会将url如  localhost:8080/index.html?current=6 ，将current自动赋给page中的current,调用的是set方法*/
    public String getIndexPage(Model model, Page page){
        //方法调用之前，SpringMVC会自动实例化Model，并将Page注入Model,在thymeleaf中可以直接访问Page
        page.setRows(discussPostService.findDiscussPostRows(0));
        page.setPath("/index");
        List<DiscussPost> discussPosts = discussPostService.findDiscussPosts(0, page.getOffset(), page.getLimit());
//        String requestURI = request.getRequestURI();
//        String current = request.getParameter("current");
        List<Map<String,Object>> list = new ArrayList<>();
        if (discussPosts != null) {
            for (DiscussPost discussPost : discussPosts) {
                Map<String,Object> map = new HashMap<>();
                map.put("post",discussPost);
                User user = userService.findUserById(discussPost.getUserId());
                map.put("user",user);
                list.add(map);
            }
        }
        model.addAttribute("discussPosts",list);
        return "index";
    }
}
