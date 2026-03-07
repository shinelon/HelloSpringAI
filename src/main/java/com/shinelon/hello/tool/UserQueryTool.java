package com.shinelon.hello.tool;

import com.shinelon.hello.data.RbacMockDataService;
import com.shinelon.hello.model.entity.PermissionDO;
import com.shinelon.hello.model.entity.RoleDO;
import com.shinelon.hello.model.entity.UserDO;
import com.shinelon.hello.model.vo.UserInfoVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * 用户查询工具
 * 根据手机号查询用户信息，包括角色和权限
 *
 * @author shinelon
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserQueryTool {

    private final RbacMockDataService rbacMockDataService;

    /**
     * 根据手机号查询用户信息
     *
     * @param phone 手机号
     * @return 用户信息，包含角色和权限
     */
    @Tool(description = "根据手机号查询用户信息，返回用户的详细信息包括角色和权限列表")
    public UserInfoVO queryUserInfo(
            @ToolParam(description = "用户手机号，11位数字") String phone) {
        
        log.info("[queryUserInfo] 开始查询用户信息, phone={}", phone);
        
        if (phone == null || phone.trim().isEmpty()) {
            log.warn("[queryUserInfo] 手机号为空");
            return null;
        }

        Optional<UserDO> userOpt = rbacMockDataService.findUserByPhone(phone.trim());
        if (userOpt.isEmpty()) {
            log.warn("[queryUserInfo] 未找到手机号对应的用户, phone={}", phone);
            return null;
        }

        UserDO user = userOpt.get();
        UserInfoVO userInfo = buildUserInfoVO(user);
        
        log.info("[queryUserInfo] 查询成功, userId={}, name={}, roleCount={}", 
            userInfo.getUserId(), userInfo.getName(), userInfo.getRoles().size());
        
        return userInfo;
    }

    private UserInfoVO buildUserInfoVO(UserDO user) {
        List<UserInfoVO.RoleWithPermissionsVO> roles = buildRolesWithPermissions(user.getRoleIds());
        
        return UserInfoVO.builder()
            .userId(user.getUserId())
            .name(user.getName())
            .phone(user.getPhone())
            .email(user.getEmail())
            .company(user.getCompany())
            .roles(roles)
            .build();
    }

    private List<UserInfoVO.RoleWithPermissionsVO> buildRolesWithPermissions(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return Collections.emptyList();
        }

        return roleIds.stream()
            .map(this::buildRoleWithPermissions)
            .toList();
    }

    private UserInfoVO.RoleWithPermissionsVO buildRoleWithPermissions(Long roleId) {
        Optional<RoleDO> roleOpt = rbacMockDataService.findRoleById(roleId);
        if (roleOpt.isEmpty()) {
            return null;
        }

        RoleDO role = roleOpt.get();
        List<PermissionDO> permissions = rbacMockDataService.findPermissionsByRoleId(roleId);
        
        List<UserInfoVO.PermissionSimpleVO> permissionVOs = permissions.stream()
            .map(this::buildPermissionSimpleVO)
            .toList();

        return UserInfoVO.RoleWithPermissionsVO.builder()
            .roleId(role.getRoleId())
            .roleName(role.getRoleName())
            .roleCode(role.getRoleCode())
            .permissions(permissionVOs)
            .build();
    }

    private UserInfoVO.PermissionSimpleVO buildPermissionSimpleVO(PermissionDO permission) {
        return UserInfoVO.PermissionSimpleVO.builder()
            .permissionId(permission.getPermissionId())
            .permissionName(permission.getPermissionName())
            .permissionCode(permission.getPermissionCode())
            .build();
    }
}
