package com.chinaero.kerbaltalks.contorller;

import com.chinaero.kerbaltalks.annotation.LoginRequired;
import com.chinaero.kerbaltalks.entity.DiscussPost;
import com.chinaero.kerbaltalks.entity.User;
import com.chinaero.kerbaltalks.service.DiscussPostService;
import com.chinaero.kerbaltalks.util.HostHolder;
import com.chinaero.kerbaltalks.util.KerbaltalksUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.xml.stream.events.Comment;
import java.util.Date;

@Controller
@RequestMapping("/discuss")
public class DiscussPostController {

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private HostHolder hostHolder;

    @LoginRequired
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @ResponseBody
    public String addDiscussPost(String title, String content) {
        User user = hostHolder.getUser();
        if (user == null) {
            return KerbaltalksUtil.getJSONString(403, "请登录后再发帖！");
        }

        DiscussPost post = new DiscussPost();
        post.setUserId(user.getId());
        post.setTitle(title);
        post.setContent(content);
        post.setCreateTime(new Date());
        discussPostService.addDiscussPost(post);

        // 报错的情况，将来统一处理。
        return KerbaltalksUtil.getJSONString(0, "发布成功！");
    }

}
