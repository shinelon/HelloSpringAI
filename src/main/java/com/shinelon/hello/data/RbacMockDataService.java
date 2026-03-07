package com.shinelon.hello.data;

import com.shinelon.hello.model.entity.PermissionDO;
import com.shinelon.hello.model.entity.RoleDO;
import com.shinelon.hello.model.entity.UserDO;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
public class RbacMockDataService {

    private final Map<Long, UserDO> userMap = new ConcurrentHashMap<>();
    private final Map<Long, RoleDO> roleMap = new ConcurrentHashMap<>();
    private final Map<Long, PermissionDO> permissionMap = new ConcurrentHashMap<>();
    
    private final AtomicLong approvalIdCounter = new AtomicLong(0);

    @PostConstruct
    public void init() {
        log.info("[RbacMockDataService] 开始初始化Mock数据");
        initPermissions();
        initRoles();
        initUsers();
        log.info("[RbacMockDataService] Mock数据初始化完成 - 用户:{}, 角色:{}, 权限:{}", 
            userMap.size(), roleMap.size(), permissionMap.size());
    }

    private void initPermissions() {
        List<PermissionDO> permissions = Arrays.asList(
            PermissionDO.builder()
                .permissionId(1L)
                .permissionName("用户查看")
                .permissionCode("USER_READ")
                .resource("/api/users")
                .action("GET")
                .build(),
            PermissionDO.builder()
                .permissionId(2L)
                .permissionName("用户编辑")
                .permissionCode("USER_WRITE")
                .resource("/api/users")
                .action("POST,PUT")
                .build(),
            PermissionDO.builder()
                .permissionId(3L)
                .permissionName("用户删除")
                .permissionCode("USER_DELETE")
                .resource("/api/users")
                .action("DELETE")
                .build(),
            PermissionDO.builder()
                .permissionId(4L)
                .permissionName("角色查看")
                .permissionCode("ROLE_READ")
                .resource("/api/roles")
                .action("GET")
                .build(),
            PermissionDO.builder()
                .permissionId(5L)
                .permissionName("角色编辑")
                .permissionCode("ROLE_WRITE")
                .resource("/api/roles")
                .action("POST,PUT")
                .build(),
            PermissionDO.builder()
                .permissionId(6L)
                .permissionName("权限查看")
                .permissionCode("PERMISSION_READ")
                .resource("/api/permissions")
                .action("GET")
                .build(),
            PermissionDO.builder()
                .permissionId(7L)
                .permissionName("权限编辑")
                .permissionCode("PERMISSION_WRITE")
                .resource("/api/permissions")
                .action("POST,PUT")
                .build(),
            PermissionDO.builder()
                .permissionId(8L)
                .permissionName("审批查看")
                .permissionCode("APPROVAL_READ")
                .resource("/api/approvals")
                .action("GET")
                .build(),
            PermissionDO.builder()
                .permissionId(9L)
                .permissionName("审批处理")
                .permissionCode("APPROVAL_WRITE")
                .resource("/api/approvals")
                .action("POST,PUT")
                .build(),
            PermissionDO.builder()
                .permissionId(10L)
                .permissionName("日志查看")
                .permissionCode("LOG_READ")
                .resource("/api/logs")
                .action("GET")
                .build()
        );

        permissions.forEach(p -> permissionMap.put(p.getPermissionId(), p));
    }

    private void initRoles() {
        List<RoleDO> roles = Arrays.asList(
            RoleDO.builder()
                .roleId(1L)
                .roleName("管理员")
                .roleCode("ADMIN")
                .description("系统管理员，拥有所有权限")
                .permissionIds(Arrays.asList(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L))
                .build(),
            RoleDO.builder()
                .roleId(2L)
                .roleName("普通用户")
                .roleCode("USER")
                .description("普通用户，拥有基础权限")
                .permissionIds(Arrays.asList(1L, 4L, 8L, 10L))
                .build(),
            RoleDO.builder()
                .roleId(3L)
                .roleName("审计员")
                .roleCode("AUDITOR")
                .description("审计员，拥有查看和审批权限")
                .permissionIds(Arrays.asList(10L, 8L, 9L, 1L, 4L))
                .build()
        );

        roles.forEach(r -> roleMap.put(r.getRoleId(), r));
    }

    private void initUsers() {
        List<UserDO> users = Arrays.asList(
            UserDO.builder()
                .userId(1L)
                .name("张三")
                .phone("13800138001")
                .email("zhangsan@example.com")
                .company("技术部")
                .roleIds(Collections.singletonList(1L))
                .build(),
            UserDO.builder()
                .userId(2L)
                .name("李四")
                .phone("13800138002")
                .email("lisi@example.com")
                .company("市场部")
                .roleIds(Collections.singletonList(2L))
                .build(),
            UserDO.builder()
                .userId(3L)
                .name("王五")
                .phone("13800138003")
                .email("wangwu@example.com")
                .company("财务部")
                .roleIds(Collections.singletonList(3L))
                .build(),
            UserDO.builder()
                .userId(4L)
                .name("赵六")
                .phone("13800138004")
                .email("zhaoliu@example.com")
                .company("人事部")
                .roleIds(Collections.singletonList(2L))
                .build(),
            UserDO.builder()
                .userId(5L)
                .name("孙七")
                .phone("13800138005")
                .email("sunqi@example.com")
                .company("审计部")
                .roleIds(Collections.singletonList(3L))
                .build()
        );

        users.forEach(u -> userMap.put(u.getUserId(), u));
    }

    public Optional<UserDO> findUserByPhone(String phone) {
        log.debug("[findUserByPhone] phone={}", phone);
        return userMap.values().stream()
            .filter(user -> phone.equals(user.getPhone()))
            .findFirst();
    }

    public Optional<RoleDO> findRoleByIdentifier(String identifier) {
        log.debug("[findRoleByIdentifier] identifier={}", identifier);
        if (identifier == null || identifier.trim().isEmpty()) {
            return Optional.empty();
        }
        
        String normalizedIdentifier = identifier.trim();
        return roleMap.values().stream()
            .filter(role -> normalizedIdentifier.equalsIgnoreCase(role.getRoleName()) 
                || normalizedIdentifier.equalsIgnoreCase(role.getRoleCode()))
            .findFirst();
    }

    public Optional<PermissionDO> findPermissionByIdentifier(String identifier) {
        log.debug("[findPermissionByIdentifier] identifier={}", identifier);
        if (identifier == null || identifier.trim().isEmpty()) {
            return Optional.empty();
        }
        
        String normalizedIdentifier = identifier.trim();
        return permissionMap.values().stream()
            .filter(permission -> normalizedIdentifier.equalsIgnoreCase(permission.getPermissionName()) 
                || normalizedIdentifier.equalsIgnoreCase(permission.getPermissionCode()))
            .findFirst();
    }

    public List<PermissionDO> findPermissionsByRoleId(Long roleId) {
        log.debug("[findPermissionsByRoleId] roleId={}", roleId);
        if (roleId == null) {
            return Collections.emptyList();
        }
        
        RoleDO role = roleMap.get(roleId);
        if (role == null || role.getPermissionIds() == null) {
            return Collections.emptyList();
        }
        
        return role.getPermissionIds().stream()
            .map(permissionMap::get)
            .filter(Objects::nonNull)
            .toList();
    }

    public List<RoleDO> findRolesByPermissionId(Long permissionId) {
        log.debug("[findRolesByPermissionId] permissionId={}", permissionId);
        if (permissionId == null) {
            return Collections.emptyList();
        }
        
        return roleMap.values().stream()
            .filter(role -> role.getPermissionIds() != null 
                && role.getPermissionIds().contains(permissionId))
            .toList();
    }

    public Optional<RoleDO> findRoleById(Long roleId) {
        log.debug("[findRoleById] roleId={}", roleId);
        if (roleId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(roleMap.get(roleId));
    }

    public String generateApprovalId() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        long sequence = approvalIdCounter.incrementAndGet();
        String approvalId = String.format("APV-%s-%04d", timestamp, sequence);
        log.debug("[generateApprovalId] 生成审批ID: {}", approvalId);
        return approvalId;
    }
}
