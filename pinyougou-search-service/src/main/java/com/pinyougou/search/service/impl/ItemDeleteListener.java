package com.pinyougou.search.service.impl;


import com.pinyougou.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import java.util.Arrays;

@Component
public class ItemDeleteListener implements MessageListener {

    @Autowired
    private ItemSearchService itemSearchService;
    @Override
    public void onMessage(Message message) {
        ObjectMessage objectMessage=(ObjectMessage)message;
        try {
            Long[]ids=(Long[])objectMessage.getObject();
            itemSearchService.delete(Arrays.asList(ids));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
