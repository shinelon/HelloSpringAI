package com.shinelon.hello.tool;

import com.shinelon.hello.data.RbacMockDataService;
import com.shinelon.hello.model.vo.RoleVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PermissionRoleQueryTool 单元测试
 * 使用表格驱动测试风格
 *
 * @author shinelon
 */
@DisplayName("PermissionRoleQueryTool 测试")
class PermissionRoleQueryToolTest {

    private PermissionRoleQueryTool permissionRoleQueryTool;
    private RbacMockDataService rbacMockDataService;

    @BeforeEach
    void setUp() {
        rbacMockDataService = new RbacMockDataService();
        rbacMockDataService.init();
        permissionRoleQueryTool = new PermissionRoleQueryTool(rbacMockDataService);
    }

    record QueryRolesTestCase(
            String name,
            String identifier,
            boolean shouldExist,
            int expectedRoleCount,
            String expectedRoleName
    ) {}

    static Stream<QueryRolesTestCase> queryRolesTestCases() {
        return Stream.of(
                new QueryRolesTestCase("根据权限名称查询-用户查看", "用户查看", true, 3, "管理员"),
                new QueryRolesTestCase("根据权限代码查询-USER_READ", "USER_READ", true, 3, "管理员"),
                new QueryRolesTestCase("根据权限名称查询-用户编辑", "用户编辑", true, 1, "管理员"),
                new QueryRolesTestCase("根据权限代码查询-USER_WRITE", "USER_WRITE", true, 1, "管理员"),
                new QueryRolesTestCase("根据权限名称查询-日志查看", "日志查看", true, 3, "管理员"),
                new QueryRolesTestCase("根据权限代码查询-LOG_READ", "LOG_READ", true, 3, "管理员"),
                new QueryRolesTestCase("查询不存在的权限", "不存在的权限", false, 0, null)
        );
    }

    @Nested
    @DisplayName("queryRolesByPermission 根据权限查询角色测试")
    class QueryRolesByPermissionTests {

        @ParameterizedTest(name = "[{index}] {0}")
        @MethodSource("com.shinelon.hello.tool.PermissionRoleQueryToolTest#queryRolesTestCases")
        @DisplayName("根据权限查询角色应返回正确结果")
        void queryRolesByPermission_shouldReturnCorrectResult(QueryRolesTestCase testCase) {
            // When
            List<RoleVO> result = permissionRoleQueryTool.queryRolesByPermission(testCase.identifier());

            // Then
            if (testCase.shouldExist()) {
                assertNotNull(result, testCase.name() + " 应返回角色列表");
                assertEquals(testCase.expectedRoleCount(), result.size(), 
                    testCase.name() + " 角色数量不匹配");
                assertFalse(result.isEmpty(), testCase.name() + " 角色列表不应为空");
                
                RoleVO firstRole = result.get(0);
                assertNotNull(firstRole.getRoleId(), "角色ID不应为null");
                assertNotNull(firstRole.getRoleName(), "角色名称不应为null");
                assertNotNull(firstRole.getRoleCode(), "角色代码不应为null");
            } else {
                assertNotNull(result, testCase.name() + " 应返回空列表");
                assertTrue(result.isEmpty(), testCase.name() + " 应返回空列表");
            }
        }

        @Test
        @DisplayName("查询通用权限应返回多个角色")
        void queryRolesByPermission_commonPermission_shouldReturnMultipleRoles() {
            // When
            List<RoleVO> result = permissionRoleQueryTool.queryRolesByPermission("用户查看");

            // Then
            assertNotNull(result);
            assertEquals(3, result.size(), "用户查看权限应有3个角色");
            
            List<String> roleCodes = result.stream()
                .map(RoleVO::getRoleCode)
                .toList();
            assertTrue(roleCodes.contains("ADMIN"), "应包含ADMIN角色");
            assertTrue(roleCodes.contains("USER"), "应包含USER角色");
            assertTrue(roleCodes.contains("AUDITOR"), "应包含AUDITOR角色");
        }

        @Test
        @DisplayName("查询专属权限应返回单个角色")
        void queryRolesByPermission_exclusivePermission_shouldReturnSingleRole() {
            // When
            List<RoleVO> result = permissionRoleQueryTool.queryRolesByPermission("用户编辑");

            // Then
            assertNotNull(result);
            assertEquals(1, result.size(), "用户编辑权限应只有1个角色");
            assertEquals("管理员", result.get(0).getRoleName());
            assertEquals("ADMIN", result.get(0).getRoleCode());
        }

        @Test
        @DisplayName("角色信息应包含完整字段")
        void queryRolesByPermission_shouldReturnCompleteRoleInfo() {
            // When
            List<RoleVO> result = permissionRoleQueryTool.queryRolesByPermission("USER_READ");

            // Then
            assertFalse(result.isEmpty());
            RoleVO role = result.get(0);
            assertNotNull(role.getRoleId());
            assertNotNull(role.getRoleName());
            assertNotNull(role.getRoleCode());
            assertNotNull(role.getDescription());
        }
    }

    @Nested
    @DisplayName("空值和边界测试")
    class NullAndBoundaryTests {

        @Test
        @DisplayName("权限标识为null应返回空列表")
        void queryRolesByPermission_nullIdentifier_shouldReturnEmptyList() {
            // When
            List<RoleVO> result = permissionRoleQueryTool.queryRolesByPermission(null);

            // Then
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("权限标识为空字符串应返回空列表")
        void queryRolesByPermission_emptyIdentifier_shouldReturnEmptyList() {
            // When
            List<RoleVO> result = permissionRoleQueryTool.queryRolesByPermission("");

            // Then
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("权限标识为空格应返回空列表")
        void queryRolesByPermission_blankIdentifier_shouldReturnEmptyList() {
            // When
            List<RoleVO> result = permissionRoleQueryTool.queryRolesByPermission("   ");

            // Then
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("权限标识不区分大小写")
        void queryRolesByPermission_caseInsensitive_shouldReturnSameResult() {
            // When
            List<RoleVO> result1 = permissionRoleQueryTool.queryRolesByPermission("user_read");
            List<RoleVO> result2 = permissionRoleQueryTool.queryRolesByPermission("USER_READ");
            List<RoleVO> result3 = permissionRoleQueryTool.queryRolesByPermission("User_Read");

            // Then
            assertEquals(result1.size(), result2.size());
            assertEquals(result2.size(), result3.size());
        }
    }
}
