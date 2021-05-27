package org.jujubeframework.util;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

public class ConcurrentsTest {

    @Test
    public void testExecOfTimeout() {
        AtomicInteger result = new AtomicInteger();
        Integer code = Concurrents.execOfTimeout(() -> {
            try {
                Thread.sleep(150);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return 1;
        }, 100, t -> {
            result.set(-1);
        });
        if (code != null) {
            result.set(code);
        }
        assertThat(result.get()).isEqualTo(-1);
    }

    @Test
    public void await() {
        AtomicInteger i = new AtomicInteger();
        long begin = System.currentTimeMillis();
        int max = 5;
        int intervalTime = 100;
        Concurrents.await(() -> i.getAndIncrement() > max, intervalTime);
        assertThat(System.currentTimeMillis() - begin).isGreaterThanOrEqualTo(max * intervalTime);
    }

    @Test
    public void await2() {
        AtomicInteger i = new AtomicInteger();
        long begin = System.currentTimeMillis();
        int max = 5;
        int intervalTime = 100;
        Concurrents.await(() -> i.getAndIncrement() > max, intervalTime, max - 1);
        assertThat(System.currentTimeMillis() - begin).isGreaterThanOrEqualTo((max - 1) * intervalTime);
    }
}
