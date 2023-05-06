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

    int insertMessage(Message message);

    int updateStatus(List<Integer> ids, int status);

    // 总共3个主题
    // 查询某个主题下最新的通知
    Message selectLatestNotice(int userId, String topic);

    // 查询某个主题所包含的通知数量
    int selectNoticeCount(int userId, String topic);

    // 查询未读的通知的数量
    int selectNoticeUnreadCount(int userId, String topic);

    // 查询某个主题的通知列表
    List<Message> selectNotice(int userId, String topic, int offset, int limit);
}
