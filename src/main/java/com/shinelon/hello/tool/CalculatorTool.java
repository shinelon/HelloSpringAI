package com.shinelon.hello.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 计算器工具
 * 提供基本的数学计算功能
 *
 * @author shinelon
 */
@Component
public class CalculatorTool {

    private static final int SCALE = 10;

    /**
     * 加法运算
     *
     * @param a 第一个数
     * @param b 第二个数
     * @return 两数之和
     */
    @Tool(description = "计算两个数的和，返回 a + b 的结果")
    public String add(
            @ToolParam(description = "第一个加数") double a,
            @ToolParam(description = "第二个加数") double b) {
        BigDecimal result = BigDecimal.valueOf(a).add(BigDecimal.valueOf(b));
        return formatResult(result);
    }

    /**
     * 减法运算
     *
     * @param a 被减数
     * @param b 减数
     * @return 两数之差
     */
    @Tool(description = "计算两个数的差，返回 a - b 的结果")
    public String subtract(
            @ToolParam(description = "被减数") double a,
            @ToolParam(description = "减数") double b) {
        BigDecimal result = BigDecimal.valueOf(a).subtract(BigDecimal.valueOf(b));
        return formatResult(result);
    }

    /**
     * 乘法运算
     *
     * @param a 第一个因数
     * @param b 第二个因数
     * @return 两数之积
     */
    @Tool(description = "计算两个数的乘积，返回 a * b 的结果")
    public String multiply(
            @ToolParam(description = "第一个因数") double a,
            @ToolParam(description = "第二个因数") double b) {
        BigDecimal result = BigDecimal.valueOf(a).multiply(BigDecimal.valueOf(b));
        return formatResult(result);
    }

    /**
     * 除法运算
     *
     * @param a 被除数
     * @param b 除数
     * @return 两数之商
     * @throws IllegalArgumentException 当除数为0时抛出
     */
    @Tool(description = "计算两个数的商，返回 a / b 的结果。注意：除数不能为0")
    public String divide(
            @ToolParam(description = "被除数") double a,
            @ToolParam(description = "除数，不能为0") double b) {
        if (b == 0) {
            throw new IllegalArgumentException("除数不能为0");
        }
        BigDecimal result = BigDecimal.valueOf(a).divide(BigDecimal.valueOf(b), SCALE, RoundingMode.HALF_UP);
        return formatResult(result);
    }

    /**
     * 幂运算
     *
     * @param base     底数
     * @param exponent 指数
     * @return 幂运算结果
     */
    @Tool(description = "计算 base 的 exponent 次幂，返回 base^exponent 的结果")
    public String power(
            @ToolParam(description = "底数") double base,
            @ToolParam(description = "指数") double exponent) {
        double result = Math.pow(base, exponent);
        return formatResult(BigDecimal.valueOf(result));
    }

    /**
     * 平方根运算
     *
     * @param number 要计算平方根的数
     * @return 平方根
     * @throws IllegalArgumentException 当数为负数时抛出
     */
    @Tool(description = "计算一个数的平方根，返回 sqrt(number) 的结果。注意：数必须非负")
    public String squareRoot(
            @ToolParam(description = "要计算平方根的数，必须非负") double number) {
        if (number < 0) {
            throw new IllegalArgumentException("不能对负数求平方根");
        }
        double result = Math.sqrt(number);
        return formatResult(BigDecimal.valueOf(result));
    }

    /**
     * 取模运算
     *
     * @param a 被除数
     * @param b 除数
     * @return 余数
     * @throws IllegalArgumentException 当除数为0时抛出
     */
    @Tool(description = "计算 a 除以 b 的余数，返回 a % b 的结果")
    public String modulo(
            @ToolParam(description = "被除数") double a,
            @ToolParam(description = "除数") double b) {
        if (b == 0) {
            throw new IllegalArgumentException("除数不能为0");
        }
        BigDecimal result = BigDecimal.valueOf(a).remainder(BigDecimal.valueOf(b));
        return formatResult(result);
    }

    /**
     * 格式化结果，去除不必要的尾随零
     */
    private String formatResult(BigDecimal value) {
        return value.stripTrailingZeros().toPlainString();
    }
}
