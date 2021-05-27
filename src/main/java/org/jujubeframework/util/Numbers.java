package org.jujubeframework.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.math.NumberUtils;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Locale;

/**
 * 数字工具
 * 
 * @author John Li
 */
public class Numbers {

    /**
     * 金钱格式化
     *
     * @param isAddSuffix
     *            是否添加"万"后缀。如果不添加，则返回"##,###.##"格式的数字
     * @return 返回格式如：700,000.0或70万
     */
    public static String moneyFormatOfZhPrefix(Number number, boolean isAddSuffix) {
        String suffix = "万";
        if (isAddSuffix) {
            Validate.notNull(suffix);
        }
        String result;
        DecimalFormat myformat = new DecimalFormat();
        Locale locale = Locale.CHINA;

        double num = number.doubleValue();

        if (isAddSuffix && num >= 10000) {
            num = num / 10000;

            myformat.applyPattern("##,###.####");
            result = myformat.format(num);

            result += suffix;
        } else {
            myformat.applyPattern("##,###.##");
            result = myformat.format(num);
        }
        return result;
    }

    /**
     * 数字格式化
     *
     * @param pattern
     *            如“##.##”的字符串
     */
    public static String numberFormat(Number number, String pattern) {
        DecimalFormat myformat = new DecimalFormat(pattern);
        return myformat.format(number);
    }

    /**
     * 只处理source中的数字部分
     */
    public static Integer parseInt(String source) {
        StringBuilder builder = new StringBuilder();
        if (StringUtils.isNotBlank(source)) {
            for (int i = 0; i < source.length(); i++) {
                char ch = source.charAt(i);
                if (Character.isDigit(ch)) {
                    builder.append(ch);
                }
            }
        }
        return NumberUtils.toInt(builder.toString());
    }

    /**
     * 把number转换为string，非科学计数法
     */
    public static String numberToString(Number value) {
        return new BigDecimal(value + "").toPlainString();
    }
}
