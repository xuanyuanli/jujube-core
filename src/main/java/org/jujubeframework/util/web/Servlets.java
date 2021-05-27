package org.jujubeframework.util.web;

import com.google.common.base.Charsets;
import com.google.common.net.HttpHeaders;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.jujubeframework.util.Beans;
import org.jujubeframework.util.Collections3;
import org.jujubeframework.util.Texts;
import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.springframework.web.util.UriUtils;
import org.springframework.web.util.WebUtils;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Servlet相关工具类
 *
 * @author John Li
 */
public class Servlets {

    private static final String UNKNOWN = "unknown";

    private Servlets() {
    }

    /**
     * 设置让浏览器弹出下载对话框的Header.<br>
     * 如果使用Spring MVC的方式，注意在@RequestMapping上添加produces =
     * MediaType.APPLICATION_OCTET_STREAM_VALUE
     *
     * @param fileName
     *            下载后的文件名.
     */
    public static void setFileDownloadHeader(HttpServletRequest request, HttpServletResponse response, String fileName) {
        // 中文文件名支持
        // 替换空格，否则firefox下有空格文件名会被截断,其他浏览器会将空格替换成+号
        fileName = fileName.trim().replaceAll(" ", "_");
        String encodedfileName;
        String agent = request.getHeader("User-Agent");
        boolean isMsie = (agent != null && (agent.toUpperCase().contains("MSIE") || agent.toUpperCase().contains("EDGE")));
        if (isMsie) {
            encodedfileName = urlEncode(fileName);
        } else {
            encodedfileName = new String(fileName.getBytes(), Charsets.ISO_8859_1);
        }
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encodedfileName + "\"");
        response.setContentType("application/octet-stream");
    }

    /**
     * URL 编码, Encode默认为UTF-8.
     */
    private static String urlEncode(String part) {
        try {
            return URLEncoder.encode(part, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 客户端对Http Basic验证的 Header进行编码.
     */
    public static String encodeHttpBasic(String userName, String password) {
        String encode = userName + ":" + password;
        return "Basic " + Base64.encodeBase64String(encode.getBytes());
    }

    /**
     * 获得真实ip地址
     */
    public static String getIpAddr(HttpServletRequest request) {
        String ip = getIpAddr(getFormatHeader(request));
        if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip) || "127.0.0.1".equals(ip.trim()) || ip.startsWith("0:0:0")) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    /**
     * 获得真实ip地址
     */
    public static String getIpAddr(Map<String, String> headers) {
        String ip = getHeaderOfIgnoringCase(headers, "X-Forwarded-For");
        if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = getHeaderOfIgnoringCase(headers, "X-Real-IP");
        }
        if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = getHeaderOfIgnoringCase(headers, "Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = getHeaderOfIgnoringCase(headers, "WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = getHeaderOfIgnoringCase(headers, "HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = getHeaderOfIgnoringCase(headers, "HTTP_X_FORWARDED_FOR");
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0];
        }
        int limit = 15;
        if (ip != null && ip.length() > limit) {
            ip = ip.substring(0, limit);
        }
        return ip;
    }

    /** 忽略大小写的getHeader */
    private static String getHeaderOfIgnoringCase(Map<String, String> headers, String headerName) {
        String value = headers.get(headerName);
        if (StringUtils.isEmpty(value)) {
            value = headers.get(headerName.toLowerCase());
        }
        return value;
    }

    /**
     * 获得客户端浏览器<br>
     * 参考了Jquery1.8的实现
     */
    public static String getBrowser(final HttpServletRequest request) {
        return getBrowser(getFormatHeader(request));
    }

    /**
     * 获得客户端浏览器<br>
     * 参考了Jquery1.8的实现
     */
    public static String getBrowser(Map<String, String> headers) {
        String browser = "";
        String userAgent = getHeaderOfIgnoringCase(headers, "User-Agent");
        if (StringUtils.isBlank(userAgent)) {
            return browser;
        }
        userAgent = userAgent.toLowerCase();
        String[] matched = Texts.getGroups("(chrome)[ \\/]([\\w.]+)", userAgent);
        if (matched.length == 0) {
            matched = Texts.getGroups("(webkit)[ \\/]([\\w.]+)", userAgent);
        }
        if (matched.length == 0) {
            matched = Texts.getGroups("(opera)(?:.*version|)[ \\/]([\\w.]+)", userAgent);
        }
        if (matched.length == 0) {
            matched = Texts.getGroups("(msie) ([\\w.]+)", userAgent);
        }
        String compatible = "compatible";
        if (matched.length == 0 && !userAgent.contains(compatible)) {
            matched = Texts.getGroups("(mozilla)(?:.*? rv:([\\w.]+)|)", userAgent);
        }
        int two = 2;
        int three = 3;
        if (matched.length == two) {
            browser = matched[1];
        } else if (matched.length == three) {
            browser = matched[1] + " " + matched[two];
        }
        return browser;
    }

    /**
     * 取得带相同前缀的Request Parameters, copy from spring WebUtils.
     * <p>
     * 返回的结果的Parameter名已去除前缀.
     */
    public static Map<String, Object> getParametersStartingWith(ServletRequest request, String prefix) {
        return WebUtils.getParametersStartingWith(request, prefix);
    }

    /**
     * 取得带相同后缀的Request Parameters
     */
    public static Map<String, Object> getParametersEndingWith(ServletRequest request, String suffix) {
        Enumeration<String> paramNames = request.getParameterNames();
        Map<String, Object> params = new TreeMap<>();
        if (suffix == null) {
            suffix = "";
        }
        while (paramNames != null && paramNames.hasMoreElements()) {
            String paramName = paramNames.nextElement();
            if ("".equals(suffix) || paramName.endsWith(suffix)) {
                String[] values = request.getParameterValues(paramName);
                if (values == null || values.length == 0) {
                    // ignored
                } else if (values.length > 1) {
                    params.put(paramName, values);
                } else {
                    params.put(paramName, values[0]);
                }
            }
        }
        return params;
    }

    /**
     * 组合Parameters生成Query String的Parameter部分, 并在paramter name上加上prefix.
     *
     * @see #getParametersStartingWith
     */
    public static String encodeParameterStringWithPrefix(Map<String, Object> params, String prefix) {
        if ((params == null) || (params.size() == 0)) {
            return "";
        }

        if (prefix == null) {
            prefix = "";
        }

        StringBuilder queryStringBuilder = new StringBuilder();
        Iterator<Entry<String, Object>> it = params.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, Object> entry = it.next();
            // 还原数组的url形式
            if (entry.getValue() instanceof Object[]) {
                Object[] arr = (Object[]) entry.getValue();
                for (int i = 0; i < arr.length; i++) {
                    if (i != arr.length - 1) {
                        queryStringBuilder.append(prefix).append(entry.getKey()).append('=').append(encodeUrlParam(arr[i])).append("&");
                    } else {
                        queryStringBuilder.append(prefix).append(entry.getKey()).append('=').append(encodeUrlParam(arr[i]));
                    }
                }
            } else {
                queryStringBuilder.append(prefix).append(entry.getKey()).append('=').append(encodeUrlParam(entry.getValue()));
            }
            if (it.hasNext()) {
                queryStringBuilder.append('&');
            }
        }
        return queryStringBuilder.toString();
    }

    /**
     * 对参数进行encodeURIComponent
     */
    public static String encodeUrlParam(Object param) {
        String value = String.valueOf(param);
        return UriUtils.encode(value, "utf-8");
    }

    /**
     * 获得完整的访问Url
     */
    public static String getFullUrl(HttpServletRequest request) {
        return request.getRequestURL().append("?").append(request.getQueryString()).toString();
    }


    /** 是否是web环境 */
    public static boolean isWebEnviroument() {
        try {
            Beans.forName("javax.servlet.http.HttpServletRequest");
            return true;
        } catch (Throwable e) {
            return false;
        }
    }

    /**
     * 获得当前线程的Request
     */
    public static HttpServletRequest getCurrentHttpServletRequest() {
        ServletRequestAttributes servletRequestAttributes = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes());
        if (servletRequestAttributes != null) {
            return servletRequestAttributes.getRequest();
        }
        return null;
    }

    /**
     * 获得Map版Header
     */
    public static Map<String, String> getFormatHeader(HttpServletRequest request) {
        return Collections3.enumerationToList(request.getHeaderNames()).stream().collect(Collectors.toMap(t->t, request::getHeader));
    }

    /**
     * 获得Map版Paramter
     */
    public static Map<String, String> getFormatParamter(HttpServletRequest request) {
        Map<String, String[]> pMap = request.getParameterMap();
        Map<String, String> result = new HashMap<>(pMap.size());
        for (String key : pMap.keySet()) {
            String[] values = pMap.get(key);
            result.put(key, StringUtils.join(values, ","));
        }
        return result;
    }

    /**
     * request的Accept是否是html
     */
    public static boolean requestAcceptIsHtml(HttpServletRequest request) {
        HttpRequest httpRequest = new ServletServerHttpRequest(request);
        List<MediaType> accept = httpRequest.getHeaders().getAccept();
        if (accept != null) {
            return accept.contains(MediaType.TEXT_HTML) || accept.contains(MediaType.APPLICATION_XHTML_XML);
        }
        return false;
    }

    /** 获得资源国际化的当前Locale对象 */
    public static Locale getCurrentLocale() {
        if (isWebEnviroument()) {
            HttpServletRequest request = getCurrentHttpServletRequest();
            Locale locale = Locale.CHINA;
            if (request != null) {
                locale = RequestContextUtils.getLocale(request);
                if (!locale.equals(Locale.CHINA) && !locale.equals(Locale.US)) {
                    locale = Locale.US;
                }
            }
            return locale;
        } else {
            return Locale.CHINA;
        }
    }
}
