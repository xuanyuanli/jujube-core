package com.yfs.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class TextsTest {

    @Test
    public void getChinese() {
        assertThat(Texts.getChinese("hello李world万贯")).isEqualTo("李万贯");
    }

    @Test
    public void format() {
        assertThat(Texts.format("{0}-{1}", "1", null)).isEqualTo("1-");
        assertThat(Texts.format("{1}-{0}", "1", "2")).isEqualTo("2-1");
        assertThat(Texts.format("{}-{}", "1", "2")).isEqualTo("1-2");
        assertThat(Texts.format("{}-{}$", "1", "2")).isEqualTo("1-2$");

        assertThat(Texts.format("123")).isEqualTo("123");
        assertThat(Texts.format("123{}")).isEqualTo("123");
        assertThat(Texts.format("123{}456")).isEqualTo("123456");
        assertThat(Texts.format("1{}2{}3", "-")).isEqualTo("1-23");
    }
}
