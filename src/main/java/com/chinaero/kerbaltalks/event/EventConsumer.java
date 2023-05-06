package com.chinaero.kerbaltalks.event;

import com.alibaba.fastjson.JSONObject;
import com.chinaero.kerbaltalks.entity.Event;
import com.chinaero.kerbaltalks.entity.Message;
import com.chinaero.kerbaltalks.service.DiscussPostService;
import com.chinaero.kerbaltalks.service.MessageService;
import com.chinaero.kerbaltalks.util.KerbaltalksConstant;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author : Artis Yao
 */
@Component
public class EventConsumer {

    private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);

    @Autowired
    MessageService messageService;

    @Autowired
    DiscussPostService discussPostService;

    @KafkaListener(topics = {KerbaltalksConstant.TOPIC_COMMENT, KerbaltalksConstant.TOPIC_LIKE, KerbaltalksConstant.TOPIC_FOLLOW})
    public void handleCommentMessage(ConsumerRecord record) {

        System.out.println("66666666666666666666666666666666666666666666666666666666666666666666");

        if (record == null || record.value() == null) {
            logger.error("消息内容为空");
            return ;
        }

        // 从Record里读取事件Event
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null) {
            logger.error("消息格式错误！");
            return;
        }

        // 发送站内通知
        Message message = new Message();
        message.setFromId(KerbaltalksConstant.SYSTEM_USER_ID);
        message.setToId(event.getEntityUserId());
        message.setConversationId(event.getTopic());
        message.setCreateTime(new Date());

        Map<String, Object> content = new HashMap<>();
        content.put("userId", event.getUserId());
        content.put("entityType", event.getEntityType());
        content.put("entityId", event.getEntityId());

        if (! event.getData().isEmpty()) {
            for (Map.Entry<String, Object> entry: event.getData().entrySet()) {
                content.put(entry.getKey(), entry.getValue());
            }
        }

        System.out.println("Message Coming!");
        message.setContent(JSONObject.toJSONString(content));
        messageService.addMessage(message);
    }
}
