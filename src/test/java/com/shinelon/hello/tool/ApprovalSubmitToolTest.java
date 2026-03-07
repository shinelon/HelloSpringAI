package com.shinelon.hello.tool;

import com.shinelon.hello.data.RbacMockDataService;
import com.shinelon.hello.model.vo.ApprovalVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ApprovalSubmitTool 单元测试
 * 使用表格驱动测试风格
 *
 * @author shinelon
 */
@DisplayName("ApprovalSubmitTool 测试")
class ApprovalSubmitToolTest {

    private ApprovalSubmitTool approvalSubmitTool;
    private RbacMockDataService rbacMockDataService;

    private static final Pattern APPROVAL_ID_PATTERN = Pattern.compile("APV-\\d{14}-\\d{4}");

    @BeforeEach
    void setUp() {
        rbacMockDataService = new RbacMockDataService();
        rbacMockDataService.init();
        approvalSubmitTool = new ApprovalSubmitTool(rbacMockDataService);
    }

    record SubmitApprovalTestCase(
            String name,
            String applicant,
            String appliedRole,
            String reason
    ) {}

    static Stream<SubmitApprovalTestCase> submitApprovalTestCases() {
        return Stream.of(
                new SubmitApprovalTestCase("提交管理员角色申请", "张三", "管理员", "需要管理系统"),
                new SubmitApprovalTestCase("提交普通用户角色申请", "李四", "普通用户", "需要访问基础功能"),
                new SubmitApprovalTestCase("提交审计员角色申请", "王五", "审计员", "需要审计权限"),
                new SubmitApprovalTestCase("使用角色代码申请", "赵六", "USER", "需要用户权限"),
                new SubmitApprovalTestCase("申请原因较长", "孙七", "AUDITOR", "这是一个很长的申请原因，用于测试系统对长文本的处理能力")
        );
    }

    @Nested
    @DisplayName("submitApproval 提交审批测试")
    class SubmitApprovalTests {

        @ParameterizedTest(name = "[{index}] {0}")
        @MethodSource("com.shinelon.hello.tool.ApprovalSubmitToolTest#submitApprovalTestCases")
        @DisplayName("提交审批应返回正确的审批信息")
        void submitApproval_shouldReturnCorrectApprovalInfo(SubmitApprovalTestCase testCase) {
            // When
            ApprovalVO result = approvalSubmitTool.submitApproval(
                testCase.applicant(),
                testCase.appliedRole(),
                testCase.reason()
            );

            // Then
            assertNotNull(result, testCase.name() + " 应返回审批信息");
            assertNotNull(result.getApprovalId(), testCase.name() + " 审批ID不应为null");
            assertTrue(APPROVAL_ID_PATTERN.matcher(result.getApprovalId()).matches(), 
                testCase.name() + " 审批ID格式应为APV-yyyyMMddHHmmss-xxxx");
            assertEquals("待审批", result.getStatus(), testCase.name() + " 状态应为待审批");
            assertEquals(testCase.applicant(), result.getApplicant(), testCase.name() + " 申请人不匹配");
            assertNotNull(result.getAppliedRole(), testCase.name() + " 申请角色不应为null");
            assertNotNull(result.getSubmitTime(), testCase.name() + " 提交时间不应为null");
        }

        @Test
        @DisplayName("每次提交应生成唯一的审批ID")
        void submitApproval_shouldGenerateUniqueApprovalIds() {
            // When
            ApprovalVO result1 = approvalSubmitTool.submitApproval("张三", "管理员", "原因1");
            ApprovalVO result2 = approvalSubmitTool.submitApproval("李四", "普通用户", "原因2");
            ApprovalVO result3 = approvalSubmitTool.submitApproval("王五", "审计员", "原因3");

            // Then
            assertNotEquals(result1.getApprovalId(), result2.getApprovalId(), "审批ID应唯一");
            assertNotEquals(result2.getApprovalId(), result3.getApprovalId(), "审批ID应唯一");
            assertNotEquals(result1.getApprovalId(), result3.getApprovalId(), "审批ID应唯一");
        }

        @Test
        @DisplayName("审批ID应按时间顺序递增")
        void submitApproval_shouldGenerateSequentialApprovalIds() {
            // When
            ApprovalVO result1 = approvalSubmitTool.submitApproval("张三", "管理员", "原因1");
            
            try {
                Thread.sleep(10); // 确保时间戳不同
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            ApprovalVO result2 = approvalSubmitTool.submitApproval("李四", "普通用户", "原因2");

            // Then
            assertTrue(result2.getApprovalId().compareTo(result1.getApprovalId()) > 0, 
                "后提交的审批ID应大于先提交的");
        }

        @Test
        @DisplayName("审批信息应包含完整字段")
        void submitApproval_shouldReturnCompleteApprovalInfo() {
            // When
            ApprovalVO result = approvalSubmitTool.submitApproval("张三", "管理员", "需要管理系统权限");

            // Then
            assertNotNull(result.getApprovalId());
            assertNotNull(result.getStatus());
            assertNotNull(result.getMessage());
            assertEquals("张三", result.getApplicant());
            assertNotNull(result.getAppliedRole());
            assertNotNull(result.getSubmitTime());
        }

        @Test
        @DisplayName("提交时间格式应正确")
        void submitApproval_shouldReturnCorrectSubmitTimeFormat() {
            // When
            ApprovalVO result = approvalSubmitTool.submitApproval("张三", "管理员", "原因");

            // Then
            assertNotNull(result.getSubmitTime());
            assertTrue(result.getSubmitTime().matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}"),
                "提交时间格式应为 yyyy-MM-dd HH:mm:ss");
        }
    }

    @Nested
    @DisplayName("边界和异常测试")
    class BoundaryAndExceptionTests {

        @Test
        @DisplayName("申请人为null应能正常处理")
        void submitApproval_nullApplicant_shouldHandleGracefully() {
            // When
            ApprovalVO result = approvalSubmitTool.submitApproval(null, "管理员", "原因");

            // Then
            assertNotNull(result);
            assertNull(result.getApplicant());
        }

        @Test
        @DisplayName("申请角色为null应能正常处理")
        void submitApproval_nullAppliedRole_shouldHandleGracefully() {
            // When
            ApprovalVO result = approvalSubmitTool.submitApproval("张三", null, "原因");

            // Then
            assertNotNull(result);
        }

        @Test
        @DisplayName("申请原因为null应能正常处理")
        void submitApproval_nullReason_shouldHandleGracefully() {
            // When
            ApprovalVO result = approvalSubmitTool.submitApproval("张三", "管理员", null);

            // Then
            assertNotNull(result);
        }

        @Test
        @DisplayName("所有参数为空应能正常处理")
        void submitApproval_allNull_shouldHandleGracefully() {
            // When
            ApprovalVO result = approvalSubmitTool.submitApproval(null, null, null);

            // Then
            assertNotNull(result);
            assertNotNull(result.getApprovalId());
            assertEquals("待审批", result.getStatus());
        }

        @Test
        @DisplayName("超长申请人名称应能正常处理")
        void submitApproval_veryLongApplicantName_shouldHandleGracefully() {
            // Given
            String longName = "张".repeat(1000);

            // When
            ApprovalVO result = approvalSubmitTool.submitApproval(longName, "管理员", "原因");

            // Then
            assertNotNull(result);
            assertEquals(longName, result.getApplicant());
        }
    }
}
