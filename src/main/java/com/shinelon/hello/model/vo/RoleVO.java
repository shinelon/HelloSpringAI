package com.shinelon.hello.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 角色VO
 *
 * @author shinelon
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleVO {
    
    private Long roleId;
    private String roleName;
    private String roleCode;
    private String description;
}
