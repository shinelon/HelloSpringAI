package com.shinelon.hello.common.constants;

/**
 * 通用常量类
 *
 * @author shinelon
 */
public final class CommonConstants {

    private CommonConstants() {
        // 常量类禁止实例化
    }

    /**
     * 消息内容最大长度
     */
    public static final int CONTENT_MAX_LENGTH = 4000;

    /**
     * 标题最大长度
     */
    public static final int TITLE_MAX_LENGTH = 50;

    /**
     * 日志截断长度
     */
    public static final int LOG_TRUNCATE_LENGTH = 50;

    /**
     * 会话ID长度（UUID格式）
     */
    public static final int SESSION_ID_LENGTH = 36;
}
