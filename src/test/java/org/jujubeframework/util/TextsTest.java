package org.jujubeframework.util;

import org.apache.commons.text.StringEscapeUtils;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TextsTest {

    @Test
    public void getChinese() {
        assertThat(Texts.getChinese("hello李world万贯")).isEqualTo("李万贯");
        assertThat(Texts.getChinese("*李--万贯")).isEqualTo("李万贯");
        assertThat(Texts.getChinese("*李|万贯·")).isEqualTo("李万贯");
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

        assertThat(Texts.format("{0}-{1}", 1, 2)).isEqualTo("1-2");
    }

    @Test
    public void truncate() {
        assertThat(Texts.truncate("123", 1)).isEqualTo("12...");
        assertThat(Texts.truncate("abc", 1)).isEqualTo("ab...");
        assertThat(Texts.truncate("abcdefg", 4)).isEqualTo("abcdefg");
        assertThat(Texts.truncate("123456", 3)).isEqualTo("123456");
        assertThat(Texts.truncate("123中文", 4)).isEqualTo("123中文");
        assertThat(Texts.truncate("123中文", 2)).isEqualTo("123中...");
        assertThat(Texts.truncate("中文国家", 3)).isEqualTo("中文国...");
        assertThat(Texts.truncate("中文国家", 4)).isEqualTo("中文国家");
    }

    @Test
    public void unescapeHtml() {
        String html4 = StringEscapeUtils.escapeHtml4("http://\"");
        assertThat(html4).isEqualTo("http://&quot;");
        html4 = Texts.unescapeHtml("http://&quot;");
        assertThat(html4).isEqualTo("http://\"");
    }

    @Test
    public void getGroup() {
        String group = Texts.getGroup("^[a-zA-Z]*", "B08");
        assertThat(group).isEqualTo("B");

        group = Texts.getGroup("\\(.*?\\)", "22 Feb 2020 11:00 CET (10:00 GMT)");
        assertThat(group).isEqualTo("(10:00 GMT)");
    }

    @Test
    public void getHideName() {
        assertThat(Texts.getHideName("13478967895", 4, 3, 4)).isEqualTo("134****7895");
        assertThat(Texts.getHideName("1347896789", 4, 3, 4)).isEqualTo("134****6789");
        assertThat(Texts.getHideName("134896789", 4, 3, 4)).isEqualTo("134****6789");
        assertThat(Texts.getHideName("13489", 4, 3, 4)).isEqualTo("134****9");
        assertThat(Texts.getHideName("189", 4, 3, 4)).isEqualTo("189****");
    }

    @Test
    public void regQuery() {
        List<Texts.RegexQueryInfo> regexQueryInfos = Texts.regQuery("offset=(\\w+)", "&offset=5#");
        assertThat(regexQueryInfos).hasSize(1);
        assertThat(regexQueryInfos.get(0).getGroup()).isEqualTo("offset=5");
        assertThat(regexQueryInfos.get(0).getGroups().get(0)).isEqualTo("5");
    }

    @Test
    public void replaceBlank(){
        assertThat(Texts.replaceBlank("78 78 ")).isEqualTo("7878");
        assertThat(Texts.replaceBlank(" 1")).isEqualTo("1");
        assertThat(Texts.replaceBlank("1\n2")).isEqualTo("12");
        assertThat(Texts.replaceBlank("1  \n  2")).isEqualTo("12");
    }
}
