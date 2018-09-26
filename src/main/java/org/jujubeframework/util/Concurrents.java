package org.jujubeframework.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jujubeframework.util.support.concurrent.PreNameThreadFactory;
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

    protected static Logger logger = LoggerFactory.getLogger(Concurrents.class);

    /**
     * 带超时设置的执行
     *
     * @param execBody             执行主体
     * @param timeout              超时时间,单位为毫秒
     * @param timeoutExceptionCall 超时异常的回调
     */
    @SuppressWarnings("unchecked")
    public static <T> T execOfTimeout(Supplier<T> execBody, long timeout, Consumer<Exception> timeoutExceptionCall) {
        final AtomicReference<T> temp = new AtomicReference<>();
        ExecutorService executor = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(50), new PreNameThreadFactory("concurrents-execoftimeout"));
        T result = null;
        Future<Boolean> future = executor.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                temp.set(execBody.get());
                return true;
            }
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
}
