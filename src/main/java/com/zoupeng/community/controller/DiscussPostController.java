package com.zoupeng.community.controller;

import com.zoupeng.community.entity.*;
import com.zoupeng.community.event.EventProducer;
import com.zoupeng.community.service.CommentService;
import com.zoupeng.community.service.DiscussPostService;
import com.zoupeng.community.service.LikeService;
import com.zoupeng.community.service.UserService;
import com.zoupeng.community.util.CommunityConstant;
import com.zoupeng.community.util.CommunityUtil;
import com.zoupeng.community.util.HostHolder;
import com.zoupeng.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping("/discuss")
public class DiscussPostController implements CommunityConstant {

    @Autowired
    DiscussPostService service;

    @Autowired
    UserService userService;
    @Autowired
    HostHolder holder;

    @Autowired
    EventProducer eventProducer;

    @Autowired
    RedisTemplate redisTemplate;
    @RequestMapping(path = "/add",method = RequestMethod.POST)
    @ResponseBody
    public Map<String,Object> addDiscussPost(String title,String content){
        User user = holder.getUser();
        Map<String,Object> map = new HashMap<>();
        if (user == null){
            map.put("code",403);
            map.put("msg","你还没有登录");
            return map;
        }
        DiscussPost post = new DiscussPost();
        post.setUserId(user.getId());
        post.setTitle(title);
        post.setContent(content);
        post.setCreateTime(new Date());
        service.addDiscussPost(post);

        //触发发帖事件
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(user.getId())
                .setEntityId(post.getId())
                .setEntityType(ENTITY_TYPE_POST);
        eventProducer.fireEvent(event);

        //计算帖子分数
        String redisKey = RedisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(redisKey,post.getId());

        //报错的情况，将来进行处理
        map.put("code",0);
        map.put("msg","发布成功");
        return map;
    }

    @Autowired
    CommentService commentService;


    @Autowired
    LikeService likeService;
    @RequestMapping(path = "/detail/{discussPostId}",method = RequestMethod.GET)
    public String getDiscussPost(@PathVariable("discussPostId") int discussPostId,
                                 Model model,
                                 Page page){
        //查询帖子
        DiscussPost post = service.findDiscussPostById(discussPostId);
        model.addAttribute("post",post);
        //作者
        User user = userService.findUserById(post.getUserId());
        model.addAttribute("user",user);
        //赞
        long postLikeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST,post.getId());
        model.addAttribute("likeCount",postLikeCount);
        model.addAttribute("likeStatus",
                holder.getUser() == null?0:likeService.findEntityLikeStatus(holder.getUser().getId(),ENTITY_TYPE_POST,post.getId()));

        //评论的分页信息
        page.setLimit(5);
        page.setPath("/discuss/detail/" + discussPostId);
        page.setRows(post.getCommentCount());
        //一个帖子对应多个评论
        List<Comment> commentList = commentService.findCommentsByEntity(
                ENTITY_TYPE_POST, post.getId(), page.getOffset(), page.getLimit());
        //遍历该帖子的所有评论，将评论和对应的User封装到Map中
        //ListVo是评论 + 对应的User的集合
        List<Map<String,Object>> commentVoList = new ArrayList<>();
        if (commentList != null){
            for (Comment comment:commentList){
                Map<String,Object> commentVo = new HashMap<>();
                commentVo.put("comment",comment);
                //评论对应的用户
                //由于评论对应的target就是评论本身，所有不用取targetId
                commentVo.put("user",userService.findUserById(comment.getUserId()));
                //评论的赞
                long commentLikeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT,comment.getId());
                commentVo.put("likeCount",commentLikeCount);
                commentVo.put("likeStatus",
                        holder.getUser() == null?0:likeService.findEntityLikeStatus(holder.getUser().getId(),ENTITY_TYPE_COMMENT,comment.getId()));
                //评论中也有评论，作用在评论上，下面称为回复
                //回复列表,reply
                List<Comment> replyList = commentService.findCommentsByEntity(
                        ENTITY_TYPE_COMMENT, comment.getId(), 0, Integer.MAX_VALUE);
                //回复的Vo列表
                List<Map<String,Object>> replyVoList = new ArrayList<>();
                if (replyList != null){
                    for (Comment reply: replyList){
                        Map<String,Object> replyVo = new HashMap<>();
                        replyVo.put("reply",reply);
                        replyVo.put("user",userService.findUserById(reply.getUserId()));
                        //回复目标,回复的目标是用户
                        User target = reply.getTargetId() == 0 ? null : userService.findUserById(reply.getTargetId());
                        replyVo.put("target",target);
                        //回复的赞
                        long replyLikeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT,reply.getId());
                        replyVo.put("likeCount",replyLikeCount);
                        replyVo.put("likeStatus",
                                holder.getUser() == null?0:likeService.findEntityLikeStatus(holder.getUser().getId(),ENTITY_TYPE_COMMENT,reply.getId()));
                        replyVoList.add(replyVo);
                    }
                }
                //回复的数量
                int replyCount = commentService.findCommentCount(ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("replyCount",replyCount);
                commentVo.put("replys",replyVoList);
                commentVoList.add(commentVo);
            }
            model.addAttribute("comments",commentVoList);

        }
        return "/site/discuss-detail";
    }


    //置顶
    @RequestMapping(path = "/top",method = RequestMethod.POST)
    @ResponseBody
    public String setTop(int id){
        service.updateType(id,1);

        //更新ElasticSearch
        //触发发帖事件
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(holder.getUser().getId())
                .setEntityId(id)
                .setEntityType(ENTITY_TYPE_POST);
        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(0);
    }

    //加精
    @RequestMapping(path = "/wonderful",method = RequestMethod.POST)
    @ResponseBody
    public String setWonderful(int id){
        service.updateStatus(id,1);

        //更新ElasticSearch
        //触发发帖事件
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(holder.getUser().getId())
                .setEntityId(id)
                .setEntityType(ENTITY_TYPE_POST);
        eventProducer.fireEvent(event);

        //计算帖子分数
        String redisKey = RedisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(redisKey,id);

        return CommunityUtil.getJSONString(0);
    }

    //删除
    @RequestMapping(path = "/delete",method = RequestMethod.POST)
    @ResponseBody
    public String setDelete(int id){
        service.updateStatus(id,2);

        //更新ElasticSearch
        //触发发帖事件
        Event event = new Event()
                .setTopic(TOPIC_DELETE)
                .setUserId(holder.getUser().getId())
                .setEntityId(id)
                .setEntityType(ENTITY_TYPE_POST);
        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(0);
    }

}
