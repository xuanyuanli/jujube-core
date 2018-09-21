package org.jujubeframework;

import java.text.DecimalFormat;

public class Test {

    public static void main(String[] args) {
        System.out.println(formatDoubleNum(56.909089,2));
    }

    public static String formatDoubleNum(double num, int length) {
        String numStr = Double.toString(num);
        if ((numStr.length() - numStr.indexOf(".")) < length) {
            return numStr;
        }
        StringBuffer strAfterDot = new StringBuffer();
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
