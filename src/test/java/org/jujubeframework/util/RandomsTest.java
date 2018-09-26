package org.jujubeframework.util;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

public class RandomsTest {

    @Test
    public void test() {
        assertThat(Randoms.randomCodes(6)).hasSize(6);
    }

}
