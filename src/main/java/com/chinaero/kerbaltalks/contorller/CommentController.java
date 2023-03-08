package com.chinaero.kerbaltalks.contorller;

import com.chinaero.kerbaltalks.entity.Comment;
import com.chinaero.kerbaltalks.service.CommentService;
import com.chinaero.kerbaltalks.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

@Controller
@RequestMapping("/comment")
public class CommentController {

    private final HostHolder hostHolder;
    private final CommentService commentService;

    public CommentController(HostHolder hostHolder, CommentService commentService) {
        this.hostHolder = hostHolder;
        this.commentService = commentService;
    }

    @RequestMapping(path = "/add/{discussPostId}", method = RequestMethod.POST)
    public String addComment(@PathVariable("discussPostId") String discussPostId, Comment comment) {
        comment.setUserId(hostHolder.getUser().getId());
        comment.setStatus(0);
        comment.setCreateTime(new Date());
        commentService.addComment(comment);

        return "redirect:/discuss/detail/" + discussPostId;
    }


}
