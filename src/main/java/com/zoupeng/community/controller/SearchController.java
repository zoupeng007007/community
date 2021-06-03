package com.zoupeng.community.controller;


import com.zoupeng.community.entity.DiscussPost;
import com.zoupeng.community.entity.Page;
import com.zoupeng.community.service.CommentService;
import com.zoupeng.community.service.ElasticsearchService;
import com.zoupeng.community.service.LikeService;
import com.zoupeng.community.service.UserService;
import com.zoupeng.community.util.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class SearchController  implements CommunityConstant {

    @Autowired
    private ElasticsearchService elasticsearchService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    @Autowired
    CommentService commentService;

    // search?keyword=xxx
    @RequestMapping(path = "/search",method = RequestMethod.GET)
    public String search(String keyword, Page page, Model model){


        //搜索帖子
        PageImpl<DiscussPost> result = elasticsearchService.searchDiscussPost(keyword, page.getCurrent() - 1, page.getLimit());
        //聚合数据
        List<Map<String,Object>> discussPostVoList = new ArrayList<>();
        if (result != null){
            for (DiscussPost discussPost:result){
                Map<String,Object> discussPostVo = new HashMap<>();
                //帖子
                discussPostVo.put("post",discussPost);
                //作者
                discussPostVo.put("user",userService.findUserById(discussPost.getUserId()));
                //点赞数量
                discussPostVo.put("likeCount",likeService.findEntityLikeCount(ENTITY_TYPE_POST,discussPost.getId()));
                //评论数量
                discussPostVo.put("commentCount",commentService.findCommentCount(ENTITY_TYPE_POST,discussPost.getId()));
                discussPostVoList.add(discussPostVo);
            }
        }
        model.addAttribute("discussPostVoList",discussPostVoList);
        model.addAttribute("keyword",keyword);
        page.setPath("/search?keyword=" + keyword);
        page.setRows(result == null?0: (int) result.getTotalElements());

        return "/site/search";
    }
}
