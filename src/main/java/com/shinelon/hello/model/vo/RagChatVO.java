package com.shinelon.hello.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * RAG 对话响应 VO
 *
 * @author shinelon
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RagChatVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * AI 回复内容
     */
    private String content;

    /**
     * 检索到的文档数量
     */
    private Integer sourceCount;

    /**
     * 文档来源列表
     */
    private List<String> sources;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
