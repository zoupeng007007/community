package com.zoupeng.community.service;

import com.zoupeng.community.dao.DiscussPostMapper;
import com.zoupeng.community.entity.DiscussPost;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DiscussPostService {
    @Autowired
    private DiscussPostMapper mapper;

    public List<DiscussPost> findDiscussPosts(int userId, int offset, int limit) {
        return mapper.selectDiscussPost(userId, offset, limit);
    }

    public int findDiscussPostRows(int userId) {
        return mapper.selectDiscussPostRows(userId);
    }

}
