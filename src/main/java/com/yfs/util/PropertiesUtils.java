package com.yfs.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @author John Li
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PropertiesUtils {

    /**
     * 保存内容到相应路径
     * 
     * @see {@link saveProperties(String descPath, List<String> content, String
     *      encoding, boolean escapeUnicode)}
     */
    public static void saveProperties(String descPath, String content, String encoding, boolean escapeUnicode) throws IOException {
        String[] lines = content.split("\n");
        saveProperties(descPath, Arrays.asList(lines), encoding, escapeUnicode);
    }

    /**
     * 保存内容到相应路径
     * 
     * @param descPath
     *            目标路径
     * @param content
     *            内容
     * @param encoding
     *            保存编码
     * @param escapeUnicode
     *            val是否转换为unicode
     * @throws IOException
     */
    public static void saveProperties(String descPath, List<String> content, String encoding, boolean escapeUnicode) throws IOException {
        List<String> data = new ArrayList<>();
        for (String line : content) {
            String newLine = line;
            if (line != null && line.contains("=")) {
                String[] kv = line.split("=");
                if (kv.length == 2) {
                    String key = kv[0];
                    String val = kv[1];
                    key = saveConvert(key, true, escapeUnicode);
                    val = saveConvert(val, false, escapeUnicode);
                    newLine = key + "=" + val;
                }
            }
            data.add(newLine);
        }
        FileUtils.writeLines(new File(descPath), encoding, data);
    }

    public static String keyConvert(String key) {
        return saveConvert(key, true, true);
    }

    public static String valueConvert(String val) {
        return saveConvert(val, false, true);
    }

    /** 对key和value特殊字符进行转义，参考Properties中的此方法 */
    private static String saveConvert(String theString, boolean escapeSpace, boolean escapeUnicode) {
        int len = theString.length();
        int bufLen = len * 2;
        if (bufLen < 0) {
            bufLen = Integer.MAX_VALUE;
        }
        StringBuffer outBuffer = new StringBuffer(bufLen);

        for (int x = 0; x < len; x++) {
            char aChar = theString.charAt(x);
            // Handle common case first, selecting largest block that
            // avoids the specials below
            if ((aChar > 61) && (aChar < 127)) {
                if (aChar == '\\') {
                    outBuffer.append('\\');
                    outBuffer.append('\\');
                    continue;
                }
                outBuffer.append(aChar);
                continue;
            }
            switch (aChar) {
            case ' ':
                if (x == 0 || escapeSpace) {
                    outBuffer.append('\\');
                }
                outBuffer.append(' ');
                break;
            case '\t':
                outBuffer.append('\\');
                outBuffer.append('t');
                break;
            case '\n':
                outBuffer.append('\\');
                outBuffer.append('n');
                break;
            case '\r':
                outBuffer.append('\\');
                outBuffer.append('r');
                break;
            case '\f':
                outBuffer.append('\\');
                outBuffer.append('f');
                break;
            case '=': // Fall through
            case ':': // Fall through
            case '#': // Fall through
            case '!':
                outBuffer.append('\\');
                outBuffer.append(aChar);
                break;
            default:
                if (((aChar < 0x0020) || (aChar > 0x007e)) & escapeUnicode) {
                    outBuffer.append('\\');
                    outBuffer.append('u');
                    outBuffer.append(toHex((aChar >> 12) & 0xF));
                    outBuffer.append(toHex((aChar >> 8) & 0xF));
                    outBuffer.append(toHex((aChar >> 4) & 0xF));
                    outBuffer.append(toHex(aChar & 0xF));
                } else {
                    outBuffer.append(aChar);
                }
            }
        }
        return outBuffer.toString();
    }

    /**
     * Convert a nibble to a hex character
     * 
     * @param nibble
     *            the nibble to convert.
     */
    private static char toHex(int nibble) {
        return HEX_DIGIT[(nibble & 0xF)];
    }

    /** A table of hex digits */
    private static final char[] HEX_DIGIT = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
}
