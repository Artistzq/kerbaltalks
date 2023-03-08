package com.chinaero.kerbaltalks.dao;

import com.chinaero.kerbaltalks.entity.Message;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface MessageMapper {
    // 会话：两个用户的对话，Message

    // 当前用户的会话列表，需要去重
    List<Message> selectConversations(int userId, int offset, int limit);

    // 当前用户接收到多少个会话
    int selectConversationCount(int userId);

    // 某个会话里面有多少条聊天记录？
    List<Message> selectLetters(String conversationId, int offset, int limit);

    int selectLetterCount(String conversationId);

    // 某个用户的未读私信数量
    int selectUnreadLetterCount(int userId, String conversationId);
}
