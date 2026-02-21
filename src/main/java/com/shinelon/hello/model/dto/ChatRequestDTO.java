package com.shinelon.hello.model.dto;

import com.shinelon.hello.common.constants.CommonConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 对话请求DTO
 *
 * @author shinelon
 */
@Data
public class ChatRequestDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 会话ID（可选，不传则创建新会话）
     */
    private String sessionId;

    /**
     * 用户消息内容（必填）
     */
    @NotBlank(message = "消息内容不能为空")
    @Size(max = CommonConstants.CONTENT_MAX_LENGTH, message = "消息内容不能超过" + CommonConstants.CONTENT_MAX_LENGTH + "字符")
    private String content;
}
