package com.shinelon.hello.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 审批VO
 *
 * @author shinelon
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalVO {
    
    private String approvalId;
    private String status;
    private String message;
    private String applicant;
    private String appliedRole;
    private String submitTime;
}
