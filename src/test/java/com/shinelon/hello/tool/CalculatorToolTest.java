package com.shinelon.hello.tool;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CalculatorTool 单元测试
 * 使用表格驱动测试风格
 *
 * @author shinelon
 */
@DisplayName("CalculatorTool 测试")
class CalculatorToolTest {

    private CalculatorTool calculatorTool;

    @BeforeEach
    void setUp() {
        calculatorTool = new CalculatorTool();
    }

    /**
     * 基本运算测试用例
     */
    record BinaryOperationTestCase(
            String name,
            double a,
            double b,
            String expected
    ) {}

    /**
     * 错误处理测试用例
     */
    record ErrorHandlingTestCase(
            String name,
            double a,
            double b,
            String expectedErrorFragment
    ) {}

    /**
     * 单参数运算测试用例
     */
    record UnaryOperationTestCase(
            String name,
            double value,
            String expected
    ) {}

    // ==================== 加法测试数据 ====================
    static Stream<BinaryOperationTestCase> addTestCases() {
        return Stream.of(
                new BinaryOperationTestCase("正整数相加", 10, 5, "15"),
                new BinaryOperationTestCase("负数相加", -10, -5, "-15"),
                new BinaryOperationTestCase("正负数相加", 10, -5, "5"),
                new BinaryOperationTestCase("零加数", 0, 5, "5"),
                new BinaryOperationTestCase("数加零", 5, 0, "5"),
                new BinaryOperationTestCase("小数相加", 0.1, 0.2, "0.3"),
                new BinaryOperationTestCase("大小数相加", 1000000, 0.001, "1000000.001"),
                new BinaryOperationTestCase("大数相加", Double.MAX_VALUE, 0, String.valueOf(Double.MAX_VALUE))
        );
    }

    // ==================== 减法测试数据 ====================
    static Stream<BinaryOperationTestCase> subtractTestCases() {
        return Stream.of(
                new BinaryOperationTestCase("正整数相减", 10, 5, "5"),
                new BinaryOperationTestCase("大数减小数", 10, 20, "-10"),
                new BinaryOperationTestCase("负数相减", -10, -5, "-5"),
                new BinaryOperationTestCase("正负数相减", 10, -5, "15"),
                new BinaryOperationTestCase("零减数", 0, 5, "-5"),
                new BinaryOperationTestCase("数减零", 5, 0, "5"),
                new BinaryOperationTestCase("小数相减", 0.3, 0.1, "0.2"),
                new BinaryOperationTestCase("相同数相减", 5, 5, "0")
        );
    }

    // ==================== 乘法测试数据 ====================
    static Stream<BinaryOperationTestCase> multiplyTestCases() {
        return Stream.of(
                new BinaryOperationTestCase("正整数相乘", 10, 5, "50"),
                new BinaryOperationTestCase("负数相乘", -10, -5, "50"),
                new BinaryOperationTestCase("正负数相乘", 10, -5, "-50"),
                new BinaryOperationTestCase("零乘数", 0, 5, "0"),
                new BinaryOperationTestCase("数乘零", 5, 0, "0"),
                new BinaryOperationTestCase("小数相乘", 0.1, 0.2, "0.02"),
                new BinaryOperationTestCase("1乘数", 1, 5, "5"),
                new BinaryOperationTestCase("大数相乘", 1000, 1000, "1000000")
        );
    }

    // ==================== 除法测试数据 ====================
    static Stream<BinaryOperationTestCase> divideTestCases() {
        return Stream.of(
                new BinaryOperationTestCase("正整数相除", 10, 5, "2"),
                new BinaryOperationTestCase("不能整除", 10, 3, "3.3333333333"),
                new BinaryOperationTestCase("负数相除", -10, -5, "2"),
                new BinaryOperationTestCase("正负数相除", 10, -5, "-2"),
                new BinaryOperationTestCase("零除以数", 0, 5, "0"),
                new BinaryOperationTestCase("小数相除", 0.6, 0.2, "3"),
                new BinaryOperationTestCase("1除以数", 10, 10, "1"),
                new BinaryOperationTestCase("大数除以小数", 100, 0.1, "1000")
        );
    }

    static Stream<ErrorHandlingTestCase> divideByZeroTestCases() {
        return Stream.of(
                new ErrorHandlingTestCase("除以零", 10, 0, "除数不能为0"),
                new ErrorHandlingTestCase("负数除以零", -10, 0, "除数不能为0"),
                new ErrorHandlingTestCase("零除以零", 0, 0, "除数不能为0")
        );
    }

    // ==================== 幂运算测试数据 ====================
    static Stream<BinaryOperationTestCase> powerTestCases() {
        return Stream.of(
                new BinaryOperationTestCase("2的3次幂", 2, 3, "8"),
                new BinaryOperationTestCase("10的2次幂", 10, 2, "100"),
                new BinaryOperationTestCase("5的0次幂", 5, 0, "1"),
                new BinaryOperationTestCase("2的-1次幂", 2, -1, "0.5"),
                new BinaryOperationTestCase("0的5次幂", 0, 5, "0"),
                new BinaryOperationTestCase("负数的偶次幂", -2, 2, "4"),
                new BinaryOperationTestCase("负数的奇次幂", -2, 3, "-8"),
                new BinaryOperationTestCase("1的任意次幂", 1, 100, "1")
        );
    }

    // ==================== 平方根测试数据 ====================
    static Stream<UnaryOperationTestCase> squareRootTestCases() {
        return Stream.of(
                new UnaryOperationTestCase("4的平方根", 4, "2"),
                new UnaryOperationTestCase("16的平方根", 16, "4"),
                new UnaryOperationTestCase("0的平方根", 0, "0"),
                new UnaryOperationTestCase("1的平方根", 1, "1"),
                new UnaryOperationTestCase("2的平方根", 2, "1.4142135623730951"),
                new UnaryOperationTestCase("小数的平方根", 0.25, "0.5")
        );
    }

    static Stream<UnaryOperationTestCase> squareRootNegativeTestCases() {
        return Stream.of(
                new UnaryOperationTestCase("-1的平方根", -1, "不能对负数求平方根"),
                new UnaryOperationTestCase("-100的平方根", -100, "不能对负数求平方根"),
                new UnaryOperationTestCase("-0.1的平方根", -0.1, "不能对负数求平方根")
        );
    }

    // ==================== 取模测试数据 ====================
    static Stream<BinaryOperationTestCase> moduloTestCases() {
        return Stream.of(
                new BinaryOperationTestCase("10模3", 10, 3, "1"),
                new BinaryOperationTestCase("15模5", 15, 5, "0"),
                new BinaryOperationTestCase("7模7", 7, 7, "0"),
                new BinaryOperationTestCase("负数取模", -10, 3, "-1"),
                new BinaryOperationTestCase("零取模", 0, 5, "0"),
                new BinaryOperationTestCase("小数取模", 10.5, 3, "1.5")
        );
    }

    static Stream<ErrorHandlingTestCase> moduloByZeroTestCases() {
        return Stream.of(
                new ErrorHandlingTestCase("模零", 10, 0, "除数不能为0"),
                new ErrorHandlingTestCase("零模零", 0, 0, "除数不能为0")
        );
    }

    // ==================== 加法测试 ====================
    @Nested
    @DisplayName("add 加法测试")
    class AddTests {

        @ParameterizedTest(name = "[{index}] {0}: {1} + {2} = {3}")
        @MethodSource("com.shinelon.hello.tool.CalculatorToolTest#addTestCases")
        @DisplayName("加法运算应返回正确结果")
        void add_shouldReturnCorrectResult(BinaryOperationTestCase testCase) {
            // When
            String result = calculatorTool.add(testCase.a(), testCase.b());

            // Then
            assertBigDecimalEquals(testCase.expected(), result, testCase.name());
        }
    }

    // ==================== 减法测试 ====================
    @Nested
    @DisplayName("subtract 减法测试")
    class SubtractTests {

        @ParameterizedTest(name = "[{index}] {0}: {1} - {2} = {3}")
        @MethodSource("com.shinelon.hello.tool.CalculatorToolTest#subtractTestCases")
        @DisplayName("减法运算应返回正确结果")
        void subtract_shouldReturnCorrectResult(BinaryOperationTestCase testCase) {
            // When
            String result = calculatorTool.subtract(testCase.a(), testCase.b());

            // Then
            assertBigDecimalEquals(testCase.expected(), result, testCase.name());
        }
    }

    // ==================== 乘法测试 ====================
    @Nested
    @DisplayName("multiply 乘法测试")
    class MultiplyTests {

        @ParameterizedTest(name = "[{index}] {0}: {1} * {2} = {3}")
        @MethodSource("com.shinelon.hello.tool.CalculatorToolTest#multiplyTestCases")
        @DisplayName("乘法运算应返回正确结果")
        void multiply_shouldReturnCorrectResult(BinaryOperationTestCase testCase) {
            // When
            String result = calculatorTool.multiply(testCase.a(), testCase.b());

            // Then
            assertBigDecimalEquals(testCase.expected(), result, testCase.name());
        }
    }

    // ==================== 除法测试 ====================
    @Nested
    @DisplayName("divide 除法测试")
    class DivideTests {

        @ParameterizedTest(name = "[{index}] {0}: {1} / {2} = {3}")
        @MethodSource("com.shinelon.hello.tool.CalculatorToolTest#divideTestCases")
        @DisplayName("除法运算应返回正确结果")
        void divide_shouldReturnCorrectResult(BinaryOperationTestCase testCase) {
            // When
            String result = calculatorTool.divide(testCase.a(), testCase.b());

            // Then
            assertBigDecimalEquals(testCase.expected(), result, testCase.name());
        }

        @ParameterizedTest(name = "[{index}] {0}")
        @MethodSource("com.shinelon.hello.tool.CalculatorToolTest#divideByZeroTestCases")
        @DisplayName("除零应抛出异常")
        void divide_byZero_shouldThrowException(ErrorHandlingTestCase testCase) {
            // When & Then
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> calculatorTool.divide(testCase.a(), testCase.b())
            );
            assertTrue(exception.getMessage().contains(testCase.expectedErrorFragment()),
                    () -> testCase.name() + " 应包含错误信息: " + testCase.expectedErrorFragment());
        }
    }

    // ==================== 幂运算测试 ====================
    @Nested
    @DisplayName("power 幂运算测试")
    class PowerTests {

        @ParameterizedTest(name = "[{index}] {0}: {1}^{2} = {3}")
        @MethodSource("com.shinelon.hello.tool.CalculatorToolTest#powerTestCases")
        @DisplayName("幂运算应返回正确结果")
        void power_shouldReturnCorrectResult(BinaryOperationTestCase testCase) {
            // When
            String result = calculatorTool.power(testCase.a(), testCase.b());

            // Then
            assertBigDecimalEquals(testCase.expected(), result, testCase.name());
        }
    }

    // ==================== 平方根测试 ====================
    @Nested
    @DisplayName("squareRoot 平方根测试")
    class SquareRootTests {

        @ParameterizedTest(name = "[{index}] {0}: sqrt({1}) = {2}")
        @MethodSource("com.shinelon.hello.tool.CalculatorToolTest#squareRootTestCases")
        @DisplayName("平方根运算应返回正确结果")
        void squareRoot_shouldReturnCorrectResult(UnaryOperationTestCase testCase) {
            // When
            String result = calculatorTool.squareRoot(testCase.value());

            // Then
            assertBigDecimalEquals(testCase.expected(), result, testCase.name());
        }

        @ParameterizedTest(name = "[{index}] {0}")
        @MethodSource("com.shinelon.hello.tool.CalculatorToolTest#squareRootNegativeTestCases")
        @DisplayName("负数平方根应抛出异常")
        void squareRoot_negativeNumber_shouldThrowException(UnaryOperationTestCase testCase) {
            // When & Then
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> calculatorTool.squareRoot(testCase.value())
            );
            assertTrue(exception.getMessage().contains(testCase.expected()),
                    () -> testCase.name() + " 应包含错误信息: " + testCase.expected());
        }
    }

    // ==================== 取模测试 ====================
    @Nested
    @DisplayName("modulo 取模测试")
    class ModuloTests {

        @ParameterizedTest(name = "[{index}] {0}: {1} % {2} = {3}")
        @MethodSource("com.shinelon.hello.tool.CalculatorToolTest#moduloTestCases")
        @DisplayName("取模运算应返回正确结果")
        void modulo_shouldReturnCorrectResult(BinaryOperationTestCase testCase) {
            // When
            String result = calculatorTool.modulo(testCase.a(), testCase.b());

            // Then
            assertBigDecimalEquals(testCase.expected(), result, testCase.name());
        }

        @ParameterizedTest(name = "[{index}] {0}")
        @MethodSource("com.shinelon.hello.tool.CalculatorToolTest#moduloByZeroTestCases")
        @DisplayName("模零应抛出异常")
        void modulo_byZero_shouldThrowException(ErrorHandlingTestCase testCase) {
            // When & Then
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> calculatorTool.modulo(testCase.a(), testCase.b())
            );
            assertTrue(exception.getMessage().contains(testCase.expectedErrorFragment()),
                    () -> testCase.name() + " 应包含错误信息: " + testCase.expectedErrorFragment());
        }
    }

    // ==================== 辅助方法 ====================

    /**
     * 比较两个数字字符串是否相等（使用BigDecimal）
     */
    private void assertBigDecimalEquals(String expected, String actual, String testName) {
        try {
            BigDecimal expectedValue = new BigDecimal(expected);
            BigDecimal actualValue = new BigDecimal(actual);
            assertTrue(
                    expectedValue.compareTo(actualValue) == 0,
                    () -> testName + " 失败: 期望 " + expected + " 但得到 " + actual
            );
        } catch (NumberFormatException e) {
            assertEquals(expected, actual, testName + " 失败");
        }
    }
}
