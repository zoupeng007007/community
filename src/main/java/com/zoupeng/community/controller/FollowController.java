package com.zoupeng.community.controller;

import com.zoupeng.community.annotation.LoginRequired;
import com.zoupeng.community.entity.Event;
import com.zoupeng.community.entity.Page;
import com.zoupeng.community.entity.User;
import com.zoupeng.community.event.EventProducer;
import com.zoupeng.community.service.FollowService;
import com.zoupeng.community.service.UserService;
import com.zoupeng.community.util.CommunityConstant;
import com.zoupeng.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

//处理关注取关请求
@Controller
public class FollowController implements CommunityConstant{

    @Autowired
    FollowService followService;

    @Autowired
    HostHolder hostHolder;

    @Autowired
    private EventProducer eventProducer;

    @RequestMapping(path = "/follow", method = RequestMethod.POST)
    @ResponseBody
    @LoginRequired
    public Map<String, Object> follow(int entityType, int entityId) {
        User user = hostHolder.getUser();
        Map<String, Object> map = new HashMap<>();
        followService.follow(user.getId(), entityType, entityId);
        map.put("code", 0);
        map.put("msg", "已关注");
        //触发关注事件
        Event event = new Event()
                .setTopic(TOPIC_FOLLOW)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(entityType)
                .setEntityId(entityId)
                .setEntityUserId(entityId);
        eventProducer.fireEvent(event);
        return map;
    }

    @RequestMapping(path = "/unFollow", method = RequestMethod.POST)
    @ResponseBody
    @LoginRequired
    public Map<String, Object> unFollow(int entityType, int entityId) {
        User user = hostHolder.getUser();
        Map<String, Object> map = new HashMap<>();
        followService.unFollow(user.getId(), entityType, entityId);
        map.put("code", 0);
        map.put("msg", "已取消关注");
        return map;
    }

    @Autowired
    UserService userService;

    @RequestMapping(path = "/followees/{userId}", method = RequestMethod.GET)
    public String getFollowees(@PathVariable("userId") int userId, Page page, Model model) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        model.addAttribute("user", user);
        page.setLimit(5);
        page.setPath("/followees/" + userId);
        page.setRows((int) followService.findFolloweeCount(userId, CommunityConstant.ENTITY_TYPE_USER));
        List<Map<String, Object>> userList = followService.findFollowees(userId, page.getOffset(), page.getLimit());
        //当前用户是否关注的该用户
        if (userList != null) {
            for (Map<String, Object> map : userList) {
                User u = (User) map.get("user");
                map.put("hasFollowed", hasFollowed(u.getId()));
            }
        }
        model.addAttribute("users", userList);

        return "/site/followee";
    }

    @RequestMapping(path = "/followers/{userId}", method = RequestMethod.GET)
    public String getFollowers(@PathVariable("userId") int userId, Page page, Model model) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        model.addAttribute("user", user);
        page.setLimit(5);
        page.setPath("/followers/" + userId);
        page.setRows((int) followService.findFollowerCount(CommunityConstant.ENTITY_TYPE_USER, userId));
        List<Map<String, Object>> userList = followService.findFollowers(userId, page.getOffset(), page.getLimit());
        //当前用户是否关注的该用户
        if (userList != null) {
            for (Map<String, Object> map : userList) {
                User u = (User) map.get("user");
                map.put("hasFollowed", hasFollowed(u.getId()));
            }
        }
        model.addAttribute("users", userList);

        return "/site/follower";
    }

    private Boolean hasFollowed(int userId) {
        //没登陆就是没关注
        if (hostHolder.getUser() == null) {
            return false;
        }
        return followService.hasFollowed(hostHolder.getUser().getId(), CommunityConstant.ENTITY_TYPE_USER, userId);
    }
}
