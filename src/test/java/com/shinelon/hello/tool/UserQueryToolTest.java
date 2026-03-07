package com.shinelon.hello.tool;

import com.shinelon.hello.data.RbacMockDataService;
import com.shinelon.hello.model.vo.UserInfoVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * UserQueryTool 单元测试
 * 使用表格驱动测试风格
 *
 * @author shinelon
 */
@DisplayName("UserQueryTool 测试")
class UserQueryToolTest {

    private UserQueryTool userQueryTool;
    private RbacMockDataService rbacMockDataService;

    @BeforeEach
    void setUp() {
        rbacMockDataService = new RbacMockDataService();
        rbacMockDataService.init();
        userQueryTool = new UserQueryTool(rbacMockDataService);
    }

    record QueryUserTestCase(
            String name,
            String phone,
            boolean shouldExist,
            String expectedName,
            String expectedCompany,
            int expectedRoleCount
    ) {}

    static Stream<QueryUserTestCase> queryUserTestCases() {
        return Stream.of(
                new QueryUserTestCase("查询管理员张三", "13800138001", true, "张三", "技术部", 1),
                new QueryUserTestCase("查询普通用户李四", "13800138002", true, "李四", "市场部", 1),
                new QueryUserTestCase("查询审计员王五", "13800138003", true, "王五", "财务部", 1),
                new QueryUserTestCase("查询不存在用户", "99999999999", false, null, null, 0)
        );
    }

    @Nested
    @DisplayName("queryUserInfo 查询用户信息测试")
    class QueryUserInfoTests {

        @ParameterizedTest(name = "[{index}] {0}")
        @MethodSource("com.shinelon.hello.tool.UserQueryToolTest#queryUserTestCases")
        @DisplayName("根据手机号查询用户信息应返回正确结果")
        void queryUserInfo_shouldReturnCorrectResult(QueryUserTestCase testCase) {
            // When
            UserInfoVO result = userQueryTool.queryUserInfo(testCase.phone());

            // Then
            if (testCase.shouldExist()) {
                assertNotNull(result, testCase.name() + " 应返回用户信息");
                assertEquals(testCase.expectedName(), result.getName(), testCase.name() + " 用户名不匹配");
                assertEquals(testCase.phone(), result.getPhone(), testCase.name() + " 手机号不匹配");
                assertEquals(testCase.expectedCompany(), result.getCompany(), testCase.name() + " 公司不匹配");
                assertNotNull(result.getRoles(), testCase.name() + " 角色列表不应为null");
                assertEquals(testCase.expectedRoleCount(), result.getRoles().size(), 
                    testCase.name() + " 角色数量不匹配");
            } else {
                assertNull(result, testCase.name() + " 应返回null");
            }
        }

        @Test
        @DisplayName("查询管理员用户应返回完整的角色和权限信息")
        void queryUserInfo_adminUser_shouldReturnRolesWithPermissions() {
            // When
            UserInfoVO result = userQueryTool.queryUserInfo("13800138001");

            // Then
            assertNotNull(result);
            assertEquals("张三", result.getName());
            assertNotNull(result.getRoles());
            assertEquals(1, result.getRoles().size());
            
            UserInfoVO.RoleWithPermissionsVO role = result.getRoles().get(0);
            assertEquals("管理员", role.getRoleName());
            assertEquals("ADMIN", role.getRoleCode());
            assertNotNull(role.getPermissions());
            assertTrue(role.getPermissions().size() > 0, "管理员应有权限");
        }

        @Test
        @DisplayName("查询普通用户应返回基础权限")
        void queryUserInfo_normalUser_shouldReturnBasicPermissions() {
            // When
            UserInfoVO result = userQueryTool.queryUserInfo("13800138002");

            // Then
            assertNotNull(result);
            assertEquals("李四", result.getName());
            assertNotNull(result.getRoles());
            assertEquals(1, result.getRoles().size());
            
            UserInfoVO.RoleWithPermissionsVO role = result.getRoles().get(0);
            assertEquals("普通用户", role.getRoleName());
            assertEquals("USER", role.getRoleCode());
            assertNotNull(role.getPermissions());
            assertEquals(4, role.getPermissions().size(), "普通用户应有4个权限");
        }
    }

    @Nested
    @DisplayName("空值和边界测试")
    class NullAndBoundaryTests {

        @Test
        @DisplayName("手机号为null应返回null")
        void queryUserInfo_nullPhone_shouldReturnNull() {
            // When
            UserInfoVO result = userQueryTool.queryUserInfo(null);

            // Then
            assertNull(result);
        }

        @Test
        @DisplayName("手机号为空字符串应返回null")
        void queryUserInfo_emptyPhone_shouldReturnNull() {
            // When
            UserInfoVO result = userQueryTool.queryUserInfo("");

            // Then
            assertNull(result);
        }

        @Test
        @DisplayName("手机号为空格应返回null")
        void queryUserInfo_blankPhone_shouldReturnNull() {
            // When
            UserInfoVO result = userQueryTool.queryUserInfo("   ");

            // Then
            assertNull(result);
        }
    }
}
