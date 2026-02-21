package com.shinelon.hello.dao;

import com.shinelon.hello.model.entity.ChatSessionDO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 会话DAO接口
 *
 * @author shinelon
 */
@Repository
public interface ChatSessionDao extends JpaRepository<ChatSessionDO, Long> {

    /**
     * 根据会话ID查询会话
     *
     * @param sessionId 会话ID
     * @return 会话实体
     */
    Optional<ChatSessionDO> findBySessionId(String sessionId);

    /**
     * 检查会话是否存在
     *
     * @param sessionId 会话ID
     * @return 是否存在
     */
    boolean existsBySessionId(String sessionId);

    /**
     * 根据会话ID删除会话
     *
     * @param sessionId 会话ID
     */
    void deleteBySessionId(String sessionId);

    /**
     * 分页查询所有会话，按更新时间倒序
     *
     * @param pageable 分页参数
     * @return 会话分页列表
     */
    Page<ChatSessionDO> findAllByOrderByUpdateTimeDesc(Pageable pageable);
}
