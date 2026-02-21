package com.shinelon.hello.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 消息实体
 *
 * @author shinelon
 */
@Data
@Entity
@Table(name = "chat_message", indexes = {
        @Index(name = "idx_msg_session_id", columnList = "session_id"),
        @Index(name = "idx_msg_create_time", columnList = "create_time")
})
public class ChatMessageDO {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 会话ID
     */
    @Column(name = "session_id", nullable = false, length = 36)
    private String sessionId;

    /**
     * 角色：user/assistant
     */
    @Column(nullable = false, length = 20)
    private String role;

    /**
     * 消息内容
     */
    @Lob
    @Column(nullable = false)
    private String content;

    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;
}
