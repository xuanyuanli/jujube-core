package com.yfs.constant;

import java.lang.reflect.Field;
import java.util.Collections;

/**
 * 系统属性
 * @author John Li
 */
public class SystemProperties {

    private SystemProperties() {
    }

    /** 项目目录 */
    public static final String PROJECT_DIR = System.getProperty("user.dir");
    /** 操作系统位数 */
    public static final String OS_ARCH = System.getProperty("os.arch");
    /** 系统临时目录 */
    public static final String TMPDIR = System.getProperty("java.io.tmpdir");
    /** 操作系统名称 */
    public static final String OS_NAME = System.getProperty("os.name");
    /** 系统编码 */
    public static final String OS_ENCODING = System.getProperty("sun.jnu.encoding");
    /** 用户家目录 */
    public static final String USER_HOME = System.getProperty("user.home");
    /** 用户名 */
    public static final String USER_NAME = System.getProperty("user.name");
    /** 项目用到的 class path 集合 */
    public static final String CLASS_PATH = System.getProperty("java.class.path");
    /** 用户所在地语言 */
    public static final String USER_LANGUAGE = System.getProperty("user.language");
    /** 文件分隔符 */
    public static final String FILE_SEPARATOR = System.getProperty("file.separator");
    /** 图形桌面名称 */
    public static final String OS_DESKTOP = System.getProperty("sun.desktop");

    /** JVM vendor info. */
    public static final String JVM_VENDOR = System.getProperty("java.vm.vendor");
    public static final String JVM_VERSION = System.getProperty("java.vm.version");
    public static final String JVM_NAME = System.getProperty("java.vm.name");

    /** The value of <tt>System.getProperty("java.version")</tt>. **/
    public static final String JAVA_VERSION = System.getProperty("java.version");

    /** True iff running on Linux. */
    public static final boolean LINUX = OS_NAME.startsWith("Linux");
    /** True iff running on Windows. */
    public static final boolean WINDOWS = OS_NAME.startsWith("Windows");
    /** True iff running on SunOS. */
    public static final boolean SUN_OS = OS_NAME.startsWith("SunOS");
    /** True iff running on Mac OS X */
    public static final boolean MAC_OS_X = OS_NAME.startsWith("Mac OS X");
    /** True iff running on FreeBSD */
    public static final boolean FREE_BSD = OS_NAME.startsWith("FreeBSD");

    public static final String OS_VERSION = System.getProperty("os.version");
    public static final String JAVA_VENDOR = System.getProperty("java.vendor");

    public static final boolean JRE_IS_MINIMUM_JAVA8;

    /** True iff running on a 64bit JVM */
    public static final boolean JRE_IS_64BIT;

    static {
        boolean is64Bit = false;
        try {
            final Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
            final Field unsafeField = unsafeClass.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            final Object unsafe = unsafeField.get(null);
            final int addressSize = ((Number) unsafeClass.getMethod("addressSize").invoke(unsafe)).intValue();
            is64Bit = addressSize >= 8;
        } catch (Exception e) {
            final String x = System.getProperty("sun.arch.data.model");
            String str64 = "64";
            if (x != null) {
                is64Bit = x.indexOf(str64) != -1;
            } else {
                if (OS_ARCH != null && OS_ARCH.indexOf(str64) != -1) {
                    is64Bit = true;
                } else {
                    is64Bit = false;
                }
            }
        }
        JRE_IS_64BIT = is64Bit;

        // this method only exists in Java 8:
        boolean v8 = true;
        try {
            Collections.class.getMethod("emptySortedSet");
        } catch (NoSuchMethodException nsme) {
            v8 = false;
        }
        JRE_IS_MINIMUM_JAVA8 = v8;
    }
}
