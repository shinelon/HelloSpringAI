package com.shinelon.hello.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 用户信息VO
 *
 * @author shinelon
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoVO {
    
    private Long userId;
    private String name;
    private String phone;
    private String email;
    private String company;
    private List<RoleWithPermissionsVO> roles;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoleWithPermissionsVO {
        private Long roleId;
        private String roleName;
        private String roleCode;
        private List<PermissionSimpleVO> permissions;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PermissionSimpleVO {
        private Long permissionId;
        private String permissionName;
        private String permissionCode;
    }
}
