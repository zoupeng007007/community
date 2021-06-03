package com.zoupeng.community.dao;

import com.zoupeng.community.entity.Message;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;


@SpringBootTest
class MessageMapperTest {

    @Autowired
    MessageMapper messageMapper;
    @Test
    void selectConversations() {
        List<Message> messages = messageMapper.selectConversations(9998, 0, Integer.MAX_VALUE);
        for (Message message: messages){
            System.out.println(message);
        }
    }

    @Test
    void selectConversationCount() {
        int i = messageMapper.selectConversationCount(9998);
        System.out.println(i);
    }

    @Test
    void selectLetters() {


    }

    @Test
    void selectLetterCount() {
    }


    @Test
    void selectLetterUnreadCount() {
        System.out.println(messageMapper.selectLetterUnreadCount(9998,null));
    }
}