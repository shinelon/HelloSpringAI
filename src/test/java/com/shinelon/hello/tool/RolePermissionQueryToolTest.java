package com.shinelon.hello.tool;

import com.shinelon.hello.data.RbacMockDataService;
import com.shinelon.hello.model.vo.PermissionVO;
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
 * RolePermissionQueryTool 单元测试
 * 使用表格驱动测试风格
 *
 * @author shinelon
 */
@DisplayName("RolePermissionQueryTool 测试")
class RolePermissionQueryToolTest {

    private RolePermissionQueryTool rolePermissionQueryTool;
    private RbacMockDataService rbacMockDataService;

    @BeforeEach
    void setUp() {
        rbacMockDataService = new RbacMockDataService();
        rbacMockDataService.init();
        rolePermissionQueryTool = new RolePermissionQueryTool(rbacMockDataService);
    }

    record QueryPermissionsTestCase(
            String name,
            String identifier,
            boolean shouldExist,
            int expectedPermissionCount,
            String expectedPermissionName
    ) {}

    static Stream<QueryPermissionsTestCase> queryPermissionsTestCases() {
        return Stream.of(
                new QueryPermissionsTestCase("根据角色名称查询-管理员", "管理员", true, 10, "用户查看"),
                new QueryPermissionsTestCase("根据角色代码查询-ADMIN", "ADMIN", true, 10, "用户查看"),
                new QueryPermissionsTestCase("根据角色名称查询-普通用户", "普通用户", true, 4, "用户查看"),
                new QueryPermissionsTestCase("根据角色代码查询-USER", "USER", true, 4, "用户查看"),
                new QueryPermissionsTestCase("根据角色名称查询-审计员", "审计员", true, 5, "日志查看"),
                new QueryPermissionsTestCase("根据角色代码查询-AUDITOR", "AUDITOR", true, 5, "日志查看"),
                new QueryPermissionsTestCase("查询不存在的角色", "不存在的角色", false, 0, null)
        );
    }

    @Nested
    @DisplayName("queryPermissionsByRole 根据角色查询权限测试")
    class QueryPermissionsByRoleTests {

        @ParameterizedTest(name = "[{index}] {0}")
        @MethodSource("com.shinelon.hello.tool.RolePermissionQueryToolTest#queryPermissionsTestCases")
        @DisplayName("根据角色查询权限应返回正确结果")
        void queryPermissionsByRole_shouldReturnCorrectResult(QueryPermissionsTestCase testCase) {
            // When
            List<PermissionVO> result = rolePermissionQueryTool.queryPermissionsByRole(testCase.identifier());

            // Then
            if (testCase.shouldExist()) {
                assertNotNull(result, testCase.name() + " 应返回权限列表");
                assertEquals(testCase.expectedPermissionCount(), result.size(), 
                    testCase.name() + " 权限数量不匹配");
                assertFalse(result.isEmpty(), testCase.name() + " 权限列表不应为空");
                
                PermissionVO firstPermission = result.get(0);
                assertNotNull(firstPermission.getPermissionId(), "权限ID不应为null");
                assertNotNull(firstPermission.getPermissionName(), "权限名称不应为null");
                assertNotNull(firstPermission.getPermissionCode(), "权限代码不应为null");
            } else {
                assertNotNull(result, testCase.name() + " 应返回空列表");
                assertTrue(result.isEmpty(), testCase.name() + " 应返回空列表");
            }
        }

        @Test
        @DisplayName("查询管理员角色应返回所有10个权限")
        void queryPermissionsByRole_adminRole_shouldReturnAllPermissions() {
            // When
            List<PermissionVO> result = rolePermissionQueryTool.queryPermissionsByRole("管理员");

            // Then
            assertNotNull(result);
            assertEquals(10, result.size(), "管理员应有10个权限");
            
            List<String> permissionCodes = result.stream()
                .map(PermissionVO::getPermissionCode)
                .toList();
            assertTrue(permissionCodes.contains("USER_READ"), "应包含USER_READ权限");
            assertTrue(permissionCodes.contains("USER_WRITE"), "应包含USER_WRITE权限");
            assertTrue(permissionCodes.contains("ROLE_READ"), "应包含ROLE_READ权限");
        }

        @Test
        @DisplayName("查询普通用户角色应返回4个基础权限")
        void queryPermissionsByRole_userRole_shouldReturnBasicPermissions() {
            // When
            List<PermissionVO> result = rolePermissionQueryTool.queryPermissionsByRole("USER");

            // Then
            assertNotNull(result);
            assertEquals(4, result.size(), "普通用户应有4个权限");
            
            List<String> permissionCodes = result.stream()
                .map(PermissionVO::getPermissionCode)
                .toList();
            assertTrue(permissionCodes.contains("USER_READ"), "应包含USER_READ权限");
            assertTrue(permissionCodes.contains("ROLE_READ"), "应包含ROLE_READ权限");
            assertTrue(permissionCodes.contains("APPROVAL_READ"), "应包含APPROVAL_READ权限");
            assertTrue(permissionCodes.contains("LOG_READ"), "应包含LOG_READ权限");
        }

        @Test
        @DisplayName("权限信息应包含完整字段")
        void queryPermissionsByRole_shouldReturnCompletePermissionInfo() {
            // When
            List<PermissionVO> result = rolePermissionQueryTool.queryPermissionsByRole("ADMIN");

            // Then
            assertFalse(result.isEmpty());
            PermissionVO permission = result.get(0);
            assertNotNull(permission.getPermissionId());
            assertNotNull(permission.getPermissionName());
            assertNotNull(permission.getPermissionCode());
            assertNotNull(permission.getResource());
            assertNotNull(permission.getAction());
        }
    }

    @Nested
    @DisplayName("空值和边界测试")
    class NullAndBoundaryTests {

        @Test
        @DisplayName("角色标识为null应返回空列表")
        void queryPermissionsByRole_nullIdentifier_shouldReturnEmptyList() {
            // When
            List<PermissionVO> result = rolePermissionQueryTool.queryPermissionsByRole(null);

            // Then
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("角色标识为空字符串应返回空列表")
        void queryPermissionsByRole_emptyIdentifier_shouldReturnEmptyList() {
            // When
            List<PermissionVO> result = rolePermissionQueryTool.queryPermissionsByRole("");

            // Then
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("角色标识为空格应返回空列表")
        void queryPermissionsByRole_blankIdentifier_shouldReturnEmptyList() {
            // When
            List<PermissionVO> result = rolePermissionQueryTool.queryPermissionsByRole("   ");

            // Then
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("角色标识不区分大小写")
        void queryPermissionsByRole_caseInsensitive_shouldReturnSameResult() {
            // When
            List<PermissionVO> result1 = rolePermissionQueryTool.queryPermissionsByRole("admin");
            List<PermissionVO> result2 = rolePermissionQueryTool.queryPermissionsByRole("ADMIN");
            List<PermissionVO> result3 = rolePermissionQueryTool.queryPermissionsByRole("Admin");

            // Then
            assertEquals(result1.size(), result2.size());
            assertEquals(result2.size(), result3.size());
        }
    }
}
