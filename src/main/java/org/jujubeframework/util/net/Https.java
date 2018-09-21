package org.jujubeframework.util.net;

import java.io.InputStream;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * http相关工具类
 *
 * @author John Li
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Https {
    public static final String SET_TIMEOUTS = "setTimeouts";
    public static final String SET_CONCURRENCY = "setConcurrency";
    private static ConcurrentMap<String, Boolean> GLOB_BOOL = new ConcurrentHashMap<>();

    /** 获得页面返回码 */
    public static int getStatusCode(String href) {
        try {
            return Unirest.get(href).asString().getStatus();
        } catch (UnirestException e) {
            return 500;
        }
    }

    /** get请求获得页面内容.如果报错，则返回空字符串 */
    public static String getAsString(String href) {
        try {
            return Unirest.get(href).asString().getBody();
        } catch (UnirestException e) {
            return "";
        }
    }

    /** post请求获得页面内容.如果报错，则返回空字符串 */
    public static String postAsString(String href) {
        try {
            return Unirest.post(href).asString().getBody();
        } catch (UnirestException e) {
            return "";
        }
    }

    /** 设置全局的超时时间(全局设置，一个JVM中只允许设置一次) */
    public static void setTimeouts(long connectionTimeout, long socketTimeout) {
        // Unirest.refresh()如果多次调用，会生成多个SyncIdleConnectionMonitorThread线程，所以这里做一下处理
        if (!GLOB_BOOL.containsKey(SET_TIMEOUTS)) {
            GLOB_BOOL.put(SET_TIMEOUTS, true);
            Unirest.setTimeouts(connectionTimeout, socketTimeout);
        }
    }

    /** 设置全局的并发数(全局设置，一个JVM中只允许设置一次) */
    public static void setConcurrency(int maxTotal, int maxPerRoute) {
        // Unirest.refresh()如果多次调用，会生成多个SyncIdleConnectionMonitorThread线程，所以这里做一下处理
        if (!GLOB_BOOL.containsKey(SET_CONCURRENCY)) {
            GLOB_BOOL.put(SET_CONCURRENCY, true);
            Unirest.setConcurrency(maxTotal, maxPerRoute);
        }
    }

    public static InputStream getAsStream(String url) {
        try {
            return Unirest.get(url).asBinary().getBody();
        } catch (UnirestException e) {
            throw new RuntimeException(e);
        }
    }
}
