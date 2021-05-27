package org.jujubeframework;

import org.jujubeframework.util.Texts;

import java.io.IOException;
import java.text.DecimalFormat;
import java.time.LocalDateTime;

public class Test {

    public static void main(String[] args) throws IOException {
        LocalDateTime localDateTime1 = LocalDateTime.of(2021, 1, 1, 10, 45, 8);
        LocalDateTime localDateTime2 = LocalDateTime.of(2021, 1, 1, 1, 45, 8);
        System.out.println(localDateTime1.isAfter(localDateTime2));
    }

    private static void test2() throws IOException {
        System.out.println(Texts.class.getName());
    }

    public static String formatDoubleNum(double num, int length) {
        String numStr = Double.toString(num);
        if ((numStr.length() - numStr.indexOf(".")) < length) {
            return numStr;
        }
        StringBuilder strAfterDot = new StringBuilder();
        int i = 0;
        while (i < length) {
            strAfterDot.append("0");
            i++;
        }
        String formatStr = "0." + strAfterDot.toString();
        DecimalFormat df = new DecimalFormat(formatStr);
        return df.format(num);
    }
}
