package com.shinelon.hello.tool;

import com.shinelon.hello.data.RbacMockDataService;
import com.shinelon.hello.model.vo.ApprovalVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 审批提交工具
 * 提交角色或权限申请的审批请求
 *
 * @author shinelon
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ApprovalSubmitTool {

    private final RbacMockDataService rbacMockDataService;
    
    private static final DateTimeFormatter DATE_TIME_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 提交审批申请
     *
     * @param applicant 申请人
     * @param appliedRole 申请的角色
     * @param reason 申请原因
     * @return 审批信息
     */
    @Tool(description = "提交角色或权限申请的审批请求，返回审批ID和状态信息")
    public ApprovalVO submitApproval(
            @ToolParam(description = "申请人姓名") String applicant,
            @ToolParam(description = "申请的角色名称或代码，如：管理员、ADMIN、普通用户、USER") String appliedRole,
            @ToolParam(description = "申请原因和理由") String reason) {
        
        log.info("[submitApproval] 开始提交审批, applicant={}, appliedRole={}, reason={}", 
            applicant, appliedRole, reason);
        
        String approvalId = rbacMockDataService.generateApprovalId();
        String submitTime = LocalDateTime.now().format(DATE_TIME_FORMATTER);
        String message = buildApprovalMessage(applicant, appliedRole, reason);
        
        ApprovalVO approval = ApprovalVO.builder()
            .approvalId(approvalId)
            .status("待审批")
            .message(message)
            .applicant(applicant)
            .appliedRole(appliedRole)
            .submitTime(submitTime)
            .build();
        
        log.info("[submitApproval] 审批提交成功, approvalId={}, applicant={}", 
            approvalId, applicant);
        
        return approval;
    }

    private String buildApprovalMessage(String applicant, String appliedRole, String reason) {
        StringBuilder message = new StringBuilder();
        message.append("审批申请已提交");
        
        if (applicant != null && !applicant.trim().isEmpty()) {
            message.append("，申请人：").append(applicant);
        }
        
        if (appliedRole != null && !appliedRole.trim().isEmpty()) {
            message.append("，申请角色：").append(appliedRole);
        }
        
        if (reason != null && !reason.trim().isEmpty()) {
            message.append("，申请原因：").append(reason);
        }
        
        return message.toString();
    }
}
