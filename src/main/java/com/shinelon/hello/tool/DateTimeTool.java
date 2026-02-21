package com.shinelon.hello.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 日期时间工具
 * 提供获取当前日期、时间、星期等功能的工具
 *
 * @author shinelon
 */
@Component
public class DateTimeTool {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    /**
     * 星期名称数组（周一到周日）
     */
    private static final String[] WEEK_DAY_NAMES = {"星期一", "星期二", "星期三", "星期四", "星期五", "星期六", "星期日"};

    /**
     * 获取当前的日期和时间
     *
     * @return 当前日期时间字符串，格式：yyyy-MM-dd HH:mm:ss
     */
    @Tool(description = "获取当前的日期和时间，返回格式为 yyyy-MM-dd HH:mm:ss")
    public String getCurrentDateTime() {
        return LocalDateTime.now().format(DATETIME_FORMATTER);
    }

    /**
     * 获取当前日期
     *
     * @return 当前日期字符串，格式：yyyy-MM-dd
     */
    @Tool(description = "获取当前的日期，返回格式为 yyyy-MM-dd")
    public String getCurrentDate() {
        return LocalDate.now().format(DATE_FORMATTER);
    }

    /**
     * 获取当前时间
     *
     * @return 当前时间字符串，格式：HH:mm:ss
     */
    @Tool(description = "获取当前的时间，返回格式为 HH:mm:ss")
    public String getCurrentTime() {
        return LocalDateTime.now().format(TIME_FORMATTER);
    }

    /**
     * 获取当前是星期几
     *
     * @return 星期几的中文名称
     */
    @Tool(description = "获取当前是星期几，返回星期几的中文名称")
    public String getDayOfWeek() {
        DayOfWeek dayOfWeek = LocalDate.now().getDayOfWeek();
        return WEEK_DAY_NAMES[dayOfWeek.getValue() - 1];
    }

    /**
     * 获取指定日期是星期几
     *
     * @param date 日期字符串，格式：yyyy-MM-dd
     * @return 星期几的中文名称
     */
    @Tool(description = "获取指定日期是星期几，参数为日期字符串，格式为 yyyy-MM-dd")
    public String getDayOfWeekByDate(
            @ToolParam(description = "日期字符串，格式为 yyyy-MM-dd，例如 2024-01-15") String date) {
        try {
            LocalDate localDate = LocalDate.parse(date, DATE_FORMATTER);
            DayOfWeek dayOfWeek = localDate.getDayOfWeek();
            return WEEK_DAY_NAMES[dayOfWeek.getValue() - 1];
        } catch (Exception e) {
            return "日期格式错误，请使用 yyyy-MM-dd 格式";
        }
    }
}
