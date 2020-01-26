package com.lifan.community;

import com.lifan.community.util.MailCilent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MailTests {
    @Autowired
    private MailCilent mailCilent;

    @Autowired
    private TemplateEngine templateEngine;
    @Test
    public void textTextMail(){
        mailCilent.sendMail("648535355@qq.com","Test","welcome");
    }

    @Test
    public  void testHtmlMail(){
        Context context = new Context();
        context.setVariable("username","sunday");

        String content = templateEngine.process("/mail/demo",context);
        System.out.println(content);

        mailCilent.sendMail("648535355@qq.com","HTML",content);
    }


}
