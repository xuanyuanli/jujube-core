package org.jujubeframework.util.web;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.web.util.UriUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * url处理工具类
 *
 * @author John Li
 */
@NoArgsConstructor
public class Urls {

    /** 解析url结果 */
    @Data
    public static class UrlEntity {
        /**
         * 基础url
         */
        private String baseUrl;
        /**
         * url参数
         */
        private Map<String, String> params;
    }

    /**
     * 解析url
     */
    public static UrlEntity parse(String url) {
        UrlEntity entity = new UrlEntity();
        if (StringUtils.isBlank(url)) {
            return entity;
        }
        url = url.trim();
        String[] urlParts = url.split("\\?");
        entity.baseUrl = urlParts[0];
        // 没有参数
        if (urlParts.length == 1) {
            return entity;
        }
        // 有参数
        String[] params = urlParts[1].split("&");
        entity.params = new HashMap<>(params.length);
        for (String param : params) {
            String[] keyValue = param.split("=");
            if (keyValue.length >= 2) {
                entity.params.put(keyValue[0], keyValue[1]);
            }
        }
        return entity;
    }

    /**
     * java版的encodeURIComponent
     */
    public static String encodeURIComponent(String s) {
        String result;
        try {
            result = URLEncoder.encode(s, "UTF-8").replaceAll("\\+", "%20").replaceAll("%21", "!").replaceAll("%27", "'").replaceAll("%28", "(").replaceAll("%29", ")")
                    .replaceAll("%7E", "~");
        }
        // This exception should never occur.
        catch (UnsupportedEncodingException e) {
            result = s;
        }
        return result;
    }

    /**
     * java版的decodeURIComponent
     */
    public static String decodeURIComponent(String url) {
        if (url != null) {
            url = UriUtils.decode(url, "UTF-8");
            url = StringEscapeUtils.unescapeHtml4(url);
            return url;
        }
        return "";
    }

    /** */
    public static String decodeURIComponent1(String url) throws UnsupportedEncodingException {
        if (url != null) {
            url = URLDecoder.decode(url, "UTF-8");
            return url;
        }
        return "";
    }

}
