package com.shinelon.hello.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * RAG 对话请求 DTO
 *
 * @author shinelon
 */
@Data
public class RagChatRequestDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户查询内容
     */
    @NotBlank(message = "查询内容不能为空")
    @Size(max = 2000, message = "查询内容不能超过2000字符")
    private String query;
}
