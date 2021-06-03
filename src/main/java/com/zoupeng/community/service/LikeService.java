package com.zoupeng.community.service;

import com.zoupeng.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

//点赞
@Service
public class LikeService {
    @Autowired
    private RedisTemplate redisTemplate;

    //点赞业务
    public void like(int userId, int entityType, int entityId, int entityUserId) {
        //由于在对评论点赞时，user的赞也需要更新，需要考虑Redis事务。
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
                String userLikeKey = RedisKeyUtil.getUserLikeKey(entityUserId);
                Boolean isMember = redisOperations.opsForSet().isMember(entityLikeKey, userId);
                //开启事务
                redisOperations.multi();
                if (isMember) {//点过，需要取消
                    redisTemplate.opsForSet().remove(entityLikeKey, userId);
                    redisOperations.opsForValue().decrement(userLikeKey);
                } else {//没点过
                    redisTemplate.opsForSet().add(entityLikeKey, userId);
                    redisOperations.opsForValue().increment(userLikeKey);
                }
                return redisOperations.exec();
            }
        });
    }

    //查询实体点赞数量
    public long findEntityLikeCount(int entityType, int entityId) {
        return redisTemplate.opsForSet().size(RedisKeyUtil.getEntityLikeKey(entityType, entityId));
    }

    //查询某人对某实体的点赞状态,可以采用boolean，但可能以后业务有点踩的功能，采用int
    public int findEntityLikeStatus(int userId, int entityType, int entityId) {
        return redisTemplate.opsForSet().isMember(RedisKeyUtil.getEntityLikeKey(entityType, entityId), userId) ? 1 : 0;
    }

    //查询某个用户获得的赞的数量
    public int findUserLikeCount(int userId) {
        String userLikeKey = RedisKeyUtil.getUserLikeKey(userId);
        Integer count = (Integer) redisTemplate.opsForValue().get(userLikeKey);
        return count == null ? 0 : count.intValue();
    }
}
