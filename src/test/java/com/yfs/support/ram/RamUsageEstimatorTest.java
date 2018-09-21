package com.yfs.support.ram;

import com.yfs.util.RamUsageEstimator;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.yfs.util.BeansTest.User;
import com.yfs.util.DataGenerator;

public class RamUsageEstimatorTest {

    @Test
    public void testSizeOfObject() {
        Map<String, Object> map = DataGenerator.fullMap();
        Map<String, Object> map2 = new HashMap<>();
        User user = new User();
        user.setAge(12);
        user.setName("zhansgan");
        assertThat(RamUsageEstimator.sizeOf(map)).isGreaterThan(100);
        assertThat(RamUsageEstimator.sizeOf(map2)).isGreaterThan(1);
        assertThat(RamUsageEstimator.sizeOf(user)).isGreaterThan(10);
    }
}
