package com.chinaero.kerbaltalks.contorller;

import com.chinaero.kerbaltalks.annotation.LoginRequired;
import com.chinaero.kerbaltalks.entity.Comment;
import com.chinaero.kerbaltalks.entity.DiscussPost;
import com.chinaero.kerbaltalks.entity.Event;
import com.chinaero.kerbaltalks.event.EventProducer;
import com.chinaero.kerbaltalks.service.CommentService;
import com.chinaero.kerbaltalks.service.DiscussPostService;
import com.chinaero.kerbaltalks.util.HostHolder;
import com.chinaero.kerbaltalks.util.KerbaltalksConstant;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;
import java.util.HashMap;

@Controller
@RequestMapping("/comment")
public class    CommentController {

    private final HostHolder hostHolder;
    private final CommentService commentService;
    private final EventProducer eventProducer;
    private final DiscussPostService discussPostService;

    public CommentController(HostHolder hostHolder, CommentService commentService, EventProducer eventProducer, DiscussPostService discussPostService) {
        this.hostHolder = hostHolder;
        this.commentService = commentService;
        this.eventProducer = eventProducer;
        this.discussPostService = discussPostService;
    }

    @LoginRequired
    @RequestMapping(path = "/add/{discussPostId}", method = RequestMethod.POST)
    public String addComment(@PathVariable("discussPostId") String discussPostId, Comment comment) {
        comment.setUserId(hostHolder.getUser().getId());
        comment.setStatus(0);
        comment.setCreateTime(new Date());
        commentService.addComment(comment);

        // 触发评论事件
        Event event = Event.builder()
                .topic(KerbaltalksConstant.TOPIC_COMMENT)
                .userId(hostHolder.getUser().getId())
                .entityType(comment.getEntityType())
                .entityId(comment.getEntityId())
                .data(new HashMap<>())
                .build()
                .addData("postId", discussPostId);

        if (comment.getEntityType() == KerbaltalksConstant.ENTITY_TYPE_POST) {
            DiscussPost target = discussPostService.findDiscussPostById(comment.getEntityId());
            event.setEntityUserId(target.getUserId());
        } else if (comment.getEntityType() == KerbaltalksConstant.ENTITY_TYPE_COMMENT) {
            Comment target = commentService.findCommentById(comment.getEntityId());
            event.setEntityUserId(target.getUserId());
        }
        // 并发、异步
        eventProducer.fireEvent(event);

        return "redirect:/discuss/detail/" + discussPostId;
    }


}
