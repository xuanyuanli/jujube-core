package org.jujubeframework.util.web;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;
import java.util.Map.Entry;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.collect.Maps;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.UriUtils;
import org.springframework.web.util.WebUtils;

import com.google.common.base.Charsets;
import com.google.common.net.HttpHeaders;
import org.jujubeframework.util.Collections3;
import org.jujubeframework.util.Texts;

/**
 * Servlet相关工具类
 * 
 * @author John Li
 */
public class Servlets {

    public static final long ONE_YEAR_SECONDS = 60L * 60L * 24L * 365L;
    static final Logger logger = LoggerFactory.getLogger(Servlets.class);
    public static final String UNKNOWN = "unknown";

    private Servlets() {
    }

    /**
     * 设置客户端缓存过期时间 的Header.
     */
    public static void setExpiresHeader(HttpServletResponse response, long expiresSeconds) {
        // Http 1.0 header, set a fix expires date.
        response.setDateHeader(HttpHeaders.EXPIRES, System.currentTimeMillis() + (expiresSeconds * 1000));
        // Http 1.1 header, set a time after now.
        response.setHeader(HttpHeaders.CACHE_CONTROL, "private, max-age=" + expiresSeconds);
    }

    /**
     * 设置禁止客户端缓存的Header.
     */
    public static void setNoCacheHeader(HttpServletResponse response) {
        // Http 1.0 header
        response.setDateHeader(HttpHeaders.EXPIRES, 1L);
        response.addHeader(HttpHeaders.PRAGMA, "no-cache");
        // Http 1.1 header
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, max-age=0");
    }

    /**
     * 设置LastModified Header.
     */
    public static void setLastModifiedHeader(HttpServletResponse response, long lastModifiedDate) {
        response.setDateHeader(HttpHeaders.LAST_MODIFIED, lastModifiedDate);
    }

    /**
     * 设置Etag Header.
     */
    public static void setEtag(HttpServletResponse response, String etag) {
        response.setHeader(HttpHeaders.ETAG, etag);
    }

    /**
     * 根据浏览器If-Modified-Since Header, 计算文件是否已被修改.
     * 
     * 如果无修改, checkIfModify返回false ,设置304 not modify status.
     * 
     * @param lastModified
     *            内容的最后修改时间.
     */
    public static boolean checkIfModifiedSince(HttpServletRequest request, HttpServletResponse response, long lastModified) {
        long ifModifiedSince = request.getDateHeader(HttpHeaders.IF_MODIFIED_SINCE);
        int thousand = 1000;
        if ((ifModifiedSince != -1) && (lastModified < (ifModifiedSince + thousand))) {
            response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            return false;
        }
        return true;
    }

    /**
     * 根据浏览器 If-None-Match Header, 计算Etag是否已无效.
     * 
     * 如果Etag有效, checkIfNoneMatch返回false, 设置304 not modify status.
     * 
     * @param etag
     *            内容的ETag.
     */
    public static boolean checkIfNoneMatchEtag(HttpServletRequest request, HttpServletResponse response, String etag) {
        String headerValue = request.getHeader(HttpHeaders.IF_NONE_MATCH);
        if (headerValue != null) {
            boolean conditionSatisfied = false;
            String symbol = "*";
            if (!symbol.equals(headerValue)) {
                StringTokenizer commaTokenizer = new StringTokenizer(headerValue, ",");

                while (!conditionSatisfied && commaTokenizer.hasMoreTokens()) {
                    String currentToken = commaTokenizer.nextToken();
                    if (currentToken.trim().equals(etag)) {
                        conditionSatisfied = true;
                    }
                }
            } else {
                conditionSatisfied = true;
            }

            if (conditionSatisfied) {
                response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                response.setHeader(HttpHeaders.ETAG, etag);
                return false;
            }
        }
        return true;
    }

    /**
     * 设置让浏览器弹出下载对话框的Header.
     * 
     * @param fileName
     *            下载后的文件名.
     */
    public static void setFileDownloadHeader(HttpServletRequest request, HttpServletResponse response, String fileName) {
        // 中文文件名支持
        // 替换空格，否则firefox下有空格文件名会被截断,其他浏览器会将空格替换成+号
        String encodedfileName = fileName.trim().replaceAll(" ", "_");
        String agent = request.getHeader("User-Agent");
        boolean isMSIE = (agent != null && (agent.toUpperCase().indexOf("MSIE") != -1 || agent.toUpperCase().indexOf("Edge") != -1));
        if (isMSIE) {
            encodedfileName = urlEncode(fileName);
        } else {
            encodedfileName = new String(fileName.getBytes(), Charsets.ISO_8859_1);
        }
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encodedfileName + "\"");
    }

    /**
     * URL 编码, Encode默认为UTF-8.
     */
    public static String urlEncode(String part) {
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
        if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip) || isInvalidIp(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    /**
     * 获得真实ip地址
     */
    public static String getIpAddr(Map<String, String> headers) {
        String ip = headers.get("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip) || isInvalidIp(ip)) {
            ip = headers.get("x-forwarded-for");
        }
        if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip) || isInvalidIp(ip)) {
            ip = headers.get("X-Real-IP");
        }
        if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip) || isInvalidIp(ip)) {
            ip = headers.get("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip) || isInvalidIp(ip)) {
            ip = headers.get("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip) || isInvalidIp(ip)) {
            ip = headers.get("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip) || isInvalidIp(ip)) {
            ip = headers.get("HTTP_X_FORWARDED_FOR");
        }
        String symbol = ":";
        if (StringUtils.isBlank(ip) || ip.contains(symbol)) {
            ip = "106.37.236.148";
        }
        String symbol1 = ",";
        if (ip != null && ip.contains(symbol1)) {
            ip = ip.split(symbol1)[0];
        }
        int limit = 15;
        if (ip.length() > limit) {
            ip = ip.substring(0, limit);
        }
        return ip;
    }

    /** 是否是无效ip */
    private static boolean isInvalidIp(String ip) {
        return ip.trim().startsWith("127.0.0") || ip.startsWith("0:0:0");
    }

    /**
     * 获得客户端浏览器<br>
     * 参考了Jquery1.8的实现
     */
    public static String getBrowser(final  HttpServletRequest request) {
        HashSet<String> set = new HashSet<String>(Collections3.enumerationToList(request.getHeaderNames()));
        Map<String,String> map = Maps.asMap(set, name -> request.getHeader(name));
        return getBrowser(map);
    }

    /**
     * 获得客户端浏览器<br>
     * 参考了Jquery1.8的实现
     */
    public static String getBrowser(Map<String, String> headers) {
        String browser = "";
        String userAgent = headers.get("User-Agent");
        if (StringUtils.isBlank(userAgent)) {
            userAgent = headers.get("user-agent");
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
        if (matched.length == 0 && userAgent.indexOf(compatible) < 0) {
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
     * 
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
        Map<String, Object> params = new TreeMap<String, Object>();
        if (suffix == null) {
            suffix = "";
        }
        while (paramNames != null && paramNames.hasMoreElements()) {
            String paramName = paramNames.nextElement();
            if ("".equals(suffix) || paramName.endsWith(suffix)) {
                String[] values = request.getParameterValues(paramName);
                if (values == null || values.length == 0) {
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

    /** 对参数进行encodeURIComponent */
    public static String encodeUrlParam(Object param) {
        String value = String.valueOf(param);
        return UriUtils.encode(value, "utf-8");
    }


    /** 获得完整的访问Url */
    public static String getFullUrl(HttpServletRequest request) {
        return request.getRequestURL().append("?").append(request.getQueryString()).toString();
    }

    /** java版的decodeURIComponent */
    public static String decodeURIComponent(String url) {
        url = UriUtils.decode(url, "UTF-8");
        url = StringEscapeUtils.unescapeHtml4(url);
        return url;
    }

    /** java版的encodeURIComponent */
    public static String encodeURIComponent(String url) {
        return StringEscapeUtils.unescapeHtml4(UriUtils.encode(url, "UTF-8"));
    }


    /** 获得当前线程的Request */
    public static HttpServletRequest getCurrentHttpServletRequest() {
        ServletRequestAttributes servletRequestAttributes = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes());
        if (servletRequestAttributes != null) {
            return servletRequestAttributes.getRequest();
        }
        return null;
    }

    /** 获得Map版Header */
    public static Map<String, String> getFormatHeader(HttpServletRequest request) {
        List<String> headerNames = Collections3.enumerationToList(request.getHeaderNames());
        Map<String, String> result = new HashMap<>(headerNames.size());
        for (String name : headerNames) {
            result.put(name, request.getHeader(name));
        }
        return result;
    }

    /** 获得Map版Paramter */
    public static Map<String, String> getFormatParamter(HttpServletRequest request) {
        Map<String, String[]> pMap = request.getParameterMap();
        Map<String, String> result = new HashMap<>(pMap.size());
        for (String key : pMap.keySet()) {
            String[] values = pMap.get(key);
            result.put(key, StringUtils.join(values, ","));
        }
        return result;
    }

    /** request的Accept是否是html */
    public static boolean requestAcceptIsHtml(HttpServletRequest request) {
        HttpRequest httpRequest = new ServletServerHttpRequest(request);
        List<MediaType> accept = httpRequest.getHeaders().getAccept();
        if (accept != null) {
            return accept.contains(MediaType.TEXT_HTML) || accept.contains(MediaType.APPLICATION_XHTML_XML);
        }
        return false;
    }
}
