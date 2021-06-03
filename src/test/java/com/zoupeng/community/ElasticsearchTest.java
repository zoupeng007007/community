package com.zoupeng.community;


import com.zoupeng.community.dao.DiscussPostMapper;
import com.zoupeng.community.dao.elasticsearch.DiscussPostRepository;
import com.zoupeng.community.entity.DiscussPost;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.test.context.ContextConfiguration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class ElasticsearchTest {

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private DiscussPostRepository discussPostRepository;

    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Test
    public void testInsert(){
        discussPostRepository.save(discussPostMapper.selectDiscussPostById(8));
        discussPostRepository.save(discussPostMapper.selectDiscussPostById(11));
        discussPostRepository.save(discussPostMapper.selectDiscussPostById(15));
    }

    @Test
    public void testInsertList(){
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(1,0,100,0));
    }

    @Test
    public void testUpdate(){
        DiscussPost post = discussPostMapper.selectDiscussPostById(8);
        post.setContent("撒旦就可能个几把。哈哈");
        discussPostRepository.save(post);
    }

    @Test
    public void testDelete(){
        discussPostRepository.deleteById(8);
    }

    @Test
    public void testDeleteAll(){
        discussPostRepository.deleteAll();
    }

    @Test
    public void testSearchByRepository(){
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery("一个测试撒旦来吗", "title", "content"))
                .withSort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                .withPageable(PageRequest.of(0, 10))
                .withHighlightFields(
                        new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
                        new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
                ).build();

        //底层获取了高亮的值，但没有处理
//        Page<DiscussPost> search1 = discussPostRepository.search(searchQuery);


        //采用elasticsearchRestTemplate
        SearchHits<DiscussPost> search = elasticsearchRestTemplate.search(searchQuery, DiscussPost.class);

        if (search.getTotalHits() <= 0){
            return;
        }
        List<DiscussPost> searchProductList = search.stream().map(SearchHit::getContent).collect(Collectors.toList());

        Pageable pageable = PageRequest.of(0,10);
        //分页
        PageImpl<DiscussPost> discussPosts = new PageImpl<>(searchProductList, pageable, search.getTotalHits());
        for (DiscussPost discussPost:discussPosts){
            System.out.println(discussPost);
        }
        System.out.println(discussPosts.getTotalPages());
        System.out.println(discussPosts.getNumber());
        System.out.println(discussPosts.getSize());
        System.out.println(discussPosts.getTotalElements());


    }

    @Test
    public void testSearchByTemplate(){
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery("一个测试撒旦来吗", "title", "content"))
                .withSort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                .withPageable(PageRequest.of(0, 10))
                .withHighlightFields(
                        new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
                        new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
                ).build();
        SearchHits<DiscussPost> hits = elasticsearchRestTemplate.search(searchQuery, DiscussPost.class);
        List<DiscussPost> list = new ArrayList<>();
        for (SearchHit hit:hits){
            DiscussPost post = discussPostMapper.selectDiscussPostById(Integer.parseInt(hit.getId()));

            List contentList = hit.getHighlightField("content");
            if (contentList!=null){
                String content = (String) contentList.get(0);
                post.setContent(content);
            }
            List titleList = hit.getHighlightField("title");
            if (titleList!=null){
                String title = (String) titleList.get(0);
                post.setTitle(title);
            }
            list.add(post);
        }
        Pageable pageable = PageRequest.of(0,10);
        //分页
        PageImpl<DiscussPost> discussPosts = new PageImpl<>(list, pageable, hits.getTotalHits());
        for (DiscussPost discussPost:discussPosts){
            System.out.println(discussPost);
        }
    }
}
