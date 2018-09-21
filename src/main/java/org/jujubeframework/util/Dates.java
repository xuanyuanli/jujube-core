package org.jujubeframework.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 日期工具类
 * @author John Li
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Dates {
    public static final int THIRTEEN = 13;
    public static final int TEN = 10;
    public static final int SEVEN = 7;
    private static Logger logger = LoggerFactory.getLogger(Dates.class);

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
     */
    public static String formatTimeMillisByDatePattern(long times) {
        return formatTimeMillis(times, DEFAULT_PATTERNS[3]);
    }

    /**
     * 按照{yyyy-MM-dd HH:mm:ss}格式化时间
     */
    public static String formatTimeMillisByFullDatePattern(long times) {
        return formatTimeMillis(times, DEFAULT_PATTERNS[0]);
    }

    /** 根据pattern转换字符串为Date */
    public static Date parse(String source, String pattern) {
        try {
            return new SimpleDateFormat(pattern).parse(source);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    /** 根据pattern和时区转换字符串为Date */
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

    /** 根据{@value #DEFAULT_PATTERNS}转换字符串为Date */
    public static Date parse(String source) {
        for (String pattern : DEFAULT_PATTERNS) {
            try {
                return new SimpleDateFormat(pattern).parse(source);
            } catch (ParseException e) {
                continue;
            }
        }
        throw new RuntimeException("找不到适合的pattern");
    }

    /**
     * @see {@link #parse(String, String)}
     */
    public static long parseToTimeMillis(String source, String pattern) {
        return parse(source, pattern).getTime();
    }

    /**
     * @see {@link #parse(String)}
     */
    public static long parseToTimeMillis(String source) {
        return parse(source).getTime();
    }

    /**
     * 获取当天结束时间
     * 
     * @param datestr
     * @return
     */
    public static long endOfDate(String datestr) {
        String today = datestr + " 23:59:59";
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
     * @return
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
     * 根据时间返回是星期几 0周日 1周一 2周二 3周三 4周四 5 周五6周六
     * 
     * @return
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

    public static long now() {
        return System.currentTimeMillis() / 1000;
    }

    /**
     * 获取当前日期前一个月日期
     */
    public static long getBeforeByMonth() {
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
        return parse(sdf.format(calendar.getTime())).getTime() / 1000;

    }

    /**
     *
     * 获得指定日期前(后)x天的日期
     *
     * @param date
     *            当前日期
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
        return new DateTime(date).dayOfMonth().withMaximumValue().getDayOfMonth();
    }

    /**
     * 获得针对目标时间的倒计时
     * 
     * @param descTime
     *            目标时间
     * @return 数组中四个元素，依次是：日、时、分、秒
     */
    public static long[] countDown(long descTime) {
        return endDown(now(), descTime);
    }

    /**
     * 计算两个日期之间的差
     * 
     * @param startTime
     *            开始时间
     * @param endTime
     *            结束时间
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
     * 获取当月日历天数
     */
    public static List<Date> getMonthDateList(Date date) {
        Calendar calendar = Calendar.getInstance();
        List<Date> list = new ArrayList<Date>();
        calendar.setTime(date);
        calendar.set(Calendar.DATE, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(calendar.getTime());
        // 获取当前日期是周几
        int week = getWeek(cal);
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
        week =getWeek(cal);

        if (week != SEVEN) {
            week = SEVEN - week;
        }

        int monthCnt = week + day - 1;
        for (int i = 0; i < monthCnt; i++) {
            calendar.add(Calendar.DATE, 1);
            list.add(calendar.getTime());
        }
        return list;
    }

    private static int getWeek(GregorianCalendar cal) {
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
        return week;
    }

    /**
     * 据今天结束还有多少秒
     */
    public static long endOfToday() {
        return (maximumTimeMillisOfToday() - System.currentTimeMillis()) / 1000;
    }

    /**
     * 今天的结束时间
     */
    public static long maximumTimeMillisOfToday() {
        return new DateTime().millisOfDay().withMaximumValue().getMillis();
    }

    /**
     * 今天开始的时间
     */
    public static long beginOfToday() {
        return new DateTime().millisOfDay().withMinimumValue().getMillis();
    }

    public static String formatNow() {
        return formatDate(new Date(), null);
    }

    public static String formatNow(String pattern) {
        return formatDate(new Date(), pattern);
    }

}
