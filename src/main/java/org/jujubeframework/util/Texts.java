package org.jujubeframework.util;

import com.google.common.collect.Maps;
import net.sourceforge.pinyin4j.PinyinHelper;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.jujubeframework.util.support.PatternHolder;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 文本字符相关工具类
 *
 * @author John Li Email：jujubeframework@163.com
 */
public class Texts {

    private Texts() {
    }

    /**
     * 转义正则特殊字符 （$()*+.[]?\^{},|）
     */
    public static String escapeExprSpecialWord(String keyword) {
        if (StringUtils.isNotBlank(keyword)) {
            String[] fbsArr = {"\\", "$", "(", ")", "*", "+", ".", "[", "]", "?", "^", "{", "}", "|"};
            for (String key : fbsArr) {
                if (keyword.contains(key)) {
                    keyword = keyword.replace(key, "\\" + key);
                }
            }
        }
        return keyword;
    }

    /**
     * 将一个转义后的符号，反转义回来
     */
    public static String[] unescape(String[] params, String escape) {
        String str = StringEscapeUtils.unescapeHtml4(escape);
        String[] result = new String[params.length];
        int i = 0;
        for (String string : params) {
            if (string.contains(escape)) {
                string = string.replace(escape, str);
            }
            result[i] = string;
            i++;
        }
        return result;
    }

    /**
     * 判断是否为合法IP
     */
    public static boolean isTrueIp(String ipAddress) {
        return find(ipAddress, "((25[0-5]|2[0-4]\\d|[01]?\\d\\d?)($|(?!\\.$)\\.)){4}");
    }

    /**
     * 是否是合法的用户名。只能包含中文、英文和数字
     */
    public static boolean isLegalForUsername(String name) {
        for (int i = 0; i < name.length(); i++) {
            char ch = name.charAt(i);
            if (!Texts.isChinese(ch) && !Texts.isEn(ch) && !Texts.isNumeric(ch)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 获得合理用户名
     */
    public static String getLegalUsername(String name) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < name.length(); i++) {
            char ch = name.charAt(i);
            if (Texts.isChinese(ch) || Texts.isEn(ch) || Texts.isNumeric(ch)) {
                result.append(ch);
            }
        }
        return result.toString();
    }

    /**
     * 正则验证密码为数字和字母的组合，且为6位以上
     */
    public static boolean checkPassWord(String passWord) {
        String regex = "^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{6,}$";
        return passWord.matches(regex);
    }

    /**
     * 清楚特殊字符
     */
    public static String cleanSpecialChar(String str) {
        String regEx = "[`~!@#$%^&*()+=|{}':;',//[//].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Pattern p = PatternHolder.getPattern(regEx);
        Matcher m = p.matcher(str);
        return m.replaceAll("").trim();
    }

    /**
     * 过滤掉超过3个字节的UTF8字符
     */
    public static String filterOffUtf8Mb4(String text) {
        byte[] bytes = null;
        try {
            bytes = text.getBytes("utf-8");
        } catch (UnsupportedEncodingException e) {
            return text;
        }
        ByteBuffer buffer = ByteBuffer.allocate(bytes.length);
        int i = 0;
        while (i < bytes.length) {
            short b = bytes[i];
            if (b > 0) {
                buffer.put(bytes[i++]);
                continue;
            }

            // 去掉符号位
            b += 256;

            if (((b >> 5) ^ 0x6) == 0) {
                buffer.put(bytes, i, 2);
                i += 2;
            } else if (((b >> 4) ^ 0xE) == 0) {
                buffer.put(bytes, i, 3);
                i += 3;
            } else if (((b >> 3) ^ 0x1E) == 0) {
                i += 4;
            } else if (((b >> 2) ^ 0x3E) == 0) {
                i += 5;
            } else if (((b >> 1) ^ 0x7E) == 0) {
                i += 6;
            } else {
                buffer.put(bytes[i++]);
            }
        }
        buffer.flip();
        try {
            return new String(buffer.array(), "utf-8");
        } catch (UnsupportedEncodingException e) {
            return text;
        }
    }

    /**
     * 邮箱验证
     */
    public static boolean emailValidate(String email) {
        String mailRegex = "^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$";
        return find(email, mailRegex);
    }

    /**
     * 手机验证
     */
    public static boolean mobileValidate(String mobile) {
        String mobileRegex = "^[1][357896]\\d{9}$";
        return find(mobile, mobileRegex);
    }

    /**
     * 替换所有空白字符
     */
    public static String replaceBlank(String str) {
        String regex = "\\s*|\t|\r|\n";
        Pattern compile = PatternHolder.getPattern(regex);
        return compile.matcher(str).replaceAll("");
    }

    /**
     * 替换utf-8中的空格，以免造成编码转换出现？的情况
     */
    public static String replaceUtf8Blank(String text) {
        if (StringUtils.isBlank(text)) {
            return "";
        }
        // utf-8空格替换;
        return text.replace(" ", " ");
    }

    /**
     * 去CSS
     */
    public static String clearCSS(String content) {
        content = content.replaceAll("<[\\s]*?style[^>]*?>[\\s\\S]*?<[\\s]*?\\/[\\s]*?style[\\s]*?>", "");
        content = content.replaceAll("[style|STYLE]\\s*?=\\s*?\".*?\"", "");
        return content;
    }

    /**
     * 获取一段html的纯文本
     */
    public static String getHtmlText(String html) {
        // 剔出<html>的标签
        String txtcontent = html.replaceAll("</?[^>]+>", "");
        // 去除字符串中的空格,回车,换行符,制表符
        txtcontent = txtcontent.replaceAll("<a>\\s*|\t|\r|\n</a>", "");
        return txtcontent;
    }

    /**
     * 正则替换封装（忽略大小写）
     *
     * <pre>
     *     举例：regReplace("@+","","@@@123@") = 123
     *     需要注意的是，正则表达式中如果出现特殊字符，需要进行转义。比如：*.$等。"\\$"进行转义
     * </pre>
     *
     * @param reg    正则表达式
     * @param repstr 要替换为的字符
     * @param instr  原始字符串
     * @return 完成替换的字符串
     */
    public static String regReplace(String reg, String repstr, String instr) {
        return regReplace(reg, repstr, instr, true);
    }

    /**
     * @see #regQuery(String, String, boolean)
     */
    public static List<RegexQueryInfo> regQuery(String reg, String instr) {
        return regQuery(reg, instr, true);
    }

    /**
     * 正则查询
     *
     * @param reg        正则表达式
     * @param instr      原始字符串
     * @param ignoreCase 是否忽略大小写
     * @return 返回多个匹配的信息
     */
    public static List<RegexQueryInfo> regQuery(String reg, String instr, boolean ignoreCase) {
        List<RegexQueryInfo> list = new ArrayList<Texts.RegexQueryInfo>();
        Pattern pattern = PatternHolder.getPattern(reg, ignoreCase);
        Matcher matcher = pattern.matcher(instr);
        while (matcher.find()) {
            RegexQueryInfo info = new RegexQueryInfo();
            info.setEnd(matcher.end());
            info.setStart(matcher.start());
            info.setGroup(matcher.group());
            List<String> groups = new ArrayList<>(matcher.groupCount());
            for (int i = 1; i <= matcher.groupCount(); i++) {
                groups.add(matcher.group(i));
            }
            info.setGroups(groups);
            list.add(info);
        }
        return list;
    }

    /**
     * 正则替换封装
     *
     * @param reg        正则表达式
     * @param repstr     要替换为的字符
     * @param instr      原始字符串
     * @param ignoreCase 是否忽略大小写
     * @return
     */
    public static String regReplace(String reg, String repstr, String instr, boolean ignoreCase) {
        Pattern pattern = PatternHolder.getPattern(reg, ignoreCase);
        Matcher matcher = pattern.matcher(instr);
        return matcher.replaceAll(repstr);
    }

    /**
     * 替换字符串str中的中文为str2
     */
    public static String replaceChinese(String str, String str2) {
        StringBuffer bf = new StringBuffer();
        for (int i = 0; i < str.length(); i++) {
            if (String.valueOf(str.charAt(i)).matches("[^x00-xff]*")) {
                bf.append(str2);
            } else {
                bf.append(str.charAt(i));
            }
        }
        return bf.toString();
    }

    /**
     * 全角转半角
     */
    public static String toDBC(String input) {
        char[] c = input.toCharArray();
        for (int i = 0; i < c.length; i++) {
            if (c[i] == '\u3000') {
                c[i] = ' ';
            } else if (c[i] > '\uFF00' && c[i] < '\uFF5F') {
                c[i] = (char) (c[i] - 65248);
            }
        }
        String returnString = new String(c);
        return returnString;
    }

    /**
     * 适用于大型字符串分割，可以设置多个分隔符
     * <pre>
     *     举例：
     *     String[] s = stringTokenizer("wo; are, student", " ,;");
     *     //s={wo,are,student}
     * </pre>
     *
     * @param srcString       要分割的字符串
     * @param tokenizerString 分隔符
     * @return 分割后的数组
     */
    public static String[] stringTokenizer(String srcString, String tokenizerString) {
        return org.springframework.util.StringUtils.tokenizeToStringArray(srcString,tokenizerString);
    }

    /**
     * 用正则匹配，查找字符串中有没有相应字符
     * <pre>举例：find("zfa_999_ic", "zfa_\\d+_ic") = true</pre>
     *
     * @param source 原字符串
     * @param regEx  正则表达式
     * @return 是否找到
     */
    public static boolean find(String source, String regEx) {
        if (StringUtils.isBlank(source)) {
            return false;
        }
        Pattern pat = PatternHolder.getPattern(regEx);
        Matcher mat = pat.matcher(source);
        boolean rs = mat.find();
        return rs;
    }

    /**
     * 高亮显示关键字(所有匹配的字符都替换)
     *
     * @param source      原文本
     * @param keyWord     关键字
     * @param styleBefore 样式前，例如<font class='red'>
     * @param styleAfter  样式后,例如</font>
     * @return
     */
    public static String highlight(String source, String keyWord, String styleBefore, String styleAfter) {
        int begin = 0;
        // 加上样式之后的关键字长度
        int len = styleAfter.length() + styleBefore.length() + keyWord.length();
        StringBuilder sb = new StringBuilder(source.length() + len * 5);
        String tag = source;
        while (true) {
            // 不区分大小写，找到关键字
            begin = tag.toUpperCase().indexOf(keyWord.toUpperCase());
            // 如果找到关键字，则关键字替换为高亮样式
            if (begin != -1) {
                int end = begin + keyWord.length();
                // 原文本中的关键字（保持其大小写状态）
                String red = tag.substring(begin, end);
                // 此次查找的字符串
                String result = tag.substring(0, end);
                // 对文本中关键字进行高亮替换
                result = result.replace(red, (styleBefore + red + styleAfter));
                // 保存已经替换完成的那一段
                sb.append(result);
                // 截取字符串，在后面继续寻找关键字，进行高亮替换
                tag = tag.substring(end);
            } else {
                // 如果没有找到关键字，把文本遗落的一段放入结果中
                sb.append(tag);
                break;
            }
        }
        return sb.toString();
    }

    /**
     * 替换图片路径前缀<br>
     * 有时候图片需要压缩，这时候替换文章内容中出现的图片前缀，通过Apache等服务器的处理，实现压缩的目的
     *
     * @param content     html格式的文章内容
     * @param subFragment 前缀截取片段。通过它来确定前缀位置
     * @param destPrefix  要替换为的前缀
     * @return
     */
    public static String replaceImageUrlPrefix(String content, String subFragment, String destPrefix) {
        String result = content;
        if (StringUtils.isNotBlank(content)) {
            String regex = "src\\s?=\\s?(['\"])(.*?)\\1";
            Pattern pattern = PatternHolder.getPattern(regex, true);
            Matcher matcher = pattern.matcher(content);
            while (matcher.find()) {
                String path = matcher.group(2);
                // 三种情况：1、path包含前缀截取片段。则确定前缀位置，进行前缀替换
                if (path.contains(subFragment)) {
                    String subPath = "";
                    int pathPrefixIndex = path.indexOf(subFragment) + subFragment.length();
                    subPath = path.substring(pathPrefixIndex);
                    result = result.replace(path, destPrefix + subPath);
                } else {
                    // 2、path不包含前缀截取片段，同时也不包含其他未知前缀，则证明图片url没有前缀，进行添加
                    if (!path.contains(".com")) {
                        result = result.replace(path, destPrefix + path);
                    }
                    // 3、path不包含前缀截取片段，同时包含其他未知前缀，则什么都不做
                }
            }
        }
        return result;
    }

    /**
     * 获得字符串第一个字母
     * <p>
     * 如果为第一个字符为英文，则取出第一个字母<br>
     * 如果为第一个字符为汉字，则汉字转化为拼音，取第一个拼音字母<br>
     * 如果第一个字符（或之后）是符号，则从第二个字符开始，按照上面规则处理。后面字符还是符号，则递归处理<br>
     * 如果全是符号，则返回null
     */
    public static String getFirstLetter(String str) {
        Validate.notBlank(str);

        String result = null;
        char firstLetter = str.charAt(0);
        if (isEn(firstLetter)) {
            result = String.valueOf(firstLetter);
        } else if (isChinese(firstLetter)) {
            result = PinyinHelper.toHanyuPinyinStringArray(firstLetter)[0].substring(0, 1);
        } else if (str.length() > 1) {
            result = getFirstLetter(str.substring(1));
        }
        return result;
    }

    /**
     * 判断字符串中是否包含汉字
     */
    public static boolean containsChinese(String s) {
        boolean result = false;
        if (StringUtils.isNotBlank(s)) {
            for (int i = 0; i < s.length(); i++) {
                String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(s.charAt(i));
                if (pinyinArray != null && pinyinArray.length > 0) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    /**
     * 获得文本中的所有中文
     */
    public static String getChinese(String content) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < content.length(); i++) {
            char ch = content.charAt(i);
            if (isChinese(ch)) {
                result.append(ch);
            }
        }
        return result.toString();
    }

    /**
     * 获取汉字的首字母集合(如果字符串中有非中文，将被舍弃)
     */
    public static String getFirstLetterArr(String str) {
        Validate.notBlank(str);
        StringBuffer sBuffer = new StringBuffer();
        for (int i = 0; i < str.length(); i++) {
            Character c = str.charAt(i);
            if (isChinese(c)) {
                sBuffer.append(getFirstLetter(String.valueOf(c)));
            }
        }
        return sBuffer.toString();
    }

    /**
     * 得到汉字的拼音
     *
     * @param content 汉字
     * @param type    1、拼音都为小写且带声调；2、拼音都为小写不带声调；3、拼音首字母大写不带声调；
     */
    public static String getLetter(String content, int type) {
        Validate.notBlank(content);
        int three = 3;
        if (type < 1 || type > three) {
            type = 1;
        }

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < content.length(); i++) {
            Character c = content.charAt(i);
            if (isChinese(c)) {
                String pinyin = PinyinHelper.toHanyuPinyinStringArray(c)[0];
                if (type == 2) {
                    pinyin = pinyin.substring(0, pinyin.length() - 1);
                } else if (type == three) {
                    pinyin = pinyin.substring(0, pinyin.length() - 1);
                    pinyin = StringUtils.capitalize(pinyin);
                }
                result.append(pinyin);
            }
        }
        return result.toString();
    }

    public static boolean isEn(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }

    public static boolean isNumeric(char c) {
        return Character.isDigit(c);
    }

    /**
     * 字符是否是中文
     */
    public static boolean isChinese(char c) {
        return containsChinese(String.valueOf(c));
    }

    /**
     * 是否是科学计数法
     */
    public static boolean isScientificNotation(String str) {
        try {
            BigDecimal bd = new BigDecimal(str);
            bd.toPlainString();
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * 和JavaScript中RegExp对象的exec()方法一样<br>
     * 只返回第一个匹配的结果，数组中第一个元素包含正则表达式匹配的字符串，余下的元素是与圆括号内的子表达式相匹配的子串
     */
    public static String[] getGroups(String regex, String source) {
        Pattern pattern = PatternHolder.getPattern(regex);
        Matcher matcher = pattern.matcher(source);
        String[] groups = new String[0];
        if (matcher.find()) {
            int count = matcher.groupCount();
            groups = new String[count + 1];
            for (int i = 0; i <= count; i++) {
                groups[i] = matcher.group(i);
            }
        }
        return groups;
    }

    /**
     * 获取匹配到的文本
     */
    public static String getGroup(String regex, String source) {
        Pattern pattern = PatternHolder.getPattern(regex);
        Matcher matcher = pattern.matcher(source);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }

    /**
     * 对lines进行分组处理
     *
     * @param lines         数据
     * @param groupFunction 获得group name的function：入参为line，返回groupName
     * @return 分组后的lines
     */
    public static Map<String, List<String>> group(List<String> lines, Function<String, String> groupFunction) {
        Map<String, List<String>> group = new LinkedHashMap<>();
        List<String> part = new ArrayList<>();
        // 先找出第一个groupName
        String groupName = "";
        int i = 0;
        for (; i < lines.size(); i++) {
            String line = lines.get(i);
            String curGroupName = groupFunction.apply(line);
            if (StringUtils.isNotBlank(curGroupName)) {
                groupName = curGroupName;
                break;
            }
        }
        for (int j = i + 1; j < lines.size(); j++) {
            String line = lines.get(j);
            String curGroupName = groupFunction.apply(line);
            if (StringUtils.isNotBlank(curGroupName)) {
                group.put(groupName, part);
                // 因为group中元素都在group下面，so...
                groupName = curGroupName;
                part = new ArrayList<>();
            } else if (StringUtils.isNotBlank(line)) {
                part.add(line);
            }
        }
        group.put(groupName, part);
        return group;
    }

    /**
     * 对分组后的数据进行再分组
     *
     * @param group         分组后的数据
     * @param groupFunction 获得sub group name的function：入参为组下元素，返回sub group name
     * @return 再分组后的数据
     */
    public static Map<String, Map<String, List<String>>> groupAgain(Map<String, List<String>> group, Function<String, String> groupFunction) {
        Map<String, Map<String, List<String>>> result = new LinkedHashMap<>();
        for (String groupName : group.keySet()) {
            List<String> groupEles = group.get(groupName);
            Map<String, List<String>> subGroup = new LinkedHashMap<>();
            result.put(groupName, subGroup);
            // 对group进行再分组
            for (String groupEle : groupEles) {
                String subGoupName = groupFunction.apply(groupEle);
                if (!subGroup.containsKey(subGoupName)) {
                    List<String> part = new ArrayList<>();
                    part.add(groupEle);
                    subGroup.put(subGoupName, part);
                } else {
                    subGroup.get(subGoupName).add(groupEle);
                }
            }
        }
        return result;
    }

    /**
     * 合并分组
     */
    public static List<String> mergeGroup(Map<String, Map<String, List<String>>> group, Function<String, String> moduleCommentFunction) {
        List<String> lines = new ArrayList<>();
        for (String groupName : group.keySet()) {
            Map<String, List<String>> subGroup = group.get(groupName);
            String moduleComment = moduleCommentFunction.apply(groupName);
            lines.add(moduleComment);
            for (Entry<String, List<String>> ele : subGroup.entrySet()) {
                lines.addAll(ele.getValue());
                lines.add("");
            }
            lines.add("");
        }
        return lines;
    }

    /**
     * 判断是否包含特殊符号,注此特殊字符不能包含&amp;,因为搜索的内容可能有这个符号
     */
    public static boolean isFormal(String content) {
        String regEx = "[`~!@#$%^*()+=|{}':;',//[//].<>/?~！@#￥%……*（）——+|{}【】‘；：”“’。，、？]";
        return find(content, regEx);
    }

    /**
     * 格式化字符串，仿C#
     *
     * <pre>
     * str = Hello {0}
     * result = format(str,"World!")
     * #result = Hello World!
     * </pre>
     *
     * @param pattern 待匹配字符串
     * @param params  参数数组
     */
    public static String format(String pattern, String... params) {
        String regex = "\\{\\d+\\}";
        int count = regQuery(regex, pattern).size();
        String symbol = "{}";
        // 类似{0}这种形式
        if (count > 0) {
            if (params.length != count) {
                throw new IllegalArgumentException("模式匹配跟参数个数不对应");
            }
            String result = pattern;
            for (int i = 0; i < count; i++) {
                String replacement = params[i];
                if (replacement == null) {
                    replacement = "";
                }
                result = result.replace("{" + i + "}", replacement);
            }
            return result;
        }
        // 或者直接{}这种形式
        else if (pattern.contains(symbol)) {
            String result = "";
            // 防止{}出现在最后一行
            pattern += " ";
            String[] arr = pattern.split("\\{\\}");
            for (int i = 0; i < arr.length - 1; i++) {
                result += arr[i] + (params.length <= i ? "" : params[i]);
                if (i == arr.length - 2) {
                    result += arr[i + 1];
                }
            }
            return result.substring(0, result.length() - 1);
        } else {
            return pattern;
        }
    }

    public static String toA(String href, String title, boolean blank) {
        return format("<a href=\"{0}\" target=\"{1}\">{2}</a>", href, blank ? "_blank" : "", title);
    }

    /**
     * 字符截断。如果超出trancationNum，则后跟‘...’
     *
     * @param source        原始字符
     * @param trancationNum 截断的字符数
     */
    public static String truncate(String source, int trancationNum) {
        if (StringUtils.isBlank(source)) {
            return "";
        }
        if (source.length() > trancationNum) {
            return source.substring(0, trancationNum) + "...";
        } else {
            return source;
        }
    }

    public static long toLong(String source) {
        return NumberUtils.toLong(source);
    }

    public static double toDouble(String source) {
        return NumberUtils.toDouble(source);
    }

    public static int toInt(String source) {
        return NumberUtils.toInt(source);
    }

    /**
     * 只取字符串中的数字
     */
    public static int parseInt(String source) {
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
     * Map的toString方法获得的字符串，现在转换为Map的toJson格式
     */
    public static Map<String, String> mapStringToJson(String source) {
        String symbol = "=";
        if (Dynamics.bool(source) && source.contains(symbol)) {
            Map<String, String> map = Maps.newHashMap();
            source = source.replace("{", "").replace("}", "");
            String[] arr = source.split(", ");
            for (String ele : arr) {
                String[] arr2 = ele.split(symbol);
                map.put(arr2[0], arr2[1]);
            }
            return map;
        }
        throw new IllegalArgumentException();
    }

    public static String capitalize(String str) {
        return StringUtils.capitalize(str);
    }

    /**
     * 正则查询的信息
     * <p>
     * 类中三个字段参考Matcher类
     */
    public static class RegexQueryInfo {
        /**整个正则匹配到的字符*/
        private String group;
        private int start;
        private int end;
        /**正则中每个括号匹配到的字符集合，从0开始。例如用(a)(b)去匹配'ab'，groups的size=2*/
        private List<String> groups;

        public String getGroup() {

            return group;
        }

        public void setGroup(String group) {
            this.group = group;
        }

        public int getStart() {
            return start;
        }

        public void setStart(int start) {
            this.start = start;
        }

        public int getEnd() {
            return end;
        }

        public void setEnd(int end) {
            this.end = end;
        }

        public List<String> getGroups() {
            return groups;
        }

        public void setGroups(List<String> groups) {
            this.groups = groups;
        }

    }

    /**
     * 手机电话号码中间用星号代替
     */
    public static String hidePhone(String source) {
        String result = "";
        if (!Dynamics.bool(source)) {
            return "";
        }
        String asterisk = "****";
        int length = source.length();
        int hindex = (int) Math.ceil(length / 2);
        hindex += 1;
        int five = 5;
        if (length > five) {
            result = source.substring(0, hindex - 3) + asterisk + source.substring(hindex + 1);
        } else {
            result = source.substring(0, 1) + asterisk + source.substring(length - 1);
        }
        return result;
    }

}
