package org.jujubeframework.util.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class AesUtilTest {

    @Test
    public void test() {
        String data = "123456";
        String key = "abcde123";
        String expected = "u2TDMZTBmDacbHWR132xqg==";
        assertThat(AesUtil.encrypt(data, key)).isEqualTo(expected);
        assertThat(AesUtil.decrypt(expected, key)).isEqualTo(data);
    }

}
