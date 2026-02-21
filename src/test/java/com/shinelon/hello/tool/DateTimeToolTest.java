package com.shinelon.hello.tool;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DateTimeTool 单元测试
 * 使用表格驱动测试风格
 *
 * @author shinelon
 */
@DisplayName("DateTimeTool 测试")
class DateTimeToolTest {

    private DateTimeTool dateTimeTool;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    @BeforeEach
    void setUp() {
        dateTimeTool = new DateTimeTool();
    }

    /**
     * 测试用例数据
     */
    record DayOfWeekTestCase(
            String name,
            String date,
            String expectedDayOfWeek
    ) {}

    static Stream<DayOfWeekTestCase> dayOfWeekTestCases() {
        return Stream.of(
                new DayOfWeekTestCase("2024-01-01 是星期一", "2024-01-01", "星期一"),
                new DayOfWeekTestCase("2024-01-07 是星期日", "2024-01-07", "星期日"),
                new DayOfWeekTestCase("2024-02-14 是星期三", "2024-02-14", "星期三"),
                new DayOfWeekTestCase("2024-12-31 是星期二", "2024-12-31", "星期二"),
                new DayOfWeekTestCase("2025-02-21 是星期五", "2025-02-21", "星期五")
        );
    }

    static Stream<DayOfWeekTestCase> invalidDateTestCases() {
        return Stream.of(
                new DayOfWeekTestCase("无效格式 - 缺少分隔符", "20240101", "日期格式错误"),
                new DayOfWeekTestCase("无效格式 - 错误分隔符", "2024/01/01", "日期格式错误"),
                new DayOfWeekTestCase("无效日期 - 月份超出范围", "2024-13-01", "日期格式错误"),
                new DayOfWeekTestCase("无效日期 - 日期超出范围", "2024-01-32", "日期格式错误"),
                new DayOfWeekTestCase("空字符串", "", "日期格式错误"),
                new DayOfWeekTestCase("null值处理", null, "日期格式错误"),
                new DayOfWeekTestCase("随机字符串", "invalid-date", "日期格式错误")
        );
    }

    @Nested
    @DisplayName("getCurrentDateTime 测试")
    class GetCurrentDateTimeTests {

        @Test
        @DisplayName("应返回 yyyy-MM-dd HH:mm:ss 格式的日期时间")
        void getCurrentDateTime_shouldReturnCorrectFormat() {
            // When
            String result = dateTimeTool.getCurrentDateTime();

            // Then
            assertNotNull(result);
            assertTrue(result.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}"),
                    "日期时间格式应为 yyyy-MM-dd HH:mm:ss");

            // 验证能被正确解析
            assertDoesNotThrow(() -> LocalDateTime.parse(result, DATETIME_FORMATTER));
        }

        @Test
        @DisplayName("返回的时间应接近当前时间")
        void getCurrentDateTime_shouldBeCloseToNow() {
            // Given
            LocalDateTime before = LocalDateTime.now().minusSeconds(1);

            // When
            String result = dateTimeTool.getCurrentDateTime();
            LocalDateTime resultDateTime = LocalDateTime.parse(result, DATETIME_FORMATTER);

            // Then
            LocalDateTime after = LocalDateTime.now().plusSeconds(1);
            // 允许1秒误差
            assertTrue(
                    !resultDateTime.isBefore(before) && !resultDateTime.isAfter(after),
                    "返回的时间应接近当前时间"
            );
        }
    }

    @Nested
    @DisplayName("getCurrentDate 测试")
    class GetCurrentDateTests {

        @Test
        @DisplayName("应返回 yyyy-MM-dd 格式的日期")
        void getCurrentDate_shouldReturnCorrectFormat() {
            // When
            String result = dateTimeTool.getCurrentDate();

            // Then
            assertNotNull(result);
            assertTrue(result.matches("\\d{4}-\\d{2}-\\d{2}"),
                    "日期格式应为 yyyy-MM-dd");

            // 验证能被正确解析
            assertDoesNotThrow(() -> LocalDate.parse(result, DATE_FORMATTER));
        }

        @Test
        @DisplayName("返回的日期应是今天")
        void getCurrentDate_shouldBeToday() {
            // When
            String result = dateTimeTool.getCurrentDate();

            // Then
            assertEquals(LocalDate.now().format(DATE_FORMATTER), result);
        }
    }

    @Nested
    @DisplayName("getCurrentTime 测试")
    class GetCurrentTimeTests {

        @Test
        @DisplayName("应返回 HH:mm:ss 格式的时间")
        void getCurrentTime_shouldReturnCorrectFormat() {
            // When
            String result = dateTimeTool.getCurrentTime();

            // Then
            assertNotNull(result);
            assertTrue(result.matches("\\d{2}:\\d{2}:\\d{2}"),
                    "时间格式应为 HH:mm:ss");
        }

        @Test
        @DisplayName("返回的时间应包含有效的时间值")
        void getCurrentTime_shouldContainValidTimeValues() {
            // When
            String result = dateTimeTool.getCurrentTime();
            String[] parts = result.split(":");

            // Then
            int hours = Integer.parseInt(parts[0]);
            int minutes = Integer.parseInt(parts[1]);
            int seconds = Integer.parseInt(parts[2]);

            assertTrue(hours >= 0 && hours <= 23, "小时应在0-23之间");
            assertTrue(minutes >= 0 && minutes <= 59, "分钟应在0-59之间");
            assertTrue(seconds >= 0 && seconds <= 59, "秒应在0-59之间");
        }
    }

    @Nested
    @DisplayName("getDayOfWeek 测试")
    class GetDayOfWeekTests {

        @Test
        @DisplayName("应返回中文星期名称")
        void getDayOfWeek_shouldReturnChineseName() {
            // When
            String result = dateTimeTool.getDayOfWeek();

            // Then
            assertNotNull(result);
            assertTrue(result.startsWith("星期"),
                    "星期名称应以'星期'开头");
        }

        @Test
        @DisplayName("返回的星期应与当前日期对应")
        void getDayOfWeek_shouldMatchCurrentDate() {
            // Given
            DayOfWeek expected = LocalDate.now().getDayOfWeek();
            String[] weekDays = {"星期一", "星期二", "星期三", "星期四", "星期五", "星期六", "星期日"};
            String expectedDay = weekDays[expected.getValue() - 1];

            // When
            String result = dateTimeTool.getDayOfWeek();

            // Then
            assertEquals(expectedDay, result);
        }
    }

    @Nested
    @DisplayName("getDayOfWeekByDate 测试")
    class GetDayOfWeekByDateTests {

        @ParameterizedTest(name = "[{index}] {0}")
        @MethodSource("com.shinelon.hello.tool.DateTimeToolTest#dayOfWeekTestCases")
        @DisplayName("有效日期应返回正确的星期几")
        void getDayOfWeekByDate_validDate_shouldReturnCorrectDayOfWeek(DayOfWeekTestCase testCase) {
            // When
            String result = dateTimeTool.getDayOfWeekByDate(testCase.date());

            // Then
            assertEquals(testCase.expectedDayOfWeek(), result,
                    () -> testCase.name() + " 失败");
        }

        @ParameterizedTest(name = "[{index}] {0}")
        @MethodSource("com.shinelon.hello.tool.DateTimeToolTest#invalidDateTestCases")
        @DisplayName("无效日期应返回格式错误提示")
        void getDayOfWeekByDate_invalidDate_shouldReturnErrorMessage(DayOfWeekTestCase testCase) {
            // When
            String result = dateTimeTool.getDayOfWeekByDate(testCase.date());

            // Then
            assertTrue(result.contains("日期格式错误"),
                    () -> testCase.name() + " 应返回日期格式错误提示，实际返回: " + result);
        }
    }
}
