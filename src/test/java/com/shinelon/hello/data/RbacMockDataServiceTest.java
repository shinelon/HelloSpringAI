package com.shinelon.hello.data;

import com.shinelon.hello.model.entity.PermissionDO;
import com.shinelon.hello.model.entity.RoleDO;
import com.shinelon.hello.model.entity.UserDO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("RbacMockDataService测试")
class RbacMockDataServiceTest {

    @Autowired
    private RbacMockDataService service;

    @Nested
    @DisplayName("用户查询测试")
    class UserQueryTests {

        record UserQueryTC(String name, String phone, boolean shouldExist, String expectedName) {}

        static Stream<UserQueryTC> userQueryCases() {
            return Stream.of(
                new UserQueryTC("查询现有用户-张三", "13800138001", true, "张三"),
                new UserQueryTC("查询现有用户-李四", "13800138002", true, "李四"),
                new UserQueryTC("查询现有用户-王五", "13800138003", true, "王五"),
                new UserQueryTC("查询现有用户-赵六", "13800138004", true, "赵六"),
                new UserQueryTC("查询现有用户-孙七", "13800138005", true, "孙七"),
                new UserQueryTC("查询不存在的用户", "99999999999", false, null)
            );
        }

        @ParameterizedTest(name = "[{index}] {0}")
        @MethodSource("userQueryCases")
        @DisplayName("根据手机号查询用户")
        void findUserByPhone_shouldReturnCorrectResult(UserQueryTC tc) {
            Optional<UserDO> result = service.findUserByPhone(tc.phone());

            if (tc.shouldExist()) {
                assertTrue(result.isPresent(), "应该找到用户");
                assertEquals(tc.expectedName(), result.get().getName(), "用户名应该匹配");
                assertEquals(tc.phone(), result.get().getPhone(), "手机号应该匹配");
                assertNotNull(result.get().getUserId(), "用户ID不应为空");
                assertNotNull(result.get().getRoleIds(), "角色ID列表不应为空");
            } else {
                assertFalse(result.isPresent(), "不应该找到用户");
            }
        }
    }

    @Nested
    @DisplayName("角色查询测试")
    class RoleQueryTests {

        record RoleQueryTC(String name, String identifier, boolean shouldExist, String expectedCode) {}

        static Stream<RoleQueryTC> roleQueryCases() {
            return Stream.of(
                new RoleQueryTC("根据名称查询-ADMIN", "管理员", true, "ADMIN"),
                new RoleQueryTC("根据名称查询-USER", "普通用户", true, "USER"),
                new RoleQueryTC("根据名称查询-AUDITOR", "审计员", true, "AUDITOR"),
                new RoleQueryTC("根据代码查询-ADMIN", "ADMIN", true, "ADMIN"),
                new RoleQueryTC("根据代码查询-USER", "USER", true, "USER"),
                new RoleQueryTC("根据代码查询-AUDITOR", "AUDITOR", true, "AUDITOR"),
                new RoleQueryTC("名称大小写不敏感", "管理员", true, "ADMIN"),
                new RoleQueryTC("代码大小写不敏感", "admin", true, "ADMIN"),
                new RoleQueryTC("查询不存在的角色", "SUPER_ADMIN", false, null)
            );
        }

        @ParameterizedTest(name = "[{index}] {0}")
        @MethodSource("roleQueryCases")
        @DisplayName("根据标识符查询角色")
        void findRoleByIdentifier_shouldReturnCorrectResult(RoleQueryTC tc) {
            Optional<RoleDO> result = service.findRoleByIdentifier(tc.identifier());

            if (tc.shouldExist()) {
                assertTrue(result.isPresent(), "应该找到角色");
                assertEquals(tc.expectedCode(), result.get().getRoleCode(), "角色代码应该匹配");
                assertNotNull(result.get().getRoleId(), "角色ID不应为空");
                assertNotNull(result.get().getPermissionIds(), "权限ID列表不应为空");
            } else {
                assertFalse(result.isPresent(), "不应该找到角色");
            }
        }
    }

    @Nested
    @DisplayName("权限查询测试")
    class PermissionQueryTests {

        record PermissionQueryTC(String name, String identifier, boolean shouldExist, String expectedCode) {}

        static Stream<PermissionQueryTC> permissionQueryCases() {
            return Stream.of(
                new PermissionQueryTC("根据名称查询", "用户查看", true, "USER_READ"),
                new PermissionQueryTC("根据名称查询", "角色查看", true, "ROLE_READ"),
                new PermissionQueryTC("根据代码查询", "USER_WRITE", true, "USER_WRITE"),
                new PermissionQueryTC("根据代码查询", "APPROVAL_READ", true, "APPROVAL_READ"),
                new PermissionQueryTC("名称大小写不敏感", "用户查看", true, "USER_READ"),
                new PermissionQueryTC("代码大小写不敏感", "user_read", true, "USER_READ"),
                new PermissionQueryTC("查询不存在的权限", "SUPER_POWER", false, null)
            );
        }

        @ParameterizedTest(name = "[{index}] {0}")
        @MethodSource("permissionQueryCases")
        @DisplayName("根据标识符查询权限")
        void findPermissionByIdentifier_shouldReturnCorrectResult(PermissionQueryTC tc) {
            Optional<PermissionDO> result = service.findPermissionByIdentifier(tc.identifier());

            if (tc.shouldExist()) {
                assertTrue(result.isPresent(), "应该找到权限");
                assertEquals(tc.expectedCode(), result.get().getPermissionCode(), "权限代码应该匹配");
                assertNotNull(result.get().getPermissionId(), "权限ID不应为空");
            } else {
                assertFalse(result.isPresent(), "不应该找到权限");
            }
        }
    }

    @Nested
    @DisplayName("角色-权限关系测试")
    class RolePermissionRelationTests {

        record RolePermissionTC(String name, Long roleId, int expectedPermissionCount, String expectedFirstPermission) {}

        static Stream<RolePermissionTC> rolePermissionCases() {
            return Stream.of(
                new RolePermissionTC("ADMIN角色应包含所有10个权限", 1L, 10, "USER_READ"),
                new RolePermissionTC("USER角色应包含4个权限", 2L, 4, "USER_READ"),
                new RolePermissionTC("AUDITOR角色应包含5个权限", 3L, 5, "LOG_READ")
            );
        }

        @ParameterizedTest(name = "[{index}] {0}")
        @MethodSource("rolePermissionCases")
        @DisplayName("查询角色的权限列表")
        void findPermissionsByRoleId_shouldReturnCorrectPermissions(RolePermissionTC tc) {
            List<PermissionDO> permissions = service.findPermissionsByRoleId(tc.roleId());

            assertNotNull(permissions, "权限列表不应为null");
            assertEquals(tc.expectedPermissionCount(), permissions.size(), "权限数量应该匹配");
            if (!permissions.isEmpty()) {
                assertEquals(tc.expectedFirstPermission(), permissions.get(0).getPermissionCode(), "第一个权限代码应该匹配");
            }
        }
    }

    @Nested
    @DisplayName("权限-角色关系测试")
    class PermissionRoleRelationTests {

        record PermissionRoleTC(String name, Long permissionId, int expectedRoleCount) {}

        static Stream<PermissionRoleTC> permissionRoleCases() {
            return Stream.of(
                new PermissionRoleTC("USER_READ权限被所有3个角色拥有", 1L, 3),
                new PermissionRoleTC("USER_DELETE权限只被ADMIN拥有", 3L, 1),
                new PermissionRoleTC("APPROVAL_WRITE权限被ADMIN和AUDITOR拥有", 9L, 2)
            );
        }

        @ParameterizedTest(name = "[{index}] {0}")
        @MethodSource("permissionRoleCases")
        @DisplayName("查询拥有指定权限的角色列表")
        void findRolesByPermissionId_shouldReturnCorrectRoles(PermissionRoleTC tc) {
            List<RoleDO> roles = service.findRolesByPermissionId(tc.permissionId());

            assertNotNull(roles, "角色列表不应为null");
            assertEquals(tc.expectedRoleCount(), roles.size(), "角色数量应该匹配");
        }
    }

    @Nested
    @DisplayName("工具方法测试")
    class UtilityMethodTests {

        @org.junit.jupiter.api.Test
        @DisplayName("生成审批ID应返回非空字符串")
        void generateApprovalId_shouldReturnNonEmptyString() {
            String approvalId = service.generateApprovalId();

            assertNotNull(approvalId, "审批ID不应为null");
            assertFalse(approvalId.isEmpty(), "审批ID不应为空");
            assertTrue(approvalId.startsWith("APV-"), "审批ID应以APV-开头");
        }

        @org.junit.jupiter.api.Test
        @DisplayName("多次生成审批ID应唯一")
        void generateApprovalId_shouldBeUnique() {
            String id1 = service.generateApprovalId();
            String id2 = service.generateApprovalId();
            String id3 = service.generateApprovalId();

            assertNotEquals(id1, id2, "ID1和ID2应该不同");
            assertNotEquals(id2, id3, "ID2和ID3应该不同");
            assertNotEquals(id1, id3, "ID1和ID3应该不同");
        }
    }

    @Nested
    @DisplayName("数据完整性测试")
    class DataIntegrityTests {

        @org.junit.jupiter.api.Test
        @DisplayName("所有用户都应分配角色")
        void allUsers_shouldHaveRoles() {
            for (int i = 1; i <= 5; i++) {
                String phone = String.format("138001380%02d", i);
                Optional<UserDO> user = service.findUserByPhone(phone);
                assertTrue(user.isPresent(), "用户" + i + "应该存在");
                assertNotNull(user.get().getRoleIds(), "用户" + i + "的角色列表不应为null");
                assertFalse(user.get().getRoleIds().isEmpty(), "用户" + i + "应至少有一个角色");
            }
        }

        @org.junit.jupiter.api.Test
        @DisplayName("所有角色都应分配权限")
        void allRoles_shouldHavePermissions() {
            for (long i = 1; i <= 3; i++) {
                Optional<RoleDO> role = service.findRoleByIdentifier(String.valueOf(i));
                if (role.isPresent()) {
                    assertNotNull(role.get().getPermissionIds(), "角色" + i + "的权限列表不应为null");
                    assertFalse(role.get().getPermissionIds().isEmpty(), "角色" + i + "应至少有一个权限");
                }
            }
        }
    }
}
