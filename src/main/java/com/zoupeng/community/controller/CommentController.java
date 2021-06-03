package com.zoupeng.community.controller;


import com.zoupeng.community.annotation.LoginRequired;
import com.zoupeng.community.entity.Comment;
import com.zoupeng.community.entity.DiscussPost;
import com.zoupeng.community.entity.Event;
import com.zoupeng.community.event.EventProducer;
import com.zoupeng.community.service.CommentService;
import com.zoupeng.community.service.DiscussPostService;
import com.zoupeng.community.util.CommunityConstant;
import com.zoupeng.community.util.HostHolder;
import com.zoupeng.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

@Controller
@RequestMapping("/comment")
public class CommentController implements CommunityConstant {

    @Autowired
    private CommentService commentService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private RedisTemplate redisTemplate;
    @RequestMapping(path = "/add/{discussPostId}",method = RequestMethod.POST)
    @LoginRequired
    //Comment的其他属性，form表单提交时就会赋值。有些属性可以写在<input type="hidden">中，这样就可以自动赋值到参数中的comment中
    public String addComment(@PathVariable("discussPostId") int discussPostId,
                             Comment comment){
        comment.setUserId(hostHolder.getUser().getId());
        comment.setStatus(0);
        comment.setCreateTime(new Date());
        commentService.addComment(comment);
        //触发评论事件
        Event event = new Event()
                .setTopic(TOPIC_COMMENT)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(comment.getEntityType())
                .setEntityId(comment.getEntityId())
                .setData("post_id",discussPostId);
        //如果是帖子，需要通知的是帖子的作者，需要用到discussPostService
        if (comment.getEntityType() == ENTITY_TYPE_POST){
            DiscussPost target = discussPostService.findDiscussPostById(comment.getEntityId());
            event.setEntityUserId(target.getUserId());

            //计算帖子分数
            String redisKey = RedisKeyUtil.getPostScoreKey();
            redisTemplate.opsForSet().add(redisKey,discussPostId);
        }else {//不是评论是回复,需要通知评论的目标target
            Comment target = commentService.findCommentById(comment.getEntityId());
            event.setEntityUserId(target.getUserId());
        }

        //消息发送给Kafka
        eventProducer.fireEvent(event);

        if (comment.getEntityType() == ENTITY_TYPE_POST){
            //触发发帖事件
            event = new Event()
                    .setTopic(TOPIC_PUBLISH)
                    .setUserId(comment.getUserId())
                    .setEntityId(discussPostId)
                    .setEntityType(ENTITY_TYPE_POST);
            eventProducer.fireEvent(event);
        }

        //跳回详情页面
//        String url = request.getRequestURL().toString();
        return "redirect:/discuss/detail/" + discussPostId;
//        return url;
    }
}
