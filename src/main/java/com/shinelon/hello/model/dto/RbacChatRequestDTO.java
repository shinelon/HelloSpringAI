package com.shinelon.hello.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * RBAC对话请求DTO
 *
 * @author shinelon
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RbacChatRequestDTO {

    /**
     * 消息内容（必填）
     */
    @NotBlank(message = "消息内容不能为空")
    @Size(max = 4000, message = "消息内容不能超过4000字符")
    private String content;
}
