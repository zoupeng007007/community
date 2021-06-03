package com.zoupeng.community.quartz;

import com.zoupeng.community.entity.DiscussPost;
import com.zoupeng.community.service.DiscussPostService;
import com.zoupeng.community.service.ElasticsearchService;
import com.zoupeng.community.service.LikeService;
import com.zoupeng.community.util.CommunityConstant;
import com.zoupeng.community.util.RedisKeyUtil;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Slf4j
public class PostScoreRefreshJob implements Job, CommunityConstant {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private ElasticsearchService elasticsearchService;

    private static final Date epoch;//网站成立时间

    static {
        try {
            epoch = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2021-04-01 00:00:00");
        } catch (ParseException e) {
            throw new RuntimeException("初始化纪元失败");
        }
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        String redisKey = RedisKeyUtil.getPostScoreKey();
        //多次访问一个key
        BoundSetOperations operations = redisTemplate.boundSetOps(redisKey);
        if (operations.size() == 0) {
            log.info("任务取消，没有需要刷新的帖子");
            return;
        }
        log.info("任务开始，正在刷新帖子分数" + operations.size());

        while (operations.size() > 0) {
            //弹出一个值
            this.refresh((Integer) operations.pop());
        }
        log.info("任务结束，分数刷新完毕");
    }

    private void refresh(Integer postId) {
        DiscussPost post = discussPostService.findDiscussPostById(postId);
        if (post == null) {
            log.error("该帖子不存在，id =" + postId);
            return;
        }
        //是否加精
        boolean wonderful = post.getStatus() == 1;
        //评论数量
        int commentCount = post.getCommentCount();
        //点赞数量
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, postId);

        //计算权重
        double w = (wonderful ? 75 : 0) + commentCount * 10 + likeCount * 2;
        //分数 = 帖子权重 +  距离天数
        double score = Math.log10(Math.max(w, 1)
                + (post.getCreateTime().getTime() - epoch.getTime())/ (1000 * 3600 * 24));//1000表示换算成一秒

        //更新帖子的分数
        discussPostService.updateScore(score,postId);

        //同步搜索的数据
        post.setScore(score);
        elasticsearchService.saveDiscussPost(post);

    }
}
