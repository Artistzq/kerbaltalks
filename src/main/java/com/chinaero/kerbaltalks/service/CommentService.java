package com.chinaero.kerbaltalks.service;

import com.chinaero.kerbaltalks.dao.CommentMapper;
import com.chinaero.kerbaltalks.entity.Comment;
import com.chinaero.kerbaltalks.util.KerbaltalksConstant;
import com.chinaero.kerbaltalks.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class CommentService {

    private final CommentMapper commentMapper;
    private final SensitiveFilter sensitiveFilter;
    private final DiscussPostService discussPostService;

    public CommentService(CommentMapper commentMapper, SensitiveFilter sensitiveFilter, DiscussPostService discussPostService) {
        this.commentMapper = commentMapper;
        this.sensitiveFilter = sensitiveFilter;
        this.discussPostService = discussPostService;
    }

    public List<Comment> findCommentsByEntity(int entityType, int entityId, int offset, int limit) {
        return commentMapper.selectCommentsByEntity(entityType, entityId, offset, limit);
    }

    public int findCommentsCount(int entityType, int entityId) {
        return commentMapper.selectCountsByEntity(entityType, entityId);
    }

    /**
     * 事务管理，很多人发帖
     * @param comment
     * @return
     */
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public int addComment(Comment comment) {
        if (comment == null) {
            throw new IllegalArgumentException("参数不能为空");
        }

        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
        comment.setContent(sensitiveFilter.filter(comment.getContent()));
        int rows = commentMapper.insertComment(comment);

        if (comment.getEntityType() == KerbaltalksConstant.ENTITY_TYPE_POST) {
            int count = commentMapper.selectCountsByEntity(comment.getEntityType(), comment.getEntityId());
            discussPostService.updateCommentCount(comment.getEntityId(), count);
        }

        return rows;
    }

    public Comment findCommentById(int id) {
        return commentMapper.selectCommentById(id);
    }
}
