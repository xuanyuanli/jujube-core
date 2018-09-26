package org.jujubeframework.util;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

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

}
