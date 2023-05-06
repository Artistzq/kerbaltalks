package com.chinaero.kerbaltalks.entity;

import lombok.*;
import org.apache.kafka.clients.producer.KafkaProducer;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author : Artis Yao
 */
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Event {

    private String topic;
    private int userId;
    private int entityType;
    private int entityId;
    private int entityUserId;
    private Map<String, Object> data;

    public Event addData(String key, Object value) {
        this.data.put(key, value);
        return this;
    }
}
