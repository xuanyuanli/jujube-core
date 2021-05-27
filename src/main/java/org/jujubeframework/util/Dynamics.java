package org.jujubeframework.util;

import com.beust.jcommander.internal.Sets;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * 模仿动态语言的一些方法
 *
 * @author John Li
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Dynamics {

    @SafeVarargs
    public static <T> List<T> listOf(T... t) {
        List<T> list = new ArrayList<>();
        if (t != null) {
            Collections.addAll(list, t);
        }
        return list;
    }

    @SafeVarargs
    public static <T> T[] arrayOf(T... t) {
        return t;
    }

    @SafeVarargs
    public static <T> Set<T> setOf(T... t) {
        Set<T> set = Sets.newLinkedHashSet();
        set.addAll(listOf(t));
        return set;
    }

    public static void println(String pattern, Object... params) {
        System.out.println(Texts.format(pattern, params));
    }

    public static void print(String pattern, Object... params) {
        System.out.print(Texts.format(pattern, params));
    }

    /**
     * @see Texts#format(String, Object...)
     */
    public static String format(String pattern, Object... params) {
        return Texts.format(pattern, params);
    }

    /**
     * 如果t为空，则返回other
     */
    public static <T> T orElse(T t, T other) {
        if (t == null) {
            return other;
        } else {
            return t;
        }
    }

    /**
     * 模仿js中对象转为bool的判断
     */
    public static boolean bool(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof String) {
            return ((String) obj).length() != 0;
        } else if (obj instanceof Number) {
            return ((Number) obj).intValue() != 0;
        } else if (obj instanceof Collection) {
            return !((Collection) obj).isEmpty();
        } else if (obj instanceof Map) {
            return !((Map) obj).isEmpty();
        } else if (obj.getClass().isArray()) {
            Object[] objs = (Object[]) obj;
            return objs.length > 0;
        } else if (obj instanceof Boolean) {
            return (Boolean) obj;
        }
        return true;
    }

    /**
     * 快捷的join方法
     */
    public static String join(Object[] array, String separator) {
        return StringUtils.join(array, separator);
    }

    /**
     * 快捷的join方法
     */
    public static String join(Iterable<?> collection, String separator) {
        return StringUtils.join(collection, separator);
    }

}
