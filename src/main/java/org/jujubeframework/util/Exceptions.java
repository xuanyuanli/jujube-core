package org.jujubeframework.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DecimalFormat;

/**
 * @author John Li
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Exceptions {
    /**
     * 抛出运行时异常
     */
    public static void throwException(Throwable e) {
        throw new RuntimeException(e);
    }

    /**
     * 获得异常堆栈信息
     */
    public static String exceptionToString(Exception exception) {
        StringWriter writer = new StringWriter();
        exception.printStackTrace(new PrintWriter(writer));
        return writer.toString();
    }

    /**
     * 获得异常堆栈信息
     */
    public static String exceptionToString(Exception exception, int len) {
        String data = exceptionToString(exception);
        if (data != null && data.length() > len) {
            data = data.substring(0, len);
        }
        return data;
    }


}
