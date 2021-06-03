package com.zoupeng.community.controller;

import com.zoupeng.community.annotation.LoginRequired;
import com.zoupeng.community.entity.Event;
import com.zoupeng.community.entity.User;
import com.zoupeng.community.event.EventProducer;
import com.zoupeng.community.service.LikeService;
import com.zoupeng.community.util.CommunityConstant;
import com.zoupeng.community.util.HostHolder;
import com.zoupeng.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;


@Controller
public class LikeController  implements CommunityConstant {

    @Autowired
    LikeService likeService;

    @Autowired
    HostHolder hostHolder;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private RedisTemplate redisTemplate;
    @RequestMapping(path = "/like", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> like(int entityType, int entityId, int entityUserId,int postId) {
        User user = hostHolder.getUser();
        Map<String, Object> map = new HashMap<>();
        if (user == null){
            map.put("code",1);
            map.put("msg","点赞失败，请检查你有没有登录！" );
            return map;
        }else {
            //点赞
            likeService.like(user.getId(), entityType, entityId, entityUserId);
            //数量
            long likeCount = likeService.findEntityLikeCount(entityType, entityId);
            //状态
            int likeStatus = likeService.findEntityLikeStatus(user.getId(), entityType, entityId);
            map.put("msg", null);
            map.put("code", 0);
            map.put("likeCount", likeCount);
            map.put("likeStatus", likeStatus);

            if (entityType == ENTITY_TYPE_POST){
                //计算帖子分数
                String redisKey = RedisKeyUtil.getPostScoreKey();
                redisTemplate.opsForSet().add(redisKey,postId);
            }

            //触发点赞事件，只有赞的时候通知，取消赞不需要通知
            if (likeStatus == 1){
                Event event = new Event()
                        .setTopic(TOPIC_LIKE)
                        .setUserId(hostHolder.getUser().getId())
                        .setEntityType(entityType)
                        .setEntityId(entityId)
                        .setEntityUserId(entityUserId)
                        .setData("postId",postId);
                eventProducer.fireEvent(event);
            }
        }

        return map;
    }
}
