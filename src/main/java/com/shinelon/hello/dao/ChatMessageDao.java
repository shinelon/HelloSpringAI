package com.shinelon.hello.dao;

import com.shinelon.hello.model.entity.ChatMessageDO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 消息DAO接口
 *
 * @author shinelon
 */
@Repository
public interface ChatMessageDao extends JpaRepository<ChatMessageDO, Long> {

    /**
     * 根据会话ID查询所有消息，按创建时间升序
     *
     * @param sessionId 会话ID
     * @return 消息列表
     */
    List<ChatMessageDO> findBySessionIdOrderByCreateTimeAsc(String sessionId);

    /**
     * 根据会话ID删除所有消息
     *
     * @param sessionId 会话ID
     */
    void deleteBySessionId(String sessionId);

    /**
     * 统计会话的消息数量
     *
     * @param sessionId 会话ID
     * @return 消息数量
     */
    long countBySessionId(String sessionId);
}
