package com.zoupeng.community.dao.elasticsearch;

import com.zoupeng.community.entity.DiscussPost;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

//实现ElasticsearchRepository声明作用的类及主键类型
@Repository
public interface DiscussPostRepository extends ElasticsearchRepository<DiscussPost,Integer> {
}
