package com.shinelon.hello.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 审批状态枚举
 *
 * @author shinelon
 */
@Getter
@AllArgsConstructor
public enum ApprovalStatusEnum {

    /**
     * 待审批
     */
    PENDING("PENDING", "待审批"),

    /**
     * 已通过
     */
    APPROVED("APPROVED", "已通过"),

    /**
     * 已拒绝
     */
    REJECTED("REJECTED", "已拒绝");

    private final String code;
    private final String desc;
}
