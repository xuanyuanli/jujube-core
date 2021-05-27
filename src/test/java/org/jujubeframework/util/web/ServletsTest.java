package org.jujubeframework.util.web;

import org.jujubeframework.util.Collections3;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;

class ServletsTest {

    @Test
    void getIpAddr() {
        assertThat(Servlets.getIpAddr(new HashMap<>())).isNull();
        String ip = "127.0.0.5";
        assertThat(Servlets.getIpAddr(Collections3.newHashMap("X-Forwarded-For", ip))).isEqualTo(ip);
        assertThat(Servlets.getIpAddr(Collections3.newHashMap("x-forwarded-for", ip))).isEqualTo(ip);
        assertThat(Servlets.getIpAddr(Collections3.newHashMap("X-Real-IP", ip))).isEqualTo(ip);
        assertThat(Servlets.getIpAddr(Collections3.newHashMap("Proxy-Client-IP", ip))).isEqualTo(ip);
        assertThat(Servlets.getIpAddr(Collections3.newHashMap("WL-Proxy-Client-IP", ip))).isEqualTo(ip);
        assertThat(Servlets.getIpAddr(Collections3.newHashMap("HTTP_CLIENT_IP", ip))).isEqualTo(ip);
        assertThat(Servlets.getIpAddr(Collections3.newHashMap("HTTP_X_FORWARDED_FOR", ip))).isEqualTo(ip);
    }
}