package org.jujubeframework.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class RandomsTest {

    @Test
    public void test() {
        assertThat(Randoms.randomCodes(6)).hasSize(6);
    }

    @Test
    public void randomInt() {
        for (int i = 0; i < 10000; i++) {
            int iMax = i + 100;
            int num = Randoms.randomInt(i, iMax);
            assertThat(num).isGreaterThanOrEqualTo(i).isLessThanOrEqualTo(iMax);
        }
    }
}
