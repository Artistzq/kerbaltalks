package com.chinaero.kerbaltalks.controller;

import com.chinaero.kerbaltalks.annotation.LoginRequired;
import com.chinaero.kerbaltalks.entity.Event;
import com.chinaero.kerbaltalks.entity.User;
import com.chinaero.kerbaltalks.event.EventProducer;
import com.chinaero.kerbaltalks.service.LikeService;
import com.chinaero.kerbaltalks.util.HostHolder;
import com.chinaero.kerbaltalks.util.KerbaltalksConstant;
import com.chinaero.kerbaltalks.util.KerbaltalksUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
public class LikeController {

    private final LikeService likeService;
    private final HostHolder hostHolder;
    private final EventProducer eventProducer;


    public LikeController(HostHolder hostHolder, LikeService likeService, EventProducer eventProducer) {
        this.hostHolder = hostHolder;
        this.likeService = likeService;
        this.eventProducer = eventProducer;
    }


    @LoginRequired
    @RequestMapping(path = "like", method = RequestMethod.POST)
    @ResponseBody
    public String like(int entityType, int entityId, int entityUserId, int postId) {
        User user = hostHolder.getUser();

        // 点赞
        likeService.like(user.getId(), entityType, entityId, entityUserId);
        // 数量
        long likeCount = likeService.findEntityLikeCount(entityType, entityId);
        // 状态
        int likeStatus = likeService.findEntityLikeStatus(user.getId(), entityType, entityId);

        Map<String, Object> map = new HashMap<>();
        map.put("likeCount", likeCount);
        map.put("likeStatus", likeStatus);

        //触发点赞事件
        if (likeStatus == 1) {
            Event event = Event.builder()
                    .topic(KerbaltalksConstant.TOPIC_LIKE)
                    .userId(hostHolder.getUser().getId())
                    .entityType(entityType)
                    .entityId(entityId)
                    .entityUserId(entityUserId)
                    .data(new HashMap<>())
                    .build()
                    .addData("postId", postId);
            eventProducer.fireEvent(event);
        }


        return KerbaltalksUtil.getJSONString(0, null, map);
    }
}
