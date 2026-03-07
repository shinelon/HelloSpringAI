package com.shinelon.hello.tool;

import com.shinelon.hello.data.RbacMockDataService;
import com.shinelon.hello.model.entity.PermissionDO;
import com.shinelon.hello.model.entity.RoleDO;
import com.shinelon.hello.model.vo.RoleVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * 权限角色查询工具
 * 根据权限名称或代码查询拥有该权限的所有角色
 *
 * @author shinelon
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PermissionRoleQueryTool {

    private final RbacMockDataService rbacMockDataService;

    /**
     * 根据权限查询角色列表
     *
     * @param permissionIdentifier 权限名称或权限代码
     * @return 角色列表
     */
    @Tool(description = "根据权限名称或代码查询拥有该权限的所有角色列表")
    public List<RoleVO> queryRolesByPermission(
            @ToolParam(description = "权限名称或权限代码，如：用户查看、USER_READ、用户编辑、USER_WRITE") String permissionIdentifier) {
        
        log.info("[queryRolesByPermission] 开始查询权限对应的角色, permissionIdentifier={}", permissionIdentifier);
        
        if (permissionIdentifier == null || permissionIdentifier.trim().isEmpty()) {
            log.warn("[queryRolesByPermission] 权限标识为空");
            return Collections.emptyList();
        }

        Optional<PermissionDO> permissionOpt = rbacMockDataService.findPermissionByIdentifier(permissionIdentifier.trim());
        if (permissionOpt.isEmpty()) {
            log.warn("[queryRolesByPermission] 未找到权限, permissionIdentifier={}", permissionIdentifier);
            return Collections.emptyList();
        }

        PermissionDO permission = permissionOpt.get();
        List<RoleDO> roles = rbacMockDataService.findRolesByPermissionId(permission.getPermissionId());
        
        List<RoleVO> roleVOs = roles.stream()
            .map(this::buildRoleVO)
            .toList();
        
        log.info("[queryRolesByPermission] 查询成功, permissionName={}, roleCount={}", 
            permission.getPermissionName(), roleVOs.size());
        
        return roleVOs;
    }

    private RoleVO buildRoleVO(RoleDO role) {
        return RoleVO.builder()
            .roleId(role.getRoleId())
            .roleName(role.getRoleName())
            .roleCode(role.getRoleCode())
            .description(role.getDescription())
            .build();
    }
}
