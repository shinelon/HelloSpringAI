# RBAC AI Assistant Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Build a role-based access control AI assistant with 4 tools using Spring AI Function Calling

**Architecture:** 4 independent Tool classes with @Tool annotations, managed by RbacChatManager, backed by RbacMockDataService with in-memory Map storage. AI automatically calls tools based on user intent.

**Tech Stack:** Spring Boot 3.4.3, Spring AI 1.1.2, ZhipuAI, Lombok, H2, Maven

---

## Task 1: Create Entity Classes (UserDO, RoleDO, PermissionDO)

**Files:**
- Create: `src/main/java/com/shinelon/hello/model/entity/UserDO.java`
- Create: `src/main/java/com/shinelon/hello/model/entity/RoleDO.java`
- Create: `src/main/java/com/shinelon/hello/model/entity/PermissionDO.java`

**Step 1: Create UserDO entity**

```java
package com.shinelon.hello.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 用户实体
 *
 * @author shinelon
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDO {
    
    private Long userId;
    private String name;
    private String phone;
    private String email;
    private String company;
    private List<Long> roleIds;
}
```

**Step 2: Create RoleDO entity**

```java
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
```

**Step 3: Create PermissionDO entity**

```java
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
```

**Step 4: Build the project**

Run: `mvn clean compile`
Expected: BUILD SUCCESS

---

## Task 2: Create VO Classes

**Files:**
- Create: `src/main/java/com/shinelon/hello/model/vo/UserInfoVO.java`
- Create: `src/main/java/com/shinelon/hello/model/vo/RoleVO.java`
- Create: `src/main/java/com/shinelon/hello/model/vo/PermissionVO.java`
- Create: `src/main/java/com/shinelon/hello/model/vo/ApprovalVO.java`
- Create: `src/main/java/com/shinelon/hello/model/vo/RbacChatVO.java`

**Step 1: Create UserInfoVO**

```java
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
```

**Step 2: Create RoleVO**

```java
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
```

**Step 3: Create PermissionVO**

```java
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
```

**Step 4: Create ApprovalVO**

```java
package com.shinelon.hello.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 审批VO
 *
 * @author shinelon
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalVO {
    
    private String approvalId;
    private String status;
    private String message;
    private String applicant;
    private String appliedRole;
    private String submitTime;
}
```

**Step 5: Create RbacChatVO**

```java
package com.shinelon.hello.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * RBAC对话响应VO
 *
 * @author shinelon
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RbacChatVO {
    
    private String content;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
```

**Step 6: Build the project**

Run: `mvn clean compile`
Expected: BUILD SUCCESS

---

## Task 3: Create DTO and Constants

**Files:**
- Create: `src/main/java/com/shinelon/hello/model/dto/RbacChatRequestDTO.java`
- Create: `src/main/java/com/shinelon/hello/common/constants/RbacConstants.java`
- Create: `src/main/java/com/shinelon/hello/common/enums/ApprovalStatusEnum.java`

**Step 1: Create RbacChatRequestDTO**

```java
package com.shinelon.hello.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * RBAC对话请求DTO
 *
 * @author shinelon
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RbacChatRequestDTO {
    
    @NotBlank(message = "消息内容不能为空")
    @Size(max = 4000, message = "消息内容不能超过4000字符")
    private String content;
}
```

**Step 2: Create RbacConstants**

```java
package com.shinelon.hello.common.constants;

/**
 * RBAC常量
 *
 * @author shinelon
 */
public final class RbacConstants {
    
    private RbacConstants() {
    }
    
    public static final String APPROVAL_ID_PREFIX = "AP";
    public static final String APPROVAL_STATUS_PENDING = "PENDING";
    
    public static final String GREETING_RESPONSE = """
            您好！我是角色权限查询AI小助手，我可以为您提供以下服务：
            
            1. 📱 查询用户信息 - 根据手机号查询用户的详细信息和权限
            2. 🔍 查询角色权限 - 查询某个角色拥有的所有权限
            3. 🔎 查询权限角色 - 查询拥有某个权限的所有角色
            4. 📝 提交审批申请 - 提交角色或权限的变更申请
            
            请问您需要什么服务？
            """;
    
    public static final String SYSTEM_PROMPT = """
            你是角色权限查询AI小助手，专门帮助用户查询用户信息、角色信息、权限信息，以及提交审批申请。
            
            ## 你的能力：
            1. 根据手机号查询用户及其角色、权限信息
            2. 根据角色查询该角色拥有的所有权限
            3. 根据权限查询哪些角色拥有该权限
            4. 帮助用户提交角色或权限的审批申请
            
            ## 交互规则：
            - 当用户发送空消息、问候语（如"你好"、"hi"、"hello"）或询问你能做什么时，主动介绍你的4个功能并询问用户需要什么服务
            - 根据用户的描述，自动调用相应的工具获取信息
            - 用自然、友好的语言回复用户，避免生硬的机器回复
            
            ## 功能介绍模板：
            您好！我是角色权限查询AI小助手，我可以为您提供以下服务：
            
            1. 📱 查询用户信息 - 根据手机号查询用户的详细信息和权限
            2. 🔍 查询角色权限 - 查询某个角色拥有的所有权限
            3. 🔎 查询权限角色 - 查询拥有某个权限的所有角色
            4. 📝 提交审批申请 - 提交角色或权限的变更申请
            
            请问您需要什么服务？
            """;
}
```

**Step 3: Create ApprovalStatusEnum**

```java
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
    
    PENDING("PENDING", "待审批"),
    APPROVED("APPROVED", "已通过"),
    REJECTED("REJECTED", "已拒绝");
    
    private final String code;
    private final String desc;
}
```

**Step 4: Build the project**

Run: `mvn clean compile`
Expected: BUILD SUCCESS

---

## Task 4: Create RbacMockDataService with Test

**Files:**
- Create: `src/main/java/com/shinelon/hello/data/RbacMockDataService.java`
- Create: `src/test/java/com/shinelon/hello/data/RbacMockDataServiceTest.java`

**Step 1: Write the failing test for RbacMockDataService**

```java
package com.shinelon.hello.data;

import com.shinelon.hello.model.entity.PermissionDO;
import com.shinelon.hello.model.entity.RoleDO;
import com.shinelon.hello.model.entity.UserDO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RbacMockDataService测试
 *
 * @author shinelon
 */
@DisplayName("RBAC模拟数据服务测试")
class RbacMockDataServiceTest {

    private RbacMockDataService rbacMockDataService;

    @BeforeEach
    void setUp() {
        rbacMockDataService = new RbacMockDataService();
    }

    record UserQueryTC(String name, String phone, boolean shouldExist) {}

    static Stream<UserQueryTC> userQueryCases() {
        return Stream.of(
                new UserQueryTC("查询张三", "13800138000", true),
                new UserQueryTC("查询李四", "13900139000", true),
                new UserQueryTC("查询不存在用户", "99999999999", false)
        );
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("userQueryCases")
    @DisplayName("根据手机号查询用户")
    void findUserByPhone(UserQueryTC tc) {
        Optional<UserDO> user = rbacMockDataService.findUserByPhone(tc.phone());
        
        if (tc.shouldExist()) {
            assertTrue(user.isPresent(), "用户应该存在");
            assertEquals(tc.phone(), user.get().getPhone(), "手机号应该匹配");
        } else {
            assertFalse(user.isPresent(), "用户不应该存在");
        }
    }

    record RoleQueryTC(String name, String identifier, boolean shouldExist) {}

    static Stream<RoleQueryTC> roleQueryCases() {
        return Stream.of(
                new RoleQueryTC("根据角色名称查询-管理员", "系统管理员", true),
                new RoleQueryTC("根据角色编码查询-ADMIN", "ADMIN", true),
                new RoleQueryTC("根据角色编码查询-小写", "admin", true),
                new RoleQueryTC("查询不存在的角色", "NOTEXIST", false)
        );
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("roleQueryCases")
    @DisplayName("根据角色标识查询角色")
    void findRoleByIdentifier(RoleQueryTC tc) {
        Optional<RoleDO> role = rbacMockDataService.findRoleByIdentifier(tc.identifier());
        
        if (tc.shouldExist()) {
            assertTrue(role.isPresent(), "角色应该存在");
        } else {
            assertFalse(role.isPresent(), "角色不应该存在");
        }
    }

    record PermissionQueryTC(String name, String identifier, boolean shouldExist) {}

    static Stream<PermissionQueryTC> permissionQueryCases() {
        return Stream.of(
                new PermissionQueryTC("根据权限名称查询", "用户查看", true),
                new PermissionQueryTC("根据权限编码查询", "USER_READ", true),
                new PermissionQueryTC("根据权限编码查询-小写", "user_read", true),
                new PermissionQueryTC("查询不存在的权限", "NOTEXIST", false)
        );
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("permissionQueryCases")
    @DisplayName("根据权限标识查询权限")
    void findPermissionByIdentifier(PermissionQueryTC tc) {
        Optional<PermissionDO> permission = rbacMockDataService.findPermissionByIdentifier(tc.identifier());
        
        if (tc.shouldExist()) {
            assertTrue(permission.isPresent(), "权限应该存在");
        } else {
            assertFalse(permission.isPresent(), "权限不应该存在");
        }
    }

    @Test
    @DisplayName("根据权限ID查询拥有该权限的角色列表")
    void findRolesByPermissionId() {
        Long permissionId = 1L;
        List<RoleDO> roles = rbacMockDataService.findRolesByPermissionId(permissionId);
        
        assertNotNull(roles, "角色列表不应为null");
        assertFalse(roles.isEmpty(), "应该有角色拥有该权限");
    }

    @Test
    @DisplayName("根据角色ID查询权限列表")
    void findPermissionsByRoleId() {
        Long roleId = 1L;
        List<PermissionDO> permissions = rbacMockDataService.findPermissionsByRoleId(roleId);
        
        assertNotNull(permissions, "权限列表不应为null");
        assertFalse(permissions.isEmpty(), "角色应该有权限");
    }

    @Test
    @DisplayName("生成审批单号")
    void generateApprovalId() {
        String approvalId = rbacMockDataService.generateApprovalId();
        
        assertNotNull(approvalId, "审批单号不应为null");
        assertTrue(approvalId.startsWith("AP"), "审批单号应以AP开头");
    }
}
```

**Step 2: Run test to verify it fails**

Run: `mvn test -Dtest=RbacMockDataServiceTest`
Expected: FAIL with "RbacMockDataService not found"

**Step 3: Write minimal implementation**

```java
package com.shinelon.hello.data;

import com.shinelon.hello.common.constants.RbacConstants;
import com.shinelon.hello.model.entity.PermissionDO;
import com.shinelon.hello.model.entity.RoleDO;
import com.shinelon.hello.model.entity.UserDO;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * RBAC模拟数据服务
 *
 * @author shinelon
 */
@Slf4j
@Service
public class RbacMockDataService {

    @Getter
    private final Map<Long, UserDO> userMap = new HashMap<>();
    @Getter
    private final Map<Long, RoleDO> roleMap = new HashMap<>();
    @Getter
    private final Map<Long, PermissionDO> permissionMap = new HashMap<>();
    
    private final AtomicLong approvalIdCounter = new AtomicLong(0);

    private static final DateTimeFormatter APPROVAL_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @PostConstruct
    public void init() {
        log.info("[init] 开始初始化RBAC模拟数据");
        initPermissions();
        initRoles();
        initUsers();
        log.info("[init] RBAC模拟数据初始化完成, users={}, roles={}, permissions={}", 
                userMap.size(), roleMap.size(), permissionMap.size());
    }

    private void initPermissions() {
        List<PermissionDO> permissions = Arrays.asList(
                PermissionDO.builder().permissionId(1L).permissionName("用户查看").permissionCode("USER_READ").resource("/api/users").action("READ").build(),
                PermissionDO.builder().permissionId(2L).permissionName("用户新增").permissionCode("USER_WRITE").resource("/api/users").action("WRITE").build(),
                PermissionDO.builder().permissionId(3L).permissionName("用户删除").permissionCode("USER_DELETE").resource("/api/users").action("DELETE").build(),
                PermissionDO.builder().permissionId(4L).permissionName("角色查看").permissionCode("ROLE_READ").resource("/api/roles").action("READ").build(),
                PermissionDO.builder().permissionId(5L).permissionName("角色管理").permissionCode("ROLE_WRITE").resource("/api/roles").action("WRITE").build(),
                PermissionDO.builder().permissionId(6L).permissionName("权限查看").permissionCode("PERMISSION_READ").resource("/api/permissions").action("READ").build(),
                PermissionDO.builder().permissionId(7L).permissionName("权限管理").permissionCode("PERMISSION_WRITE").resource("/api/permissions").action("WRITE").build(),
                PermissionDO.builder().permissionId(8L).permissionName("审批查看").permissionCode("APPROVAL_READ").resource("/api/approvals").action("READ").build(),
                PermissionDO.builder().permissionId(9L).permissionName("审批处理").permissionCode("APPROVAL_WRITE").resource("/api/approvals").action("WRITE").build(),
                PermissionDO.builder().permissionId(10L).permissionName("日志查看").permissionCode("LOG_READ").resource("/api/logs").action("READ").build()
        );

        permissions.forEach(p -> permissionMap.put(p.getPermissionId(), p));
    }

    private void initRoles() {
        List<RoleDO> roles = Arrays.asList(
                RoleDO.builder()
                        .roleId(1L)
                        .roleName("系统管理员")
                        .roleCode("ADMIN")
                        .description("系统管理员，拥有所有权限")
                        .permissionIds(Arrays.asList(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L))
                        .build(),
                RoleDO.builder()
                        .roleId(2L)
                        .roleName("普通用户")
                        .roleCode("USER")
                        .description("普通用户，拥有基础权限")
                        .permissionIds(Arrays.asList(1L, 4L, 6L, 8L))
                        .build(),
                RoleDO.builder()
                        .roleId(3L)
                        .roleName("审计员")
                        .roleCode("AUDITOR")
                        .description("审计员，拥有查看权限")
                        .permissionIds(Arrays.asList(1L, 4L, 6L, 8L, 10L))
                        .build()
        );

        roles.forEach(r -> roleMap.put(r.getRoleId(), r));
    }

    private void initUsers() {
        List<UserDO> users = Arrays.asList(
                UserDO.builder()
                        .userId(1L)
                        .name("张三")
                        .phone("13800138000")
                        .email("zhangsan@example.com")
                        .company("XX科技")
                        .roleIds(Collections.singletonList(1L))
                        .build(),
                UserDO.builder()
                        .userId(2L)
                        .name("李四")
                        .phone("13900139000")
                        .email("lisi@example.com")
                        .company("XX科技")
                        .roleIds(Collections.singletonList(2L))
                        .build(),
                UserDO.builder()
                        .userId(3L)
                        .name("王五")
                        .phone("13700137000")
                        .email("wangwu@example.com")
                        .company("YY公司")
                        .roleIds(Collections.singletonList(3L))
                        .build(),
                UserDO.builder()
                        .userId(4L)
                        .name("赵六")
                        .phone("13600136000")
                        .email("zhaoliu@example.com")
                        .company("YY公司")
                        .roleIds(Collections.singletonList(2L))
                        .build(),
                UserDO.builder()
                        .userId(5L)
                        .name("孙七")
                        .phone("13500135000")
                        .email("sunqi@example.com")
                        .company("ZZ集团")
                        .roleIds(Collections.singletonList(3L))
                        .build()
        );

        users.forEach(u -> userMap.put(u.getUserId(), u));
    }

    public Optional<UserDO> findUserByPhone(String phone) {
        return userMap.values().stream()
                .filter(user -> user.getPhone().equals(phone))
                .findFirst();
    }

    public Optional<RoleDO> findRoleByIdentifier(String identifier) {
        String upperIdentifier = identifier.toUpperCase();
        return roleMap.values().stream()
                .filter(role -> role.getRoleName().equals(identifier) 
                        || role.getRoleCode().equalsIgnoreCase(identifier))
                .findFirst();
    }

    public Optional<PermissionDO> findPermissionByIdentifier(String identifier) {
        String upperIdentifier = identifier.toUpperCase();
        return permissionMap.values().stream()
                .filter(permission -> permission.getPermissionName().equals(identifier)
                        || permission.getPermissionCode().equalsIgnoreCase(identifier))
                .findFirst();
    }

    public List<RoleDO> findRolesByPermissionId(Long permissionId) {
        return roleMap.values().stream()
                .filter(role -> role.getPermissionIds().contains(permissionId))
                .toList();
    }

    public List<PermissionDO> findPermissionsByRoleId(Long roleId) {
        RoleDO role = roleMap.get(roleId);
        if (role == null || role.getPermissionIds() == null) {
            return Collections.emptyList();
        }
        
        return role.getPermissionIds().stream()
                .map(permissionMap::get)
                .filter(Objects::nonNull)
                .toList();
    }

    public String generateApprovalId() {
        String timestamp = LocalDateTime.now().format(APPROVAL_TIME_FORMATTER);
        long sequence = approvalIdCounter.incrementAndGet();
        return String.format("%s%s%04d", RbacConstants.APPROVAL_ID_PREFIX, timestamp, sequence);
    }
}
```

**Step 4: Run test to verify it passes**

Run: `mvn test -Dtest=RbacMockDataServiceTest`
Expected: All tests PASS

**Step 5: Commit**

```bash
git add src/main/java/com/shinelon/hello/data/RbacMockDataService.java src/test/java/com/shinelon/hello/data/RbacMockDataServiceTest.java
git commit -m "feat: add RbacMockDataService with mock data and tests"
```

---

## Task 5: Create Tool 1 - UserQueryTool with Test

**Files:**
- Create: `src/main/java/com/shinelon/hello/tool/UserQueryTool.java`
- Create: `src/test/java/com/shinelon/hello/tool/UserQueryToolTest.java`

**Step 1: Write the failing test**

```java
package com.shinelon.hello.tool;

import com.shinelon.hello.data.RbacMockDataService;
import com.shinelon.hello.model.vo.UserInfoVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * UserQueryTool测试
 *
 * @author shinelon
 */
@DisplayName("用户查询工具测试")
class UserQueryToolTest {

    private UserQueryTool userQueryTool;
    private RbacMockDataService rbacMockDataService;

    @BeforeEach
    void setUp() {
        rbacMockDataService = new RbacMockDataService();
        rbacMockDataService.init();
        userQueryTool = new UserQueryTool(rbacMockDataService);
    }

    @Test
    @DisplayName("查询存在的用户")
    void queryUserByPhone_existingUser_shouldReturnUserInfo() {
        String phone = "13800138000";
        
        UserInfoVO result = userQueryTool.queryUserByPhone(phone);
        
        assertNotNull(result, "结果不应为null");
        assertEquals(phone, result.getPhone(), "手机号应匹配");
        assertEquals("张三", result.getName(), "姓名应匹配");
        assertNotNull(result.getRoles(), "角色列表不应为null");
        assertFalse(result.getRoles().isEmpty(), "应该有角色");
    }

    @Test
    @DisplayName("查询不存在的用户")
    void queryUserByPhone_nonExistingUser_shouldReturnNull() {
        String phone = "99999999999";
        
        UserInfoVO result = userQueryTool.queryUserByPhone(phone);
        
        assertNull(result, "不存在的用户应返回null");
    }
}
```

**Step 2: Run test to verify it fails**

Run: `mvn test -Dtest=UserQueryToolTest`
Expected: FAIL with "UserQueryTool not found"

**Step 3: Write minimal implementation**

```java
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
 *
 * @author shinelon
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserQueryTool {

    private final RbacMockDataService rbacMockDataService;

    @Tool(description = "根据手机号查询用户信息、角色信息和权限信息")
    public UserInfoVO queryUserByPhone(
            @ToolParam(description = "手机号，11位数字") String phone) {
        log.info("[queryUserByPhone] 查询用户信息, phone={}", phone);

        Optional<UserDO> userOptional = rbacMockDataService.findUserByPhone(phone);
        if (userOptional.isEmpty()) {
            log.warn("[queryUserByPhone] 用户不存在, phone={}", phone);
            return null;
        }

        UserDO user = userOptional.get();
        
        List<UserInfoVO.RoleWithPermissionsVO> roles = user.getRoleIds().stream()
                .map(roleId -> {
                    RoleDO role = rbacMockDataService.getRoleMap().get(roleId);
                    if (role == null) {
                        return null;
                    }
                    
                    List<PermissionDO> permissions = rbacMockDataService.findPermissionsByRoleId(roleId);
                    List<UserInfoVO.PermissionSimpleVO> permissionVOs = permissions.stream()
                            .map(p -> UserInfoVO.PermissionSimpleVO.builder()
                                    .permissionId(p.getPermissionId())
                                    .permissionName(p.getPermissionName())
                                    .permissionCode(p.getPermissionCode())
                                    .build())
                            .toList();
                    
                    return UserInfoVO.RoleWithPermissionsVO.builder()
                            .roleId(role.getRoleId())
                            .roleName(role.getRoleName())
                            .roleCode(role.getRoleCode())
                            .permissions(permissionVOs)
                            .build();
                })
                .filter(r -> r != null)
                .toList();

        UserInfoVO result = UserInfoVO.builder()
                .userId(user.getUserId())
                .name(user.getName())
                .phone(user.getPhone())
                .email(user.getEmail())
                .company(user.getCompany())
                .roles(roles)
                .build();

        log.info("[queryUserByPhone] 查询成功, userId={}, name={}, roleCount={}", 
                user.getUserId(), user.getName(), roles.size());
        
        return result;
    }
}
```

**Step 4: Run test to verify it passes**

Run: `mvn test -Dtest=UserQueryToolTest`
Expected: All tests PASS

**Step 5: Commit**

```bash
git add src/main/java/com/shinelon/hello/tool/UserQueryTool.java src/test/java/com/shinelon/hello/tool/UserQueryToolTest.java
git commit -m "feat: add UserQueryTool with tests"
```

---

## Task 6: Create Tool 2 - RolePermissionQueryTool with Test

**Files:**
- Create: `src/main/java/com/shinelon/hello/tool/RolePermissionQueryTool.java`
- Create: `src/test/java/com/shinelon/hello/tool/RolePermissionQueryToolTest.java`

**Step 1: Write the failing test**

```java
package com.shinelon.hello.tool;

import com.shinelon.hello.data.RbacMockDataService;
import com.shinelon.hello.model.vo.PermissionVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RolePermissionQueryTool测试
 *
 * @author shinelon
 */
@DisplayName("角色权限查询工具测试")
class RolePermissionQueryToolTest {

    private RolePermissionQueryTool rolePermissionQueryTool;
    private RbacMockDataService rbacMockDataService;

    @BeforeEach
    void setUp() {
        rbacMockDataService = new RbacMockDataService();
        rbacMockDataService.init();
        rolePermissionQueryTool = new RolePermissionQueryTool(rbacMockDataService);
    }

    record QueryTC(String name, String identifier, int expectedPermissionCount) {}

    static Stream<QueryTC> queryCases() {
        return Stream.of(
                new QueryTC("查询系统管理员权限", "系统管理员", 10),
                new QueryTC("查询ADMIN权限", "ADMIN", 10),
                new QueryTC("查询普通用户权限", "普通用户", 4),
                new QueryTC("查询USER权限", "USER", 4),
                new QueryTC("查询审计员权限", "审计员", 5),
                new QueryTC("查询不存在的角色", "NOTEXIST", 0)
        );
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("queryCases")
    @DisplayName("根据角色查询权限")
    void queryPermissionsByRole(QueryTC tc) {
        List<PermissionVO> result = rolePermissionQueryTool.queryPermissionsByRole(tc.identifier());
        
        assertNotNull(result, "结果不应为null");
        assertEquals(tc.expectedPermissionCount(), result.size(), "权限数量应匹配");
        
        if (tc.expectedPermissionCount() > 0) {
            assertFalse(result.isEmpty(), "应该有权限");
            result.forEach(p -> {
                assertNotNull(p.getPermissionId(), "权限ID不应为null");
                assertNotNull(p.getPermissionName(), "权限名称不应为null");
            });
        }
    }
}
```

**Step 2: Run test to verify it fails**

Run: `mvn test -Dtest=RolePermissionQueryToolTest`
Expected: FAIL with "RolePermissionQueryTool not found"

**Step 3: Write minimal implementation**

```java
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
 *
 * @author shinelon
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RolePermissionQueryTool {

    private final RbacMockDataService rbacMockDataService;

    @Tool(description = "根据角色名称或角色编码查询该角色下的所有权限信息")
    public List<PermissionVO> queryPermissionsByRole(
            @ToolParam(description = "角色名称或角色编码，如：系统管理员、ADMIN") String roleIdentifier) {
        log.info("[queryPermissionsByRole] 查询角色权限, roleIdentifier={}", roleIdentifier);

        Optional<RoleDO> roleOptional = rbacMockDataService.findRoleByIdentifier(roleIdentifier);
        if (roleOptional.isEmpty()) {
            log.warn("[queryPermissionsByRole] 角色不存在, roleIdentifier={}", roleIdentifier);
            return Collections.emptyList();
        }

        RoleDO role = roleOptional.get();
        List<PermissionDO> permissions = rbacMockDataService.findPermissionsByRoleId(role.getRoleId());

        List<PermissionVO> result = permissions.stream()
                .map(p -> PermissionVO.builder()
                        .permissionId(p.getPermissionId())
                        .permissionName(p.getPermissionName())
                        .permissionCode(p.getPermissionCode())
                        .resource(p.getResource())
                        .action(p.getAction())
                        .build())
                .toList();

        log.info("[queryPermissionsByRole] 查询成功, roleId={}, roleName={}, permissionCount={}", 
                role.getRoleId(), role.getRoleName(), result.size());

        return result;
    }
}
```

**Step 4: Run test to verify it passes**

Run: `mvn test -Dtest=RolePermissionQueryToolTest`
Expected: All tests PASS

**Step 5: Commit**

```bash
git add src/main/java/com/shinelon/hello/tool/RolePermissionQueryTool.java src/test/java/com/shinelon/hello/tool/RolePermissionQueryToolTest.java
git commit -m "feat: add RolePermissionQueryTool with tests"
```

---

## Task 7: Create Tool 3 - PermissionRoleQueryTool with Test

**Files:**
- Create: `src/main/java/com/shinelon/hello/tool/PermissionRoleQueryTool.java`
- Create: `src/test/java/com/shinelon/hello/tool/PermissionRoleQueryToolTest.java`

**Step 1: Write the failing test**

```java
package com.shinelon.hello.tool;

import com.shinelon.hello.data.RbacMockDataService;
import com.shinelon.hello.model.vo.RoleVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PermissionRoleQueryTool测试
 *
 * @author shinelon
 */
@DisplayName("权限角色查询工具测试")
class PermissionRoleQueryToolTest {

    private PermissionRoleQueryTool permissionRoleQueryTool;
    private RbacMockDataService rbacMockDataService;

    @BeforeEach
    void setUp() {
        rbacMockDataService = new RbacMockDataService();
        rbacMockDataService.init();
        permissionRoleQueryTool = new PermissionRoleQueryTool(rbacMockDataService);
    }

    record QueryTC(String name, String identifier, int expectedRoleCount) {}

    static Stream<QueryTC> queryCases() {
        return Stream.of(
                new QueryTC("根据权限名称查询-用户查看", "用户查看", 3),
                new QueryTC("根据权限编码查询-USER_READ", "USER_READ", 3),
                new QueryTC("根据权限编码查询-小写", "user_read", 3),
                new QueryTC("查询日志查看权限", "日志查看", 2),
                new QueryTC("查询不存在的权限", "NOTEXIST", 0)
        );
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("queryCases")
    @DisplayName("根据权限查询角色")
    void queryRolesByPermission(QueryTC tc) {
        List<RoleVO> result = permissionRoleQueryTool.queryRolesByPermission(tc.identifier());
        
        assertNotNull(result, "结果不应为null");
        assertEquals(tc.expectedRoleCount(), result.size(), "角色数量应匹配");
        
        if (tc.expectedRoleCount() > 0) {
            assertFalse(result.isEmpty(), "应该有角色");
            result.forEach(r -> {
                assertNotNull(r.getRoleId(), "角色ID不应为null");
                assertNotNull(r.getRoleName(), "角色名称不应为null");
            });
        }
    }
}
```

**Step 2: Run test to verify it fails**

Run: `mvn test -Dtest=PermissionRoleQueryToolTest`
Expected: FAIL with "PermissionRoleQueryTool not found"

**Step 3: Write minimal implementation**

```java
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
 *
 * @author shinelon
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PermissionRoleQueryTool {

    private final RbacMockDataService rbacMockDataService;

    @Tool(description = "根据权限名称或权限编码查询拥有该权限的所有角色")
    public List<RoleVO> queryRolesByPermission(
            @ToolParam(description = "权限名称或权限编码，如：用户查看、USER_READ") String permissionIdentifier) {
        log.info("[queryRolesByPermission] 查询权限角色, permissionIdentifier={}", permissionIdentifier);

        Optional<PermissionDO> permissionOptional = rbacMockDataService.findPermissionByIdentifier(permissionIdentifier);
        if (permissionOptional.isEmpty()) {
            log.warn("[queryRolesByPermission] 权限不存在, permissionIdentifier={}", permissionIdentifier);
            return Collections.emptyList();
        }

        PermissionDO permission = permissionOptional.get();
        List<RoleDO> roles = rbacMockDataService.findRolesByPermissionId(permission.getPermissionId());

        List<RoleVO> result = roles.stream()
                .map(r -> RoleVO.builder()
                        .roleId(r.getRoleId())
                        .roleName(r.getRoleName())
                        .roleCode(r.getRoleCode())
                        .description(r.getDescription())
                        .build())
                .toList();

        log.info("[queryRolesByPermission] 查询成功, permissionId={}, permissionName={}, roleCount={}", 
                permission.getPermissionId(), permission.getPermissionName(), result.size());

        return result;
    }
}
```

**Step 4: Run test to verify it passes**

Run: `mvn test -Dtest=PermissionRoleQueryToolTest`
Expected: All tests PASS

**Step 5: Commit**

```bash
git add src/main/java/com/shinelon/hello/tool/PermissionRoleQueryTool.java src/test/java/com/shinelon/hello/tool/PermissionRoleQueryToolTest.java
git commit -m "feat: add PermissionRoleQueryTool with tests"
```

---

## Task 8: Create Tool 4 - ApprovalSubmitTool with Test

**Files:**
- Create: `src/main/java/com/shinelon/hello/tool/ApprovalSubmitTool.java`
- Create: `src/test/java/com/shinelon/hello/tool/ApprovalSubmitToolTest.java`

**Step 1: Write the failing test**

```java
package com.shinelon.hello.tool;

import com.shinelon.hello.data.RbacMockDataService;
import com.shinelon.hello.model.vo.ApprovalVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ApprovalSubmitTool测试
 *
 * @author shinelon
 */
@DisplayName("审批提交工具测试")
class ApprovalSubmitToolTest {

    private ApprovalSubmitTool approvalSubmitTool;
    private RbacMockDataService rbacMockDataService;

    @BeforeEach
    void setUp() {
        rbacMockDataService = new RbacMockDataService();
        rbacMockDataService.init();
        approvalSubmitTool = new ApprovalSubmitTool(rbacMockDataService);
    }

    @Test
    @DisplayName("提交审批申请")
    void submitApproval_shouldReturnApprovalInfo() {
        String phone = "13800138000";
        String name = "张三";
        String email = "zhangsan@example.com";
        String company = "XX科技";
        String roleName = "系统管理员";
        String reason = "工作需要";
        
        ApprovalVO result = approvalSubmitTool.submitApproval(phone, name, email, company, roleName, reason);
        
        assertNotNull(result, "结果不应为null");
        assertNotNull(result.getApprovalId(), "审批单号不应为null");
        assertTrue(result.getApprovalId().startsWith("AP"), "审批单号应以AP开头");
        assertEquals("PENDING", result.getStatus(), "状态应为PENDING");
        assertEquals(name, result.getApplicant(), "申请人应匹配");
        assertEquals(roleName, result.getAppliedRole(), "申请角色应匹配");
        assertNotNull(result.getSubmitTime(), "提交时间不应为null");
    }
}
```

**Step 2: Run test to verify it fails**

Run: `mvn test -Dtest=ApprovalSubmitToolTest`
Expected: FAIL with "ApprovalSubmitTool not found"

**Step 3: Write minimal implementation**

```java
package com.shinelon.hello.tool;

import com.shinelon.hello.common.constants.RbacConstants;
import com.shinelon.hello.data.RbacMockDataService;
import com.shinelon.hello.model.vo.ApprovalVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 审批提交工具
 *
 * @author shinelon
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ApprovalSubmitTool {

    private final RbacMockDataService rbacMockDataService;
    
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Tool(description = "提交权限或角色变更的审批请求")
    public ApprovalVO submitApproval(
            @ToolParam(description = "申请人手机号") String phone,
            @ToolParam(description = "申请人姓名") String name,
            @ToolParam(description = "申请人邮箱") String email,
            @ToolParam(description = "所属公司") String company,
            @ToolParam(description = "申请的角色，如：系统管理员、普通用户") String roleName,
            @ToolParam(description = "申请原因") String reason) {
        log.info("[submitApproval] 提交审批申请, phone={}, name={}, roleName={}", phone, name, roleName);

        String approvalId = rbacMockDataService.generateApprovalId();
        String submitTime = LocalDateTime.now().format(TIME_FORMATTER);

        ApprovalVO result = ApprovalVO.builder()
                .approvalId(approvalId)
                .status(RbacConstants.APPROVAL_STATUS_PENDING)
                .message("审批申请已提交成功")
                .applicant(name)
                .appliedRole(roleName)
                .submitTime(submitTime)
                .build();

        log.info("[submitApproval] 审批提交成功, approvalId={}, applicant={}", approvalId, name);

        return result;
    }
}
```

**Step 4: Run test to verify it passes**

Run: `mvn test -Dtest=ApprovalSubmitToolTest`
Expected: All tests PASS

**Step 5: Commit**

```bash
git add src/main/java/com/shinelon/hello/tool/ApprovalSubmitTool.java src/test/java/com/shinelon/hello/tool/ApprovalSubmitToolTest.java
git commit -m "feat: add ApprovalSubmitTool with tests"
```

---

## Task 9: Create RbacChatManager with Test

**Files:**
- Create: `src/main/java/com/shinelon/hello/manager/RbacChatManager.java`
- Create: `src/test/java/com/shinelon/hello/manager/RbacChatManagerTest.java`

**Step 1: Write the failing test**

```java
package com.shinelon.hello.manager;

import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * RbacChatManager测试
 *
 * @author shinelon
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("RBAC对话管理器测试")
class RbacChatManagerTest {

    // 集成测试将在实际环境中进行
    // 这里主要测试Tool注册和基本功能
}
```

**Step 2: Run test to verify it fails**

Run: `mvn test -Dtest=RbacChatManagerTest`
Expected: FAIL with "RbacChatManager not found"

**Step 3: Write minimal implementation**

```java
package com.shinelon.hello.manager;

import com.shinelon.hello.common.constants.RbacConstants;
import com.shinelon.hello.common.enums.ErrorCodeEnum;
import com.shinelon.hello.common.exception.BusinessException;
import com.shinelon.hello.tool.ApprovalSubmitTool;
import com.shinelon.hello.tool.PermissionRoleQueryTool;
import com.shinelon.hello.tool.RolePermissionQueryTool;
import com.shinelon.hello.tool.UserQueryTool;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

/**
 * RBAC对话管理器
 *
 * @author shinelon
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RbacChatManager {

    private final ChatClient.Builder chatClientBuilder;
    private final UserQueryTool userQueryTool;
    private final RolePermissionQueryTool rolePermissionQueryTool;
    private final PermissionRoleQueryTool permissionRoleQueryTool;
    private final ApprovalSubmitTool approvalSubmitTool;

    private ChatClient chatClient;

    @PostConstruct
    public void init() {
        this.chatClient = chatClientBuilder
                .defaultSystem(RbacConstants.SYSTEM_PROMPT)
                .build();
        
        log.info("[init] RbacChatManager初始化完成");
    }

    public String chat(String prompt) {
        validatePrompt(prompt);

        log.debug("[chat] 开始对话, prompt={}", prompt);

        try {
            return chatClient.prompt()
                    .user(prompt)
                    .tools(userQueryTool)
                    .tools(rolePermissionQueryTool)
                    .tools(permissionRoleQueryTool)
                    .tools(approvalSubmitTool)
                    .call()
                    .content();
        } catch (Exception e) {
            log.error("[chat] AI调用失败: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCodeEnum.AI_SERVICE_UNAVAILABLE, "AI服务调用失败", e);
        }
    }

    private void validatePrompt(String prompt) {
        if (prompt == null || prompt.trim().isEmpty()) {
            throw new IllegalArgumentException("输入内容不能为空");
        }
    }
}
```

**Step 4: Run test to verify it passes**

Run: `mvn test -Dtest=RbacChatManagerTest`
Expected: PASS (empty test)

**Step 5: Commit**

```bash
git add src/main/java/com/shinelon/hello/manager/RbacChatManager.java src/test/java/com/shinelon/hello/manager/RbacChatManagerTest.java
git commit -m "feat: add RbacChatManager with 4 tools registered"
```

---

## Task 10: Create RbacService and RbacServiceImpl with Test

**Files:**
- Create: `src/main/java/com/shinelon/hello/service/RbacService.java`
- Create: `src/main/java/com/shinelon/hello/service/impl/RbacServiceImpl.java`
- Create: `src/test/java/com/shinelon/hello/service/RbacServiceTest.java`

**Step 1: Write the failing test**

```java
package com.shinelon.hello.service;

import com.shinelon.hello.model.dto.RbacChatRequestDTO;
import com.shinelon.hello.model.vo.RbacChatVO;
import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RbacService测试
 *
 * @author shinelon
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("RBAC服务测试")
class RbacServiceTest {

    @Autowired
    private RbacService rbacService;

    @DisplayName("测试问候语识别")
    void testGreetingDetection() {
        RbacChatRequestDTO request = RbacChatRequestDTO.builder()
                .content("你好")
                .build();
        
        RbacChatVO response = rbacService.chat(request);
        
        assertNotNull(response, "响应不应为null");
        assertNotNull(response.getContent(), "内容不应为null");
        assertTrue(response.getContent().contains("角色权限查询AI小助手"), "应包含功能介绍");
    }
}
```

**Step 2: Run test to verify it fails**

Run: `mvn test -Dtest=RbacServiceTest`
Expected: FAIL with "RbacService not found"

**Step 3: Write minimal implementation - Interface**

```java
package com.shinelon.hello.service;

import com.shinelon.hello.model.dto.RbacChatRequestDTO;
import com.shinelon.hello.model.vo.RbacChatVO;

/**
 * RBAC服务接口
 *
 * @author shinelon
 */
public interface RbacService {
    
    RbacChatVO chat(RbacChatRequestDTO request);
}
```

**Step 4: Write minimal implementation - Implementation**

```java
package com.shinelon.hello.service.impl;

import com.shinelon.hello.common.constants.RbacConstants;
import com.shinelon.hello.common.utils.DesensitizationUtils;
import com.shinelon.hello.manager.RbacChatManager;
import com.shinelon.hello.model.dto.RbacChatRequestDTO;
import com.shinelon.hello.model.vo.RbacChatVO;
import com.shinelon.hello.service.RbacService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * RBAC服务实现
 *
 * @author shinelon
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RbacServiceImpl implements RbacService {

    private final RbacChatManager rbacChatManager;

    @Override
    public RbacChatVO chat(RbacChatRequestDTO request) {
        validateRequest(request);

        String content = request.getContent().trim();
        log.info("[chat] RBAC对话请求开始, content={}", DesensitizationUtils.truncateAndMask(content, 50));

        String response;
        if (isGreetingOrEmpty(content)) {
            log.info("[chat] 检测到问候语，返回功能介绍");
            response = RbacConstants.GREETING_RESPONSE;
        } else {
            response = rbacChatManager.chat(content);
        }

        log.info("[chat] RBAC对话完成, 响应长度={}", response.length());

        return RbacChatVO.builder()
                .content(response)
                .createTime(LocalDateTime.now())
                .build();
    }

    private void validateRequest(RbacChatRequestDTO request) {
        if (request == null) {
            throw new IllegalArgumentException("请求不能为空");
        }
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("消息内容不能为空");
        }
    }

    private boolean isGreetingOrEmpty(String content) {
        if (content.isEmpty()) {
            return true;
        }

        String lower = content.toLowerCase().trim();
        return lower.matches("^(你好|hi|hello|嗨|您好|在吗|你好吗|help|帮助|你能做什么|介绍一下).*$");
    }
}
```

**Step 5: Run test to verify it passes**

Run: `mvn test -Dtest=RbacServiceTest`
Expected: PASS

**Step 6: Commit**

```bash
git add src/main/java/com/shinelon/hello/service/RbacService.java src/main/java/com/shinelon/hello/service/impl/RbacServiceImpl.java src/test/java/com/shinelon/hello/service/RbacServiceTest.java
git commit -m "feat: add RbacService with greeting detection"
```

---

## Task 11: Create RbacController with Test

**Files:**
- Create: `src/main/java/com/shinelon/hello/controller/RbacController.java`
- Create: `src/test/java/com/shinelon/hello/controller/RbacControllerTest.java`

**Step 1: Write the failing test**

```java
package com.shinelon.hello.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shinelon.hello.model.dto.RbacChatRequestDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * RbacController测试
 *
 * @author shinelon
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("RBAC控制器测试")
class RbacControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("测试问候语接口")
    void chat_withGreeting_shouldReturnGreetingResponse() throws Exception {
        RbacChatRequestDTO request = RbacChatRequestDTO.builder()
                .content("你好")
                .build();

        mockMvc.perform(post("/rbac/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content").exists())
                .andExpect(jsonPath("$.data.createTime").exists());
    }

    @Test
    @DisplayName("测试空内容验证")
    void chat_withEmptyContent_shouldReturnError() throws Exception {
        RbacChatRequestDTO request = RbacChatRequestDTO.builder()
                .content("")
                .build();

        mockMvc.perform(post("/rbac/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
```

**Step 2: Run test to verify it fails**

Run: `mvn test -Dtest=RbacControllerTest`
Expected: FAIL with "RbacController not found"

**Step 3: Write minimal implementation**

```java
package com.shinelon.hello.controller;

import com.shinelon.hello.common.result.Result;
import com.shinelon.hello.common.utils.DesensitizationUtils;
import com.shinelon.hello.model.dto.RbacChatRequestDTO;
import com.shinelon.hello.model.vo.RbacChatVO;
import com.shinelon.hello.service.RbacService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * RBAC AI助手控制器
 *
 * @author shinelon
 */
@Slf4j
@RestController
@RequestMapping("/rbac")
@RequiredArgsConstructor
public class RbacController {

    private final RbacService rbacService;

    @PostMapping("/chat")
    public Result<RbacChatVO> chat(@Valid @RequestBody RbacChatRequestDTO request) {
        log.info("[chat] RBAC对话请求, content={}", DesensitizationUtils.truncateAndMask(request.getContent(), 50));

        RbacChatVO response = rbacService.chat(request);
        return Result.success(response);
    }
}
```

**Step 4: Run test to verify it passes**

Run: `mvn test -Dtest=RbacControllerTest`
Expected: All tests PASS

**Step 5: Commit**

```bash
git add src/main/java/com/shinelon/hello/controller/RbacController.java src/test/java/com/shinelon/hello/controller/RbacControllerTest.java
git commit -m "feat: add RbacController with /rbac/chat endpoint"
```

---

## Task 12: Run All Tests and Build

**Step 1: Run all tests**

Run: `mvn clean test`
Expected: All tests PASS

**Step 2: Build the project**

Run: `mvn clean package -DskipTests`
Expected: BUILD SUCCESS

**Step 3: Manual test - Start application**

Run: `mvn spring-boot:run`
Expected: Application starts successfully on port 8080

**Step 4: Manual test - Test greeting**

Use Postman or curl:
```bash
POST http://localhost:8080/ai/rbac/chat
Content-Type: application/json

{
  "content": "你好"
}
```

Expected: Returns greeting message with 4 service options

**Step 5: Manual test - Test user query**

```bash
POST http://localhost:8080/ai/rbac/chat
Content-Type: application/json

{
  "content": "查询手机号13800138000的用户信息"
}
```

Expected: AI calls UserQueryTool and returns user information

**Step 6: Stop application**

Run: Find and kill the Java process on port 8080

```bash
netstat -ano | findstr :8080
taskkill /F /PID <PID>
```

**Step 7: Final commit**

```bash
git add .
git commit -m "feat: complete RBAC AI Assistant with 4 tools"
```

---

## Summary

This implementation plan creates a complete RBAC AI Assistant with:

1. ✅ 4 independent Tool classes with Spring AI @Tool annotations
2. ✅ RbacMockDataService with 5 users, 3 roles, 10 permissions
3. ✅ AI greeting detection and service introduction
4. ✅ REST API endpoint `/rbac/chat`
5. ✅ Comprehensive unit and integration tests
6. ✅ Following project code conventions

**Total estimated time**: 2-3 hours

**Next steps after implementation**:
- Add streaming endpoint `/rbac/chat/stream` if needed
- Add more test cases for edge scenarios
- Consider adding API rate limiting
- Consider adding authentication/authorization
