package com.shinelon.hello.tool;

import com.shinelon.hello.data.RbacMockDataService;
import com.shinelon.hello.model.entity.PermissionDO;
import com.shinelon.hello.model.entity.RoleDO;
import com.shinelon.hello.model.vo.PermissionVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * 角色权限查询工具
 * 根据角色名称或代码查询该角色的所有权限
 *
 * @author shinelon
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RolePermissionQueryTool {

    private final RbacMockDataService rbacMockDataService;

    /**
     * 根据角色查询权限列表
     *
     * @param roleIdentifier 角色名称或角色代码
     * @return 权限列表
     */
    @Tool(description = "根据角色名称或代码查询该角色拥有的所有权限列表")
    public List<PermissionVO> queryPermissionsByRole(
            @ToolParam(description = "角色名称或角色代码，如：管理员、ADMIN、普通用户、USER") String roleIdentifier) {
        
        log.info("[queryPermissionsByRole] 开始查询角色权限, roleIdentifier={}", roleIdentifier);
        
        if (roleIdentifier == null || roleIdentifier.trim().isEmpty()) {
            log.warn("[queryPermissionsByRole] 角色标识为空");
            return Collections.emptyList();
        }

        Optional<RoleDO> roleOpt = rbacMockDataService.findRoleByIdentifier(roleIdentifier.trim());
        if (roleOpt.isEmpty()) {
            log.warn("[queryPermissionsByRole] 未找到角色, roleIdentifier={}", roleIdentifier);
            return Collections.emptyList();
        }

        RoleDO role = roleOpt.get();
        List<PermissionDO> permissions = rbacMockDataService.findPermissionsByRoleId(role.getRoleId());
        
        List<PermissionVO> permissionVOs = permissions.stream()
            .map(this::buildPermissionVO)
            .toList();
        
        log.info("[queryPermissionsByRole] 查询成功, roleName={}, permissionCount={}", 
            role.getRoleName(), permissionVOs.size());
        
        return permissionVOs;
    }

    private PermissionVO buildPermissionVO(PermissionDO permission) {
        return PermissionVO.builder()
            .permissionId(permission.getPermissionId())
            .permissionName(permission.getPermissionName())
            .permissionCode(permission.getPermissionCode())
            .resource(permission.getResource())
            .action(permission.getAction())
            .build();
    }
}
