package org.jujubeframework.constant;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * 编码常量定义
 *
 * @author John Li
 */
public interface Charsets {

    Charset ISO_8859_1 = StandardCharsets.ISO_8859_1;

    Charset US_ASCII = StandardCharsets.US_ASCII;

    Charset UTF_16 = StandardCharsets.UTF_16;

    Charset UTF_16BE = StandardCharsets.UTF_16BE;

    Charset UTF_16LE = StandardCharsets.UTF_16LE;

    Charset UTF_8 = StandardCharsets.UTF_8;

    Charset GBK = Charset.forName("GBK");
}
