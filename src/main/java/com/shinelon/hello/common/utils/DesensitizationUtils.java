package com.shinelon.hello.common.utils;

/**
 * 敏感信息脱敏工具类
 * 用于日志输出时对敏感信息进行脱敏处理
 *
 * @author shinelon
 */
public final class DesensitizationUtils {

    private DesensitizationUtils() {
        // 工具类禁止实例化
    }

    /**
     * 脱敏后缀保留长度
     */
    private static final int SUFFIX_LENGTH = 4;

    /**
     * 最小脱敏长度（小于此长度不脱敏）
     */
    private static final int MIN_DESENSITIZE_LENGTH = 8;

    /**
     * 对ID进行脱敏
     * 保留前4位和后4位，中间用*替代
     *
     * @param id 需要脱敏的ID
     * @return 脱敏后的ID
     */
    public static String maskId(String id) {
        if (id == null || id.isEmpty()) {
            return id;
        }
        if (id.length() < MIN_DESENSITIZE_LENGTH) {
            return maskAll(id);
        }
        int prefixLength = SUFFIX_LENGTH;
        int suffixLength = SUFFIX_LENGTH;
        String prefix = id.substring(0, prefixLength);
        String suffix = id.substring(id.length() - suffixLength);
        return prefix + "****" + suffix;
    }

    /**
     * 对内容进行截断并脱敏
     * 先截断到指定长度，再进行简单脱敏
     *
     * @param content   需要处理的内容
     * @param maxLength 最大长度
     * @return 截断脱敏后的内容
     */
    public static String truncateAndMask(String content, int maxLength) {
        if (content == null || content.isEmpty()) {
            return content;
        }
        String truncated = content.length() > maxLength
                ? content.substring(0, maxLength) + "..."
                : content;
        // 对截断后的内容，只保留前后部分
        if (truncated.length() > MIN_DESENSITIZE_LENGTH) {
            int halfLength = truncated.length() / 2;
            String prefix = truncated.substring(0, Math.min(halfLength / 2, 10));
            String suffix = truncated.substring(truncated.length() - Math.min(halfLength / 2, 10));
            return prefix + "***" + suffix;
        }
        return truncated;
    }

    /**
     * 全部脱敏（用*替代）
     *
     * @param str 需要脱敏的字符串
     * @return 脱敏后的字符串
     */
    private static String maskAll(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            sb.append('*');
        }
        return sb.toString();
    }
}
