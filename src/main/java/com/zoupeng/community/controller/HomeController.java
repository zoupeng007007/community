package com.zoupeng.community.controller;

import com.zoupeng.community.entity.DiscussPost;
import com.zoupeng.community.entity.Page;
import com.zoupeng.community.entity.User;
import com.zoupeng.community.service.DiscussPostService;
import com.zoupeng.community.service.LikeService;
import com.zoupeng.community.service.UserService;
import com.zoupeng.community.util.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController implements CommunityConstant {

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;
    @RequestMapping(path = "/",method = RequestMethod.GET)
    public String root(){
        return "forward:/index";
    }
    //返回一个页面不需要@ResponseBody
    @RequestMapping(path = "/index",method = RequestMethod.GET)
    /*获取请求后，会将url如  localhost:8080/index.html?current=6 ，将current自动赋给page中的current,调用的是set方法*/
    public String getIndexPage(Model model, Page page, @RequestParam(name = "orderMode",defaultValue = "0") int orderMode){
        //方法调用之前，SpringMVC会自动实例化Model，并将Page注入Model,在thymeleaf中可以直接访问Page
        page.setRows(discussPostService.findDiscussPostRows(0));
        page.setPath("/index?orderMode=" + orderMode);
        List<DiscussPost> discussPosts = discussPostService.findDiscussPosts(0, page.getOffset(), page.getLimit(),orderMode);
//        String requestURI = request.getRequestURI();
//        String current = request.getParameter("current");
        List<Map<String,Object>> postVoList = new ArrayList<>();
        if (discussPosts != null) {
            for (DiscussPost discussPost : discussPosts) {
                //postVo封装 post 及发布post对应的user
                Map<String,Object> postVo = new HashMap<>();
                postVo.put("post",discussPost);
                User user = userService.findUserById(discussPost.getUserId());
                postVo.put("user",user);
                long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST,discussPost.getId());
                postVo.put("likeCount",likeCount);
                postVoList.add(postVo);
            }
        }
        model.addAttribute("postVoList",postVoList);
        model.addAttribute("orderMode",orderMode);
        return "index";
    }

    //用于异常的处理
    @RequestMapping(path = "/error1",method = RequestMethod.GET)
    public String getErrorPage(){
        return "/error/500";
    }

    //拒绝访问时的提示页面
    @RequestMapping(path = "/denied", method = {RequestMethod.GET})
    public String getDeniedPage() {
        return "/error/404";
    }
}
