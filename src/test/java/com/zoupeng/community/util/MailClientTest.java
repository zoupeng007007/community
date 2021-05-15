package com.zoupeng.community.util;

import com.zoupeng.community.CommunityApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.MailSender;
import org.springframework.test.context.ContextConfiguration;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;


@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
class MailClientTest {

    @Autowired
    private MailClient mailClient;

    /*thyleaf模板引擎*/
    @Autowired
    private TemplateEngine templateEngine;
    @Test
    void sentMail() {
        mailClient.sentMail("1131739538@qq.com","testMailClient","hello");
    }

    @Test
    public void testHtmlMail(){
        Context context = new Context();
        context.setVariable("username","god");

        String content = templateEngine.process("/mail/demo", context);
        System.out.println(content);

        mailClient.sentMail("1131739538@qq.com","html",content);
    }


}