package org.jujubeframework.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

/**
 * 日期工具类
 *
 * @author John Li
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Dates {
    private static final int THIRTEEN = 13;
    private static final int TEN = 10;
    private static final int SEVEN = 7;
    private static final Logger logger = LoggerFactory.getLogger(Dates.class);
    private static final ZoneOffset UTC_P8 = ZoneOffset.of("+8");
    private static final String[] DEFAULT_PATTERNS = { "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm", "yyyy-MM-dd HH", "yyyy-MM-dd" };

    /**
     * 格式化时间
     *
     * @param date
     *            待格式化的时间
     * @param pattern
     *            格式化规则
     */
    public static String formatDate(Date date, String pattern) {
        if (date == null) {
            return "";
        }

        String thisPattern = DEFAULT_PATTERNS[0];
        if (StringUtils.isNotBlank(pattern)) {
            thisPattern = pattern;
        }
        return DateFormatUtils.format(date, thisPattern);
    }

    /**
     * 格式化时间
     *
     * @param date
     *            待格式化的时间
     * @param pattern
     *            格式化规则
     * @param zone
     *            时区
     */
    public static String formatDate(Date date, String pattern, TimeZone zone) {
        if (date == null) {
            return "";
        }
        String thisPattern = DEFAULT_PATTERNS[0];
        if (StringUtils.isNotBlank(pattern)) {
            thisPattern = pattern;
        }
        return DateFormatUtils.format(date, thisPattern, zone);
    }

    /**
     * 格式化时间
     *
     * @param time
     *            待格式化的时间
     * @param pattern
     *            格式化规则
     */
    public static String formatTimeMillis(Long time, String pattern) {
        time = time == null ? 0L : time;
        int len = time.toString().length();
        if (!(len == THIRTEEN || len == TEN)) {
            return "";
        }

        Date date = null;
        // 毫秒
        if (len == THIRTEEN) {
            date = new Date(time);
        }
        // 秒
        else if (len == TEN) {
            date = new Date(time * 1000);
        }
        return formatDate(date, pattern);
    }

    /**
     * 按照{yyyy-MM-dd}格式化时间
     *
     * @param times
     *            epoch的秒值或毫秒值
     */
    public static String formatTimeMillisByDatePattern(long times) {
        return formatTimeMillis(times, DEFAULT_PATTERNS[3]);
    }

    /**
     * 按照{yyyy-MM-dd HH:mm:ss}格式化时间
     *
     * @param times
     *            epoch的秒值或毫秒值
     */
    public static String formatTimeMillisByFullDatePattern(long times) {
        return formatTimeMillis(times, DEFAULT_PATTERNS[0]);
    }

    /**
     * 根据pattern规则转换字符串为Date
     */
    public static Date parse(String source, String pattern) {
        try {
            return new SimpleDateFormat(pattern).parse(source);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 根据pattern和时区转换字符串为Date
     */
    public static Date parse(String source, String pattern, TimeZone timeZone) {
        try {
            DateFormat dateFormat = new SimpleDateFormat(pattern);
            dateFormat.setTimeZone(timeZone);
            return dateFormat.parse(source);
        } catch (ParseException e) {
            logger.error("dates", e);
        }
        return null;
    }

    /**
     * 根据{@link Dates#DEFAULT_PATTERNS}转换字符串为Date
     */
    public static Date parse(String source) {
        for (String pattern : DEFAULT_PATTERNS) {
            try {
                return new SimpleDateFormat(pattern).parse(source);
            } catch (ParseException ignored) {
            }
        }
        throw new RuntimeException("找不到适合的pattern");
    }

    /**
     * @see Dates#parse(String, String)
     */
    public static long parseToTimeMillis(String source, String pattern) {
        return parse(source, pattern).getTime();
    }

    /**
     * @see Dates#parse(String)
     */
    public static long parseToTimeMillis(String source) {
        return parse(source).getTime();
    }

    /**
     * 根据时间返回当前是星期几
     *
     * @return 0周日 1周一 2周二 3周三 4周四 5周五 6周六
     */
    public static int getWeekMark(Date date) {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(date);
        // 获取当前日期是周几
        int week = 0;
        switch (cal.get(GregorianCalendar.DAY_OF_WEEK)) {
        case GregorianCalendar.SUNDAY:
            week = 0;
            break;
        case GregorianCalendar.MONDAY:
            week = 1;
            break;
        case GregorianCalendar.TUESDAY:
            week = 2;
            break;
        case GregorianCalendar.WEDNESDAY:
            week = 3;
            break;
        case GregorianCalendar.THURSDAY:
            week = 4;
            break;
        case GregorianCalendar.FRIDAY:
            week = 5;
            break;
        case GregorianCalendar.SATURDAY:
            week = 6;
            break;
        default:
            break;
        }
        return week;
    }

    /**
     * 获得当前时间的epoch秒值
     */
    public static long now() {
        return System.currentTimeMillis() / 1000;
    }

    /**
     * 获取当前日期前一个月日期
     */
    public static Date getBeforeByMonth() {
        // 当前日期
        Date date = new Date();
        // 格式化对象
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        // 日历对象
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        // 月份减一
        calendar.add(Calendar.MONTH, -1);
        // 输出格式化的日期
        return parse(sdf.format(calendar.getTime()));
    }

    /**
     * 获得指定日期前(后)x天的日期
     *
     * @param time
     *            yyyy-MM-dd HH:mm:ss格式的当前日期
     * @param day
     *            天数（如果day数为负数,说明是此日期前的天数）
     */
    public static long beforNumDay(long time, int day) {
        Calendar c = Calendar.getInstance();
        c.setTime(parse(Dates.formatTimeMillis(time, "yyyy-MM-dd HH:mm:ss")));
        c.add(Calendar.DAY_OF_YEAR, day);
        return parse(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(c.getTime())).getTime() / 1000;
    }

    /**
     * 获取当前月份的最后一天
     */
    public static int getLastDayOfMonth(Date date) {
        LocalDateTime localDate = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        return localDate.with(TemporalAdjusters.lastDayOfMonth()).getDayOfMonth();
    }

    /**
     * 获取当前月份的最后一天
     */
    public static int getLastDayOfMonth() {
        return getLastDayOfMonth(new Date());
    }

    /**
     * 获得针对目标时间的倒计时
     *
     * @param descTime
     *            epoch格式的目标时间
     * @return 数组中四个元素，依次是：日、时、分、秒
     */
    public static long[] countDown(long descTime) {
        return endDown(now(), descTime);
    }

    /**
     * 计算两个日期之间的差
     *
     * @param startTime
     *            epoch格式的开始时间
     * @param endTime
     *            epoch格式的结束时间
     * @return 数组中四个元素，依次是：日、时、分、秒
     */
    public static long[] endDown(long startTime, long endTime) {
        String strTime = String.valueOf(endTime);
        int len = strTime.length();
        Validate.isTrue(len == THIRTEEN || len == TEN, "endTime必须为秒或毫秒");

        String strNow = String.valueOf(startTime);
        int len2 = strNow.length();
        Validate.isTrue(len2 == THIRTEEN || len2 == TEN, "startTime必须为秒或毫秒");

        if (len == THIRTEEN) {
            endTime = endTime / 1000;
        }
        if (len2 == THIRTEEN) {
            startTime = startTime / 1000;
        }

        long[] arr = new long[4];
        long second = endTime - startTime;
        if (second < 0) {
            return null;
        }

        long minite = second / 60;
        long hour = minite / 60;
        long day = hour / 24;

        second = second % 60;
        minite = minite % 60;
        hour = hour % 24;

        arr[0] = day;
        arr[1] = hour;
        arr[2] = minite;
        arr[3] = second % 60;
        return arr;
    }

    /**
     * 据今天结束还有多少秒
     */
    public static long endOfToday() {
        return (maximumTimeMillisOfToday() - System.currentTimeMillis()) / 1000;
    }

    /**
     * 今天的结束时间
     *
     * @return 返回millis值
     */
    public static long maximumTimeMillisOfToday() {
        LocalDateTime localDateTime = LocalDateTime.now();
        return Date.from(localDateTime.with(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant()).getTime();
    }

    /**
     * 今天开始的时间
     *
     * @return 返回millis值
     */
    public static long minimumTimeMillisOfToday() {
        LocalDateTime localDateTime = LocalDateTime.now();
        return Date.from(localDateTime.with(LocalTime.MIN).atZone(ZoneId.systemDefault()).toInstant()).getTime();
    }

    /**
     * 以‘yyyy-MM-dd HH:mm:ss’格式化当前日期
     */
    public static String formatNow() {
        return formatDate(new Date(), null);
    }

    /**
     * 以指定的pattern格式化当前日期
     */
    public static String formatNow(String pattern) {
        return formatDate(new Date(), pattern);
    }

    /**
     * 判断是否是连续的日期
     *
     * @param time1
     *            比较时间
     * @param time2
     *            被比较时间
     */
    public static boolean isSerialDay(long time1, long time2) {
        time1 = Dates.beginOfDate(Dates.formatTimeMillisByDatePattern(time1));
        time2 = Dates.beginOfDate(Dates.formatTimeMillisByDatePattern(time2));
        int compare = Calcs.div(Calcs.sub(time1, time2, 0), 86400, 0).intValue();
        return compare >= -1 && compare <= 1;
    }

    /**
     * 获取当天结束时间
     *
     * @param dateStr
     *            yyyy-MM-dd格式的字符串
     */
    public static long endOfDate(String dateStr) {
        String today = dateStr + " 23:59:59";
        SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        try {
            Date date = dateformat.parse(today);
            return date.getTime() / 1000;
        } catch (ParseException e) {
            logger.error("dates", e);
            return 0;
        }
    }

    /**
     * 获取当天开始时间
     *
     * @param datestr
     *            yyyy-MM-dd格式的时间
     * @return 秒值
     */
    public static long beginOfDate(String datestr) {
        String today = datestr + " 00:00:00";
        SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        try {
            Date date = dateformat.parse(today);
            return date.getTime() / 1000;
        } catch (ParseException e) {
            logger.error("dates", e);
            return 0;
        }
    }

    /**
     * 获取日期所在月的起始时间
     */
    public static long beginOfMonth(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        final int last = calendar.getActualMinimum(Calendar.DAY_OF_MONTH);
        calendar.set(Calendar.DAY_OF_MONTH, last);
        return calendar.getTimeInMillis() / 1000;
    }

    /**
     * 获取指定天数后的时间
     */
    public static long getTimeOfLayerDays(Integer validDays) {
        return Calcs.add(Calcs.mul(validDays, 86400, 0), now(), 0).longValue();

    }

    /**
     * 今天北京时间八点
     */
    public static long getTime8() {
        String today = formatTimeMillisByDatePattern(minimumTimeMillisOfToday());
        today = today + " 8:00:00";
        return parseToTimeMillis(today);
    }

    /**
     * 获取时区，如果是GMT+8，则显示北京时间；如果是其他时区，则显示相应的时区
     */
    public static String timeZone(String timeZone) {
        timeZone = timeZone.replaceAll("[0*]|[:]", "");
        return timeZone;
    }

    /**
     * 获取当月日历天数
     */
    public static List<Date> getMonthDateList(Date date) {
        Calendar calendar = Calendar.getInstance();
        List<Date> list = new ArrayList<>();
        calendar.setTime(date);
        calendar.set(Calendar.DATE, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(calendar.getTime());
        // 获取当前日期是周几
        int week = 0;
        switch (cal.get(GregorianCalendar.DAY_OF_WEEK)) {
        case GregorianCalendar.SUNDAY:
            week = 1;
            break;
        case GregorianCalendar.MONDAY:
            week = 2;
            break;
        case GregorianCalendar.TUESDAY:
            week = 3;
            break;
        case GregorianCalendar.WEDNESDAY:
            week = 4;
            break;
        case GregorianCalendar.THURSDAY:
            week = 5;
            break;
        case GregorianCalendar.FRIDAY:
            week = 6;
            break;
        case GregorianCalendar.SATURDAY:
            week = 7;
            break;

        default:
            break;
        }
        calendar.add(Calendar.DATE, -week);
        for (int x = 0; x < week; x++) {
            calendar.add(Calendar.DATE, 1);
            list.add(calendar.getTime());
        }
        // 获取当前月份的最后一天
        int day = Dates.getLastDayOfMonth(date);
        // 计算日历最后补几天
        cal.setTime(date);
        cal.set(Calendar.DATE, day);
        week = 0;
        switch (cal.get(GregorianCalendar.DAY_OF_WEEK)) {
        case GregorianCalendar.SUNDAY:
            week = 1;
            break;
        case GregorianCalendar.MONDAY:
            week = 2;
            break;
        case GregorianCalendar.TUESDAY:
            week = 3;
            break;
        case GregorianCalendar.WEDNESDAY:
            week = 4;
            break;
        case GregorianCalendar.THURSDAY:
            week = 5;
            break;
        case GregorianCalendar.FRIDAY:
            week = 6;
            break;
        case GregorianCalendar.SATURDAY:
            week = 7;
            break;

        default:
            break;
        }

        if (week != 7) {
            week = 7 - week;
        }

        int monthCnt = week + day - 1;
        for (int i = 0; i < monthCnt; i++) {
            calendar.add(Calendar.DATE, 1);
            list.add(calendar.getTime());
        }
        return list;
    }

    /**
     * 获取当月日历天数
     */
    public static List<Date> getTotalMonthDateList(Date date) {
        Calendar calendar = Calendar.getInstance();
        List<Date> list = new ArrayList<>();
        calendar.setTime(date);
        calendar.set(Calendar.DATE, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        // 获取当前月份的最后一天

        int monthCnt = Dates.getLastDayOfMonth(date);
        for (int i = 0; i < monthCnt; i++) {
            calendar.add(Calendar.DATE, 1);
            list.add(calendar.getTime());
        }
        return list;
    }

    /**
     * 查看当前时间是否是当月1日
     */
    public static boolean isFirstDayOfMonth(Long time) {
        Calendar calendar = Calendar.getInstance();
        int len = time.toString().length();
        if (!(len == 13 || len == 10)) {
            return false;
        }
        if (len == 10) {
            time = time * 1000;
        }
        calendar.setTimeInMillis(time);
        return calendar.get(Calendar.DAY_OF_MONTH) == 1;
    }

    /**
     * 获取固定6周的日历时间
     */
    public static List<Date> getCalendarMonth(Date date) {
        List<Date> list = getMonthDateList(date);
        if (list.size() < 42) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(list.get(list.size() - 1));
            for (int i = 0; i < 7; i++) {
                calendar.add(Calendar.DATE, 1);
                list.add(calendar.getTime());
            }
        }
        return list;
    }

    /**
     * 解析一个有起止时间的范围日期
     *
     * @param dataRange
     *            起止时间
     * @param pattern
     *            时间格式
     */
    public static Long[] parseRangeDate(String dataRange, String pattern) {
        long beginTime = 0L;
        long endTime = 0L;
        if (StringUtils.isNotBlank(dataRange)) {
            String[] dateArr = dataRange.split("-");
            beginTime = parse(dateArr[0], pattern).getTime() / 1000;
            endTime = beforNumDay(parse(dateArr[1], pattern).getTime(), 1) - 1;
        }
        return new Long[] { beginTime, endTime };
    }

    /**
     * 解析ACE模板获取的起止日期
     *
     * @param dataRange
     *            起止时间(支持模板:MM/dd/yyyy-MM/dd/yyyy)
     */
    public static Long[] parseAceRangeDate(String dataRange) {
        return parseRangeDate(dataRange, "MM/dd/yyyy");
    }

    /** LocalDate转换为秒值（从1970年初开始计算） */
    public static long getEpochSecond(LocalDate localDate) {
        return localDate.atStartOfDay().toEpochSecond(UTC_P8);
    }

    /** LocalDateTime转换为秒值（从1970年初开始计算） */
    public static long getEpochSecond(LocalDateTime localDateTime) {
        return localDateTime.toEpochSecond(UTC_P8);
    }

    public static LocalDateTime dateToLocalDateTime(Date date) {
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }

    /** 两个时间戳是否为同一天 */
    public static boolean isSameDay(Long time1, Long time2) {
        if (String.valueOf(time1).length() == 13) {
            time1 = time1 / 1000;
        }
        if (String.valueOf(time2).length() == 13) {
            time2 = time2 / 1000;
        }
        LocalDate localDate1 = LocalDate.ofEpochDay(time1 / (3600 * 24));
        LocalDate localDate2 = LocalDate.ofEpochDay(time2 / (3600 * 24));
        return localDate1.isEqual(localDate2);
    }
}
