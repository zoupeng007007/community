package com.zoupeng.community.dao;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

@Repository //访问数据库的bean,加入容器
@Primary //当更改bean，有歧义时，优先装配
public class AlphaDaoImpl implements AlphaDao {
    @Override
    public String select() {
        return "查询成功";
    }
}
