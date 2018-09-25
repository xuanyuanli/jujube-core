package org.jujubeframework.util.web;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @author John Li
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Controllers {
    public static final String SUCCESS = "success";
    public static final String FAIL = "fail";

    /**
     * 重定向
     *
     * @param url url
     */
    public static String redirect(String url) {
        return "redirect:" + url;
    }

    /**
     * 重定向到404
     */
    public static String redirectTo404() {
        return "redirect:/error/404";
    }
}
