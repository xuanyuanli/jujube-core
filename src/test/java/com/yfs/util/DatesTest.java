package com.yfs.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class DatesTest {

    @Test
    public void test() {
        assertThat(Dates.now()).isGreaterThan(1);
    }

}
