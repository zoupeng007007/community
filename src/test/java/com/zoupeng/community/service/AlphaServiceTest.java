package com.zoupeng.community.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AlphaServiceTest {

    @Autowired
    private AlphaService alphaService;
    @Test
    void find1() {

    }

    @Test
    void save1() {
        Object o = alphaService.save1();
        System.out.println(o);
//        Object o = alphaService.save2();

    }
}