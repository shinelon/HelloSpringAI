package com.shinelon.hello.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 角色实体
 *
 * @author shinelon
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleDO {
    
    private Long roleId;
    private String roleName;
    private String roleCode;
    private String description;
    private List<Long> permissionIds;
}
