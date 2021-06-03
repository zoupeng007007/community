package com.zoupeng.community.service;

import com.zoupeng.community.entity.User;
import com.zoupeng.community.util.CommunityConstant;
import com.zoupeng.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
//处理关注业务
public class FollowService implements CommunityConstant {
    @Autowired
    RedisTemplate redisTemplate;

    //用户关注
    //用户关注后，需要更新用户的关注信息，以及目标实体的被关注信息，需要考虑事务
    public void follow(int userId, int entityType, int entityId) {
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                //用户关注某个类型实体的key,存储实体的信息
                String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
                //被关注实体的key
                String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
                //事务开启
                redisOperations.multi();

                redisOperations.opsForZSet().add(followeeKey, entityId, System.currentTimeMillis());
                redisOperations.opsForZSet().add(followerKey, userId, System.currentTimeMillis());
                return redisOperations.exec();
            }
        });
    }

    public void unFollow(int userId, int entityType, int entityId) {
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                //用户关注某个类型实体的key,存储实体的信息
                String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
                //被关注实体的key
                String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
                //事务开启
                redisOperations.multi();

                redisOperations.opsForZSet().remove(followeeKey, entityId);
                redisOperations.opsForZSet().remove(followerKey, userId);
                return redisOperations.exec();
            }
        });
    }

    //查询用户关注实体的数量
    public long findFolloweeCount(int userId, int entityId) {
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityId);
        //关注实体的数量
        return redisTemplate.opsForZSet().zCard(followeeKey);
    }

    //查询实体的粉丝数量
    public long findFollowerCount(int entityType, int entityId) {
        String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
        return redisTemplate.opsForZSet().zCard(followerKey);
    }

    //查询当前用户是否已关注该实体
    public boolean hasFollowed(int userId, int entityType, int entityId) {
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
        return redisTemplate.opsForZSet().score(followeeKey, entityId) != null;
    }


    @Autowired
    UserService userService;
    //查询某个用户关注的人,按时间排序  支持分页
    public List<Map<String, Object>> findFollowees(int userId, int offset, int limit) {
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, ENTITY_TYPE_USER);
        //按时间倒叙
        Set<Integer> targetIds = redisTemplate.opsForZSet().reverseRange(followeeKey, offset, offset + limit - 1);
        if (targetIds == null) {
            return null;
        }
        List<Map<String,Object>> followeeList = new ArrayList<>();
        for (int targetId:targetIds){
            Map<String,Object> followee = new HashMap<>();
            User user = userService.findUserById(targetId);
            followee.put("user",user);
            Double score = redisTemplate.opsForZSet().score(followeeKey, targetId);
            followee.put("followeeTime",new Date(score.longValue()));
            followeeList.add(followee);
        }
        return followeeList;
    }

    //查询某个用户的粉丝'
    public List<Map<String,Object>> findFollowers(int userId,int offset,int limit){
        String followerKey = RedisKeyUtil.getFollowerKey(ENTITY_TYPE_USER,userId);
        Set<Integer> targetIds = redisTemplate.opsForZSet().reverseRange(followerKey, offset, offset + limit - 1);

        if (targetIds == null){
            return null;
        }
        List<Map<String,Object>> followeeList = new ArrayList<>();
        for (int targetId:targetIds){
            Map<String,Object> followee = new HashMap<>();
            User user = userService.findUserById(targetId);
            followee.put("user",user);
            Double score = redisTemplate.opsForZSet().score(followerKey, targetId);
            followee.put("followerTime",new Date(score.longValue()));
            followeeList.add(followee);
        }
        return followeeList;
    }
}
