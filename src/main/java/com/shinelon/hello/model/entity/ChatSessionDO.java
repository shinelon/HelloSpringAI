package com.shinelon.hello.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 会话实体
 *
 * @author shinelon
 */
@Data
@Entity
@Table(name = "chat_session", indexes = {
        @Index(name = "idx_session_id", columnList = "session_id", unique = true)
})
public class ChatSessionDO {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 会话UUID
     */
    @Column(name = "session_id", nullable = false, unique = true, length = 36)
    private String sessionId;

    /**
     * 会话标题（首条消息摘要）
     */
    @Column(length = 100)
    private String title;

    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @UpdateTimestamp
    @Column(name = "update_time", nullable = false)
    private LocalDateTime updateTime;
}
