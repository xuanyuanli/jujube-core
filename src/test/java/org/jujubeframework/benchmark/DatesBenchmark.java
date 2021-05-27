package org.jujubeframework.benchmark;

import org.jujubeframework.util.Dates;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

/**
 * @author ZhaoYanqi
 * @date 2021/4/30 0030
 */
@BenchmarkMode(Mode.AverageTime)
@Warmup(iterations = 1)
@Measurement(iterations = 3, time = 3)
@Threads(3)
@Fork(1)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class DatesBenchmark {

    private static final Long time1 = 1585123364L;
    private static final Long time2 = 1585123364L;

    /** 两个时间戳是否为同一天 */
    static boolean isSameDay() {
        String pattern = "yyyyMMdd";
        String strDay1 = Dates.formatTimeMillis(DatesBenchmark.time1, pattern);
        String strDay2 = Dates.formatTimeMillis(DatesBenchmark.time2, pattern);
        return strDay1.equals(strDay2);
    }

    @Benchmark
    public static void testDateFormat() {
        isSameDay();
    }

    @Benchmark
    public static void testLocalDate() {
        Dates.isSameDay(time1, time2);
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder().include(DatesBenchmark.class.getSimpleName()).build();
        new Runner(opt).run();
    }

}
