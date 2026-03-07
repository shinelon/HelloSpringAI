package com.shinelon.hello.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 权限VO
 *
 * @author shinelon
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionVO {
    
    private Long permissionId;
    private String permissionName;
    private String permissionCode;
    private String resource;
    private String action;
}
