package com.zoupeng.community.dao;

import com.zoupeng.community.entity.LoginTicket;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;


@SpringBootTest
class LoginTicketMapperTest {

    @Autowired
    LoginTicketMapper mapper;
    @Test
    void insertLoginTicket() {
        LoginTicket ticket = new LoginTicket();
        ticket.setExpired(new Date());
        ticket.setStatus(0);
        ticket.setTicket("avc");
        ticket.setUserId(101);
        mapper.insertLoginTicket(ticket);
    }

    @Test
    void selectByTicket() {
        LoginTicket ticket = mapper.selectByTicket("avc");
        System.out.println(ticket);
    }

    @Test
    void updateStatus() {
        mapper.updateStatus("avc",1);
        System.out.println(mapper.selectByTicket("avc"));
    }
}