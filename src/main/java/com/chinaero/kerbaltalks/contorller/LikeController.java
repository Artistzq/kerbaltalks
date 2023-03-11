package com.chinaero.kerbaltalks.contorller;

import com.chinaero.kerbaltalks.annotation.LoginRequired;
import com.chinaero.kerbaltalks.entity.User;
import com.chinaero.kerbaltalks.service.LikeService;
import com.chinaero.kerbaltalks.util.HostHolder;
import com.chinaero.kerbaltalks.util.KerbaltalksUtil;
import org.springframework.beans.factory.annotation.Autowired;
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


    public LikeController(HostHolder hostHolder, LikeService likeService) {
        this.hostHolder = hostHolder;
        this.likeService = likeService;
    }


    @LoginRequired
    @RequestMapping(path = "like", method = RequestMethod.POST)
    @ResponseBody
    public String like(int entityType, int entityId) {
        User user = hostHolder.getUser();

        // 点赞
        likeService.like(user.getId(), entityType, entityId);
        // 数量
        long likeCount = likeService.findEntityLikeCount(entityType, entityId);
        // 状态
        int likeStatus = likeService.findEntityLikeStatus(user.getId(), entityType, entityId);

        Map<String, Object> map = new HashMap<>();
        map.put("likeCount", likeCount);
        map.put("likeStatus", likeStatus);

        return KerbaltalksUtil.getJSONString(0, null, map);
    }
}
