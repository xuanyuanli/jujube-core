package org.jujubeframework.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 并发工具类
 *
 * @author John Li
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Concurrents {

    protected final static Logger logger = LoggerFactory.getLogger(Concurrents.class);

    /**
     * 带超时设置的执行
     *
     * @param execBody
     *            执行主体
     * @param timeout
     *            超时时间,单位为毫秒
     * @param timeoutExceptionCall
     *            超时异常的回调
     */
    public static <T> T execOfTimeout(Supplier<T> execBody, long timeout, Consumer<Exception> timeoutExceptionCall) {
        final AtomicReference<T> temp = new AtomicReference<>();
        ExecutorService executor = createThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, 50, "concurrents-execoftimeout-");
        T result = null;
        Future<Boolean> future = executor.submit(() -> {
            temp.set(execBody.get());
            return true;
        });
        try {
            future.get(timeout, TimeUnit.MILLISECONDS);
            result = temp.get();
        } catch (TimeoutException e) {// 超时异常
            future.cancel(true);
            if (timeoutExceptionCall != null) {
                timeoutExceptionCall.accept(e);
            } else {
                logger.error("execOfTimeout()-TimeoutException", e);
            }
        } catch (Exception e) {
            logger.error("execOfTimeout()-Exception", e);
            future.cancel(true);
        } finally {
            executor.shutdown();
        }
        return result;
    }

    /** 创建一个通用的线程池 */
    public static ExecutorService createThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, int workQueueNum, String threadPrefixName) {
        return new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, new ArrayBlockingQueue<>(workQueueNum),
                new BasicThreadFactory.Builder().namingPattern(threadPrefixName + "%d").build());
    }

    /**
     * 等待某个任务执行完毕
     *
     * @param supplier
     *            任务是否完成，完成为true
     * @param intervalTime
     *            任务结果获取的间隔时间
     */
    public static void await(Supplier<Boolean> supplier, int intervalTime) {
        while (!supplier.get()) {
            Runtimes.sleep(intervalTime);
        }
    }

    /**
     * 等待某个任务执行完毕
     *
     * @param supplier
     *            任务是否完成，完成为true
     * @param intervalTime
     *            任务结果获取的间隔时间
     */
    public static void await(Supplier<Boolean> supplier, int intervalTime, int maxRetryTime) {
        int num = 0;
        while (!supplier.get() && num++ < maxRetryTime) {
            Runtimes.sleep(intervalTime);
        }
    }
}
