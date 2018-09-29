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
            for (T ele : t) {
                list.add(ele);
            }
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

    public static void println(String pattern, String... params) {
        System.out.println(Texts.format(pattern, params));
    }

    public static void print(String pattern, String... params) {
        System.out.print(Texts.format(pattern, params));
    }

    /**
     * @see Texts#format(String, String...)
     */
    public static String format(String pattern, String... params) {
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
    @SuppressWarnings("rawtypes")
    public static boolean bool(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof String) {
            if (((String) obj).length() == 0) {
                return false;
            }
        } else if (obj instanceof Number) {
            if (((Number) obj).intValue() == 0) {
                return false;
            }
        } else if (obj instanceof Collection) {
            if (((Collection) obj).isEmpty()) {
                return false;
            }
        } else if (obj instanceof Map) {
            if (((Map) obj).isEmpty()) {
                return false;
            }
        } else if (obj.getClass().isArray()) {
            Object[] objs = (Object[]) obj;
            if (objs.length <= 0) {
                return false;
            }
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
