package springboottest.springboot_demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping
public class QueueController {
    @Autowired
    private JmsMessagingTemplate jmsMessagingTemplate;

    @RequestMapping("/sendsms")
    public void sendSms(){
        Map map=new HashMap<>();
        map.put("mobile", "15820341795");
        map.put("template_code", "SMS_173349948");
        map.put("sign_name", "我做煮");
        map.put("param", "{\"code\":\"102931\"}");

        jmsMessagingTemplate.convertAndSend("sms", map);
    }
}
