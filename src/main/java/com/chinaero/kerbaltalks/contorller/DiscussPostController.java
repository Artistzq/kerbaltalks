package com.chinaero.kerbaltalks.contorller;

import com.chinaero.kerbaltalks.annotation.AntiCSRF;
import com.chinaero.kerbaltalks.annotation.LoginRequired;
import com.chinaero.kerbaltalks.entity.Comment;
import com.chinaero.kerbaltalks.entity.DiscussPost;
import com.chinaero.kerbaltalks.entity.Page;
import com.chinaero.kerbaltalks.entity.User;
import com.chinaero.kerbaltalks.service.CommentService;
import com.chinaero.kerbaltalks.service.DiscussPostService;
import com.chinaero.kerbaltalks.service.LikeService;
import com.chinaero.kerbaltalks.service.UserService;
import com.chinaero.kerbaltalks.util.HostHolder;
import com.chinaero.kerbaltalks.util.KerbaltalksConstant;
import com.chinaero.kerbaltalks.util.KerbaltalksUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

@Controller
@RequestMapping("/discuss")
public class DiscussPostController implements KerbaltalksConstant {

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private LikeService likeService;

    @LoginRequired
    @AntiCSRF
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

    @RequestMapping(path = "/detail/{discussPostId}", method = RequestMethod.GET)
    public String getDiscussPost(@PathVariable("discussPostId") int discussPostId, Model model, Page page) {
        // 帖子
        DiscussPost post = discussPostService.findDiscussPostById(discussPostId);
        model.addAttribute("post", post);
        // 作者
        User user = userService.findUserById(post.getUserId());
        model.addAttribute("username", user.getUsername());
        model.addAttribute("headerUrl", user.getHeaderUrl());
        // 帖子点赞数量
        long likeCount = likeService.findEntityLikeCount(KerbaltalksConstant.ENTITY_TYPE_POST, post.getId());
        model.addAttribute("likeCount", likeCount);
        // 帖子点赞状态
        int likeStatus = hostHolder.getUser() == null ? 0 :
                likeService.findEntityLikeStatus(hostHolder.getUser().getId(), KerbaltalksConstant.ENTITY_TYPE_POST, post.getId());
        model.addAttribute("likeStatus", likeStatus);
        // 评论分页信息
        page.setLimit(5);
        page.setPath("/discuss/detail/" + discussPostId);
        page.setRows(post.getCommentCount());

        // 评论：帖子的评论
        List<Comment> comments = commentService.findCommentsByEntity(
                ENTITY_TYPE_POST, post.getId(), page.getOffset(), page.getLimit());
        // 评论的VO列表
        List<Map<String, Object>> commentVos = new ArrayList<>();
        if (comments != null) {
            for (Comment comment: comments) {
                // 评论VO
                Map<String, Object> commentVo = new HashMap<>();
                // 添加了一个评论
                commentVo.put("comment", comment);
                // 添加该评论的作者
                commentVo.put("user", userService.findUserById(comment.getUserId()));
                // 某评论的点赞数量
                likeCount = likeService.findEntityLikeCount(KerbaltalksConstant.ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("likeCount", likeCount);
                // 某评论的点赞状态
                likeStatus = hostHolder.getUser() == null ? 0 :
                        likeService.findEntityLikeStatus(hostHolder.getUser().getId(), KerbaltalksConstant.ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("likeStatus", likeStatus);
                // 寻找评论的评论：回复列表
                List<Comment> replies = commentService.findCommentsByEntity(
                        ENTITY_TYPE_COMMENT, comment.getId(), 0, Integer.MAX_VALUE);
                // 回复的VO列表
                List<Map<String, Object>> replyVos = new ArrayList<>();
                if (replies != null) {
                    for (Comment reply: replies) {
                        Map<String, Object> replyVo = new HashMap<>();
                        replyVo.put("reply", reply);
                        replyVo.put("user", userService.findUserById(reply.getUserId()));
                        // 某评论的评论的点赞数量
                        likeCount = likeService.findEntityLikeCount(KerbaltalksConstant.ENTITY_TYPE_COMMENT, reply.getId());
                        replyVo.put("likeCount", likeCount);
                        // 某评论的点赞状态
                        likeStatus = hostHolder.getUser() == null ? 0 :
                                likeService.findEntityLikeStatus(
                                        hostHolder.getUser().getId(), KerbaltalksConstant.ENTITY_TYPE_COMMENT, reply.getId()
                                );
                        replyVo.put("likeStatus", likeStatus);
                        // 判断有没有回复目标
                        User target = reply.getTargetId() == 0 ? null : userService.findUserById(reply.getTargetId());
                        replyVo.put("target", target);

                        replyVos.add(replyVo);
                    }
                }
                commentVo.put("replies", replyVos);
                // 回复数量
//                int replyCount = commentService.findCommentsCount(ENTITY_TYPE_COMMENT, comment.getId());
                int replyCount = replies != null ? replies.size() : 0;
                commentVo.put("replyCount", replyCount);

                commentVos.add(commentVo);
            }
        }
        model.addAttribute("comments", commentVos);

        return "site/discuss-detail";
    }

}
