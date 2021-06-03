package com.zoupeng.community.util;

import com.zoupeng.community.CommunityApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;


@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
class SensitiveFilterTest {
    @Autowired
    SensitiveFilter filter;
    @Test
    void filter() {
        String s = filter.filter("赌♥博♥我爱你,可以♥开♥票，可以♥嫖♥娼，哈哈哈");
        System.out.println(s);
    }
}