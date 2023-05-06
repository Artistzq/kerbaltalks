package com.chinaero.kerbaltalks.event;

import com.alibaba.fastjson.JSONObject;
import com.chinaero.kerbaltalks.entity.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;


/**
 * @Author : Artis Yao
 */
@Component
public class EventProducer {

    @Autowired
    private KafkaTemplate kafkaTemplate;

    public void fireEvent(Event event) {
        kafkaTemplate.send(event.getTopic(), JSONObject.toJSONString(event));
        System.out.println("Message Sending!");
    }

}
