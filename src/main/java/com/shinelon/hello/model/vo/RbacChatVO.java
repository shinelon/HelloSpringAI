package com.shinelon.hello.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * RBAC对话响应VO
 *
 * @author shinelon
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RbacChatVO {
    
    private String content;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
