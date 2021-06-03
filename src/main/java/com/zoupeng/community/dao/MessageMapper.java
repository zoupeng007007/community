package com.zoupeng.community.dao;

import com.zoupeng.community.entity.Message;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper
@Component
public interface MessageMapper {
    // 查询当前用户的会话列表，针对每个会话只返回一条最新的私信
    List<Message> selectConversations(int userId, int offset, int limit);

    //查询当前用户的会话数量，当前用户接收或发送的私信
    int selectConversationCount(int userId);

    //查询某个会话包含的私信列表(私信列表时展现)
    List<Message> selectLetters(String conversationId, int offset, int limit);

    //查询某个会话所包含的私信数量
    int selectLetterCount(String conversationId);

    //查询未读私信的数量,动态查询，如果传了会话id则查询的是会话的私信数，否则是所有当前用户所有未读的私信数
    int selectLetterUnreadCount(int userId, String conversationId);

    //增加消息
    int insertMessage(Message message);

    //修改消息状态，已读未读删除
    int updateStatus(List<Integer> ids, int status);

    //查询某一个主题下的最新的通知
    Message selectLatestNotice(int userId, String topic);

    //查询某个主题所包含的通知的数量
    int selectNoticeCount(int userId, String topic);

    //查询未读的通知数量
    int selectNoticeUnreadCount(int userId, String topic);

    //查询某给主题所包含的通知列表
    List<Message> selectNotices(int userId,String topic,int offset,int limit);
}
