package com.zoupeng.community.dao;

import com.zoupeng.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper
@Component
public interface DiscussPostMapper {
    //考虑能够查到单个用户的所有帖子，也是为动态sql，支持分页
    List<DiscussPost> selectDiscussPost(int userId,int offset,int limit);
    //查询表里一共有多少条数据,当需要查询某个用户的所有帖子数量时，加上userId @Param别名
    //如果需要动态获取参数，但方法中有且只有一个参数并且在if中使用，必须取别名，否则后续会报错
    int selectDiscussPostRows(@Param("userId") int userId);
}
