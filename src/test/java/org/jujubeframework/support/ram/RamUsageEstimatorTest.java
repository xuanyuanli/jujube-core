package org.jujubeframework.support.ram;

import org.assertj.core.api.Assertions;
import static org.assertj.core.api.Assertions.assertThat;
import org.jujubeframework.util.BeansTest;
import org.jujubeframework.util.DataGenerator;
import org.jujubeframework.util.RamUsageEstimator;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class RamUsageEstimatorTest {

    @Test
    public void testSizeOfObject() {
        Map<String, Object> map = DataGenerator.fullMap();
        Map<String, Object> map2 = new HashMap<>();
        BeansTest.User user = new BeansTest.User();
        user.setAge(12);
        user.setName("zhansgan");
        Assertions.assertThat(RamUsageEstimator.sizeOf(map)).isGreaterThan(100);
        assertThat(RamUsageEstimator.sizeOf(map2)).isGreaterThan(1);
        assertThat(RamUsageEstimator.sizeOf(user)).isGreaterThan(10);
    }
}
