package com.shinelon.hello.controller;

import com.shinelon.hello.common.result.Result;
import com.shinelon.hello.common.utils.DesensitizationUtils;
import com.shinelon.hello.model.dto.RbacChatRequestDTO;
import com.shinelon.hello.model.vo.RbacChatVO;
import com.shinelon.hello.service.RbacService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * RBAC控制器
 *
 * @author shinelon
 */
@Slf4j
@RestController
@RequestMapping("/rbac")
@RequiredArgsConstructor
public class RbacController {

    private final RbacService rbacService;

    /**
     * RBAC对话接口
     *
     * @param request 对话请求
     * @return 对话响应
     */
    @PostMapping("/chat")
    public Result<RbacChatVO> chat(@Valid @RequestBody RbacChatRequestDTO request) {
        log.info("[chat] RBAC对话请求, content={}", 
                DesensitizationUtils.truncateAndMask(request.getContent(), 50));

        RbacChatVO response = rbacService.chat(request);
        
        log.info("[chat] RBAC对话成功, 响应长度={}", response.getContent().length());
        return Result.success(response);
    }
}
