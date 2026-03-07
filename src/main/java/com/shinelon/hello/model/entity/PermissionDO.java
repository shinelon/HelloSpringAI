package com.shinelon.hello.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 权限实体
 *
 * @author shinelon
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionDO {
    
    private Long permissionId;
    private String permissionName;
    private String permissionCode;
    private String resource;
    private String action;
}
