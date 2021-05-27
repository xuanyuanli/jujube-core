package org.jujubeframework.util;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.math.NumberUtils;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

/**
 * 算数工具类(calculate)<br>
 * <p>
 * Math类常用函数
 * <ul>
 * <li>pow:幂运算</li>
 * <li>abs:绝对值</li>
 * <li>floor:地板，12.6 = 12.0</li>
 * <li>ceil:天花板，12.3 = 13.0</li>
 * </ul>
 *
 * @author John Li
 */
public class Calcs {

    private static final double DOUBLE_ZERO = 0.0d;
    private static final int TWO = 2;

    private Calcs() {
    }

    /**
     * 确认两个金额值是否相等（非常严谨的比较）
     */
    public static boolean equ(String str1, String str2) {
        Validate.notBlank(str1);
        Validate.notBlank(str2);

        str1 = str1.trim();
        str2 = str2.trim();
        // stripTrailingZeros()能去掉后面的0，进行比较
        BigDecimal b1 = new BigDecimal(str1).stripTrailingZeros();
        BigDecimal b2 = new BigDecimal(str2).stripTrailingZeros();
        return b1.equals(b2);
    }

    /**
     * 确认两个金额值是否相等（非常严谨的比较）
     */
    public static boolean equ(Number str1, Number str2) {
        return equ(String.valueOf(str1 == null ? "0" : str1), str2 != null ? str2.toString() : "0");
    }

    /**
     * 第一个数是否比第二个数小（非常严谨的比较）
     */
    public static boolean isLow(String str1, String str2) {
        Validate.notBlank(str1);
        Validate.notBlank(str2);

        str1 = str1.trim();
        str2 = str2.trim();
        BigDecimal b1 = new BigDecimal(str1).stripTrailingZeros();
        BigDecimal b2 = new BigDecimal(str2).stripTrailingZeros();
        BigDecimal b3 = b1.min(b2).stripTrailingZeros();
        boolean bl1 = b3.equals(b1);
        boolean bl2 = b3.equals(b2);
        return (bl1 && !bl2);
    }

    /**
     * 第一个数是否小于等于第二个数
     */
    public static boolean isLte(Number str1, Number str2) {
        return isLow(str1, str2) || equ(str1, str2);
    }

    /**
     * 第一个数是否比第二个数小（非常严谨的比较）
     */
    public static boolean isLow(Number str1, Number str2) {
        return isLow(String.valueOf(str1 == null ? "0" : str1), String.valueOf(str2 == null ? "0" : str2));
    }

    /**
     * 加法运算
     *
     * @param str1
     *            被加数
     * @param str2
     *            加数
     * @param iScale
     *            精确度（小数点后保留位数）
     */
    public static String add(String str1, String str2, int iScale) {
        Validate.notBlank(str1);
        Validate.notBlank(str2);
        Validate.isTrue(iScale > -1);

        str1 = str1.trim();
        str2 = str2.trim();
        BigDecimal b1 = new BigDecimal(str1);
        BigDecimal b2 = new BigDecimal(str2);
        BigDecimal b3 = b1.add(b2);
        b3 = b3.divide(new BigDecimal("1"), iScale, BigDecimal.ROUND_HALF_UP);
        return b3.toPlainString();
    }

    /**
     * 加法
     */
    public static Number add(Number str1, Number str2, int iScale) {
        return NumberUtils.toDouble(add(String.valueOf(str1 == null ? "0" : str1), String.valueOf(str2 == null ? "0" : str2), iScale));
    }

    /**
     * 加法,保留2位
     */
    public static double add(Double d1, Double d2) {
        return add(d1, d2, 2).doubleValue();
    }

    /**
     * 加法运算,保留2位
     *
     * @param str1
     *            被加数
     * @param str2
     *            加数
     */
    public static String add(String str1, String str2) {
        return add(str1, str2, 2);
    }

    /**
     * 减法
     *
     * @param str1
     *            被减数
     * @param str2
     *            减数
     * @param iScale
     *            精确度（小数点后保留位数）
     */
    public static String sub(String str1, String str2, int iScale) {
        Validate.notBlank(str1);
        Validate.notBlank(str2);
        Validate.isTrue(iScale > -1);

        str1 = str1.trim();
        str2 = str2.trim();
        BigDecimal b1 = new BigDecimal(str1);
        BigDecimal b2 = new BigDecimal(str2);
        BigDecimal b3 = b1.subtract(b2);
        b3 = b3.divide(new BigDecimal("1"), iScale, BigDecimal.ROUND_HALF_UP);
        return b3.toPlainString();
    }

    /**
     * 减法运算,保留2位
     *
     * @param str1
     *            被减数
     * @param str2
     *            减数
     */
    public static String sub(String str1, String str2) {
        return sub(str1, str2, 2);
    }

    /**
     * 减法
     */
    public static Number sub(Number str1, Number str2, int iScale) {
        return NumberUtils.toDouble(sub(String.valueOf(str1 == null ? "0" : str1), String.valueOf(str2 == null ? "0" : str2), iScale));
    }

    /**
     * 减法,保留2位
     */
    public static double sub(Double d1, Double d2) {
        return sub(d1, d2, 2).doubleValue();
    }

    /**
     * 乘法运算 指定保留到小数点后位数
     *
     * @param str1
     *            被乘数
     * @param str2
     *            乘数
     * @param iScale
     *            精确度（小数点后保留位数）
     */
    public static String mul(String str1, String str2, int iScale) {
        Validate.notBlank(str1);
        Validate.notBlank(str2);
        Validate.isTrue(iScale > -1);

        str1 = str1.trim();
        str2 = str2.trim();
        BigDecimal b1 = new BigDecimal(str1);
        BigDecimal b2 = new BigDecimal(str2);
        BigDecimal b3 = b1.multiply(b2);
        // BigDecimal.ROUND_HALF_UP 遇到5的时候向上取值
        b3 = b3.divide(new BigDecimal("1"), iScale, BigDecimal.ROUND_HALF_UP);
        return b3.toPlainString();
    }

    /**
     * 乘法运算,保留2位
     */
    public static String mul(String str1, String str2) {
        return mul(str1, str2, 2);
    }

    /**
     * 乘法运算,保留2位
     */
    public static double mul(Double str1, Double str2) {
        return mul(str1, str2, 2).doubleValue();
    }

    /**
     * 乘法
     */
    public static Number mul(Number str1, Number str2, int iScale) {
        return NumberUtils.toDouble(mul(String.valueOf(str1 == null ? "0" : str1), String.valueOf(str2 == null ? "0" : str2), iScale));
    }

    /**
     * 除法运算 指定保留到小数点后位数
     *
     * @param str1
     *            被除数
     * @param str2
     *            除数
     * @param iScale
     *            精确度（小数点后保留位数）
     */
    public static String div(String str1, String str2, int iScale) {
        Validate.notBlank(str1);
        Validate.notBlank(str2);
        Validate.isTrue(iScale > -1);
        if (NumberUtils.toDouble(str2) == DOUBLE_ZERO) {
            str2 = "1";
        }

        str1 = str1.trim();
        str2 = str2.trim();
        BigDecimal b1 = new BigDecimal(str1);
        BigDecimal b2 = new BigDecimal(str2);
        BigDecimal b3 = b1.divide(b2, iScale, BigDecimal.ROUND_HALF_UP);
        return b3.toPlainString();
    }

    /**
     * 除法运算 保留到小数点后2位
     */
    public static String div(String str1, String str2) {
        return div(str1, str2, 2);
    }

    /**
     * 除法
     */
    public static Number div(Number str1, Number str2, int iScale) {
        return NumberUtils.toDouble(div(String.valueOf(str1 == null ? "0" : str1), String.valueOf(str2 == null ? "0" : str2), iScale));
    }

    public static Double div(Double str1, Double str2) {
        return div(str1, str2, 2).doubleValue();
    }

    /**
     * 计算平均数
     */
    public static double getAverage(List<Double> list) {
        double sum = 0;
        for (Double aDouble : list) {
            sum += aDouble;
        }
        return sum / list.size();
    }

    /**
     * 计算中位数
     */
    public static double getMedian(List<Double> list) {
        Collections.sort(list);
        if (list.size() % TWO == 0) {
            int index = (list.size() / 2) - 1;
            return (list.get(index) + list.get(index + 1)) / 2;
        } else {
            int index = (list.size() / 2) + 1;
            return list.get(index);
        }
    }

}
