package com.chinaero.kerbaltalks.dao;

import com.chinaero.kerbaltalks.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CommentMapper {

    List<Comment> selectCommentsByEntity(int entityType, int entityId, int offset, int limit);

    int selectCountsByEntity(int entityType, int entityId);

}
