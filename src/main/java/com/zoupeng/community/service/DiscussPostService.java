package com.zoupeng.community.service;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.zoupeng.community.dao.DiscussPostMapper;
import com.zoupeng.community.entity.DiscussPost;
import com.zoupeng.community.util.SensitiveFilter;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class DiscussPostService {
    @Autowired
    private DiscussPostMapper mapper;

    //同时间最多缓存的页数
    @Value("${caffeine.posts.max-size}")
    private int maxSize;

    @Value("${caffine.posts.expire-seconds}")
    private int expireSeconds;

    //Caffeine核心接口Cache  ,  loadingCache ,AsyncLoadingCache

    //帖子列表的缓存
    private LoadingCache<String,List<DiscussPost>> postListCache;

    //帖子总数的缓存
    private LoadingCache<Integer,Integer> postRowsCache;

    @PostConstruct
    public void init(){
        //初始化帖子列表缓存
        postListCache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
                .build(new CacheLoader<String, List<DiscussPost>>() {
                    @Nullable
                    @Override
                    //当缓存中无数据时，通过什么方法获得的逻辑
                    public List<DiscussPost> load(@NonNull String key) throws Exception {
                        if (key == null || key.length() == 0){
                            throw new IllegalArgumentException("参数错误");
                        }
                        String[] params = key.split(":");
                        if (params == null || params.length != 2){
                            throw new IllegalArgumentException("参数错误");
                        }
                        int offset = Integer.valueOf(params[0]);
                        int limit = Integer.valueOf(params[1]);

                        //二级缓存  本地 ——--->DB
                        log.debug("load post list from DB");
                        return mapper.selectDiscussPosts(0,offset,limit,1);
                    }
                });
        //初始化帖子总数缓存

        postRowsCache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSeconds,TimeUnit.SECONDS)
                .build(new CacheLoader<Integer, Integer>() {
                    @Nullable
                    @Override
                    public Integer load(@NonNull Integer integer) throws Exception {
                        log.debug("load post rows from DB");
                        return mapper.selectDiscussPostRows(integer);
                    }
                });
    }
    public List<DiscussPost> findDiscussPost(int userId, int offset, int limit) {
        return mapper.selectDiscussPost(userId, offset, limit);
    }

    public List<DiscussPost> findDiscussPosts(int userId, int offset, int limit,int orderMode) {
        if (userId == 0 && orderMode == 1){
            return postListCache.get(offset + ":"  + limit);
        }
        log.debug("load post list from DB");
        return mapper.selectDiscussPosts(userId, offset, limit,orderMode);
    }

    public int findDiscussPostRows(int userId) {
        if (userId == 0){
            return postRowsCache.get(userId);
        }
        log.debug("load post rows from DB");
        return mapper.selectDiscussPostRows(userId);
    }


    @Autowired
    SensitiveFilter filter;
    public int addDiscussPost(DiscussPost discussPost){
        if (discussPost == null){
            throw new IllegalArgumentException("参数不能为空");
        }
        //转义HTML标记
        discussPost.setTitle(HtmlUtils.htmlEscape(discussPost.getTitle()));
        discussPost.setContent(HtmlUtils.htmlEscape(discussPost.getContent()));
        //过滤敏感词
        discussPost.setTitle(filter.filter(discussPost.getTitle()));
        discussPost.setContent(filter.filter(discussPost.getContent()));

        return mapper.insertDiscussPost(discussPost);
    }

    public DiscussPost findDiscussPostById(int id){
        return mapper.selectDiscussPostById(id);
    }

    public int updateCommentCount(int id,int commentCount){
        return mapper.updateCommentCount(id,commentCount);
    }

    public int updateType(int id,int type){
        return mapper.updateType(id,type);
    }

    public int updateStatus(int id,int status){
        return mapper.updateStatus(id,status);
    }

    public int updateScore(double score, Integer postId) {
        return mapper.updateScore(score,postId);
    }
}
