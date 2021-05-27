package org.jujubeframework.util.support;

import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;

/**
 * 因为Pattern会在初始化的时候进行编译，所以最好缓存一下
 *
 * @author John Li
 */
public class PatternHolder {
    private static final ConcurrentMap<String, Pattern> PATTERNS = new ConcurrentHashMap<>();

    public static Pattern getPattern(String regex) {
        return getPattern(regex, false);
    }

    public static Pattern getPattern(String regex, boolean ignoreCase) {
        String key = regex + ignoreCase;
        Pattern pattern = PATTERNS.get(key);
        if (pattern == null) {
            if (ignoreCase) {
                pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
            } else {
                pattern = Pattern.compile(regex);
            }
            PATTERNS.putIfAbsent(key, pattern);
        }
        return pattern;
    }

    /**
     * 转义正则特殊字符 （$()*+.[]?\^{},|）
     */
    public static String escapeExprSpecialWord(String keyword) {
        if (StringUtils.isNotBlank(keyword)) {
            String[] fbsArr = { "\\", "$", "(", ")", "*", "+", ".", "[", "]", "?", "^", "{", "}", "|" };
            for (String key : fbsArr) {
                if (keyword.contains(key)) {
                    keyword = keyword.replace(key, "\\" + key);
                }
            }
        }
        return keyword;
    }

    /***
     * @see Pattern#compile(String, int)
     */
    public static Pattern compile(String regex, int flags) {
        String key = regex + flags;
        Pattern pattern = PATTERNS.get(key);
        if (pattern == null) {
            pattern = Pattern.compile(regex, flags);
            PATTERNS.putIfAbsent(key, pattern);
        }
        return pattern;
    }
}
