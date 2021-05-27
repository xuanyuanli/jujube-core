package org.jujubeframework.util.web;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.Validate;
import org.jujubeframework.util.Dates;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet Cookie工具
 * 
 * @author John Li
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Cookies {
    /**
     * 读取指定名称的Cookie值
     */
    public static String getCookie(HttpServletRequest request, String key) {
        Validate.notBlank(key);

        Cookie[] cookies = request.getCookies();
        Cookie ck = null;
        if (cookies != null && cookies.length > 0) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(key)) {
                    ck = cookie;
                    break;
                }
            }
        }
        return ck != null ? ck.getValue() : null;
    }

    /**
     * 添加Cookie
     *
     * @param maxAge
     *            存活时间，以秒为单位
     */
    public static void addCookie(HttpServletResponse response, String key, String value, int maxAge) {
        Validate.notBlank(key);

        Cookie ck = new Cookie(key, value);
        ck.setSecure(true);
        ck.setMaxAge(maxAge);
        response.addCookie(ck);
    }

    /**
     * 添加Cookie到当前会话,浏览器关闭则Cookie失效
     */
    public static void addCookieForSession(HttpServletResponse response, String key, String value) {
        addCookie(response, key, value, -1);
    }

    /**
     * 添加Cookie，在第二天凌晨失效
     */
    public static void addCookieForDayEnded(HttpServletResponse response, String key, String value) {
        addCookie(response, key, value, (int) Dates.endOfToday());
    }

    /**
     * 删除指定key的Cookie
     */
    public static void delCookie(HttpServletResponse response, String key) {
        addCookie(response, key, "", 0);
    }
}
