package com.yfs.util.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class DesUtilTest {

    @Test
    public void testEncrypt() throws Exception {
        String data = "123456";
        String key = "abc1er45";
        String expected = "OkpdwDxRf6o=";
        assertThat(DesUtil.encrypt(data, key)).isEqualTo(expected);
        assertThat(DesUtil.decrypt(expected, key)).isEqualTo(data);
    }

}
