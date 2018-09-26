package org.jujubeframework.util.support.concurrent;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author John Li
 */
public class PreNameThreadFactory implements ThreadFactory {
    final static AtomicLong COUNTER = new AtomicLong();

    private String threadName;

    @Override
    public Thread newThread(Runnable r) {
        Thread thread = new Thread(r);
        thread.setName(threadName + "-" + COUNTER.incrementAndGet());
        return thread;
    }

    public PreNameThreadFactory(String threadName) {
        this.threadName = threadName;
    }

}
