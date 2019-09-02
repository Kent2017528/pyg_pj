package com.pinyougou.page.service.impl;

import com.pinyougou.page.service.ItemPageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

@Component
public class PageListener implements MessageListener {
    @Autowired
    private ItemPageService itemPageService;
    @Override
    public void onMessage(Message message) {
        TextMessage textMessage=(TextMessage)message;

        try {
            String text = textMessage.getText();

            boolean b = itemPageService.getItemHtml(Long.parseLong(text));
            System.out.println("生成静态页面结果："+b);
        } catch (JMSException e) {
            e.printStackTrace();
        }

    }
}
