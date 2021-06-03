package com.zoupeng.community.service;

import com.zoupeng.community.dao.DiscussPostMapper;
import com.zoupeng.community.dao.elasticsearch.DiscussPostRepository;
import com.zoupeng.community.entity.DiscussPost;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


//处理ES业务
@Service
public class ElasticsearchService {

    @Autowired
    private DiscussPostRepository discussPostRepository;

    @Autowired
    private ElasticsearchRestTemplate restTemplate;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    //增加帖子
    public void saveDiscussPost(DiscussPost discussPost) {
        discussPostRepository.save(discussPost);
    }

    public void deleteDiscussPost(int id) {
        discussPostRepository.deleteById(id);
    }

    //查询，支持分页
    public PageImpl<DiscussPost> searchDiscussPost(String keyword, int current, int limit) {
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery(keyword, "title", "content"))
                .withSort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                .withPageable(PageRequest.of(current, limit))
                .withHighlightFields(
                        new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
                        new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
                ).build();
        SearchHits<DiscussPost> hits = restTemplate.search(searchQuery, DiscussPost.class);
        List<DiscussPost> list = new ArrayList<>();
        if (hits != null) {
            for (SearchHit hit : hits) {
                DiscussPost post = discussPostMapper.selectDiscussPostById(Integer.parseInt(hit.getId()));
                List contentList = hit.getHighlightField("content");
                if (contentList != null && contentList.size() > 0) {
                    String content = (String) contentList.get(0);
                    post.setContent(content);
                }
                List titleList = hit.getHighlightField("title");
                if (titleList != null && titleList.size() > 0) {
                    String title = (String) titleList.get(0);
                    post.setTitle(title);
                }
                list.add(post);
            }
            Pageable pageable = PageRequest.of(current, limit);
            //分页
            PageImpl<DiscussPost> discussPosts = new PageImpl<>(list, pageable, hits.getTotalHits());
            return discussPosts;
        }
        return null;
    }
}
