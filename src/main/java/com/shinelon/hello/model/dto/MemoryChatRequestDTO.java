package com.shinelon.hello.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 带记忆的对话请求DTO
 *
 * @author shinelon
 */
@Data
public class MemoryChatRequestDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 会话ID（必填，用于标识对话上下文）
     */
    @NotBlank(message = "会话ID不能为空")
    private String conversationId;

    /**
     * 用户消息内容（必填）
     */
    @NotBlank(message = "消息内容不能为空")
    @Size(max = 4000, message = "消息内容不能超过4000字符")
    private String content;
}
