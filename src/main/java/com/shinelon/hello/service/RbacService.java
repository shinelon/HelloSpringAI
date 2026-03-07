package com.shinelon.hello.service;

import com.shinelon.hello.model.dto.RbacChatRequestDTO;
import com.shinelon.hello.model.vo.RbacChatVO;

/**
 * RBAC服务接口
 *
 * @author shinelon
 */
public interface RbacService {

    /**
     * RBAC对话
     *
     * @param request 对话请求
     * @return 对话响应
     */
    RbacChatVO chat(RbacChatRequestDTO request);
}
