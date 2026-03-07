package com.shinelon.hello.service.impl;

import com.shinelon.hello.common.enums.ErrorCodeEnum;
import com.shinelon.hello.common.exception.BusinessException;
import com.shinelon.hello.common.utils.DesensitizationUtils;
import com.shinelon.hello.manager.RbacChatManager;
import com.shinelon.hello.model.dto.RbacChatRequestDTO;
import com.shinelon.hello.model.vo.RbacChatVO;
import com.shinelon.hello.service.RbacService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * RBAC服务实现
 *
 * @author shinelon
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RbacServiceImpl implements RbacService {

    private final RbacChatManager rbacChatManager;

    private static final String GREETING_RESPONSE = "你好！我是RBAC系统助手，可以帮助您查询用户信息、角色权限、提交审批等。请问有什么可以帮您的吗？";

    private static final Set<String> GREETING_KEYWORDS = Set.of(
            "你好", "您好", "hi", "hello", "hey", "嗨", "哈喽", "早上好", "下午好", "晚上好"
    );

    @Override
    public RbacChatVO chat(RbacChatRequestDTO request) {
        validateRequest(request);

        String content = request.getContent().trim();
        log.info("[chat] RBAC对话请求开始, content={}", DesensitizationUtils.truncateAndMask(content, 50));

        String response;
        if (isGreeting(content)) {
            log.info("[chat] 检测到问候语，返回默认回复");
            response = GREETING_RESPONSE;
        } else {
            log.debug("[chat] 调用AI对话");
            response = rbacChatManager.chat(content);
        }

        log.info("[chat] RBAC对话请求完成, 响应长度={}", response.length());

        return RbacChatVO.builder()
                .content(response)
                .createTime(LocalDateTime.now())
                .build();
    }

    private void validateRequest(RbacChatRequestDTO request) {
        if (request == null) {
            throw new BusinessException(ErrorCodeEnum.PARAM_ERROR, "请求不能为空");
        }
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new BusinessException(ErrorCodeEnum.PARAM_ERROR, "消息内容不能为空");
        }
    }

    private boolean isGreeting(String content) {
        String lowerContent = content.toLowerCase().trim();
        return GREETING_KEYWORDS.stream()
                .anyMatch(keyword -> lowerContent.contains(keyword.toLowerCase()));
    }
}
