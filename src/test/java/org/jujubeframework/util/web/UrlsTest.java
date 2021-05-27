package org.jujubeframework.util.web;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author zhangliyong
 */

class UrlsTest {

    @Test
    void parse() {
        Urls.UrlEntity u = new Urls.UrlEntity();
        String url1 = "";
        url1 = "https://blog.csdn.net/wangweiren_get/article/details/82585629";
        u.setBaseUrl(url1);
        assertThat(Urls.parse(url1)).isEqualTo(u);

        String url = "https://wsaddev.artfoxlive.com/getMatchesList?matchesDate=&auctionName=&matchesName=&id=23295&nature=0&auctionType=0";
        Urls.UrlEntity u1 = new Urls.UrlEntity();
        u1.setBaseUrl("https://wsaddev.artfoxlive.com/getMatchesList");
        Map<String,String> quer = new HashMap<>();
        quer.put("id",String.valueOf(23295));
        quer.put("nature",String.valueOf(0));
        quer.put("auctionType",String.valueOf(0));
        u1.setParams(quer);
        assertThat(Urls.parse(url)).isEqualTo(u1);
    }

    @Test
    void encodeURIComponent() {
        assertThat(Urls.encodeURIComponent("https://wsaddev.artfoxlive.com/")).isEqualTo("https%3A%2F%2Fwsaddev.artfoxlive.com%2F");
        assertThat(Urls.encodeURIComponent("https://wsaddev.artfoxlive.com")).isEqualTo("https%3A%2F%2Fwsaddev.artfoxlive.com");
        assertThat(Urls.encodeURIComponent("https://wsaddev.artfoxlive.com/getMatchesList?id=23295"))
                .isEqualTo("https%3A%2F%2Fwsaddev.artfoxlive.com%2FgetMatchesList%3Fid%3D23295");
        assertThat(Urls.encodeURIComponent("https://wsaddev.artfoxlive.com/getMatchesList?matchesDate=&auctionName=%E8%89%BA%E7%8B%90&matchesName=&id=23295"))
                .isEqualTo("https%3A%2F%2Fwsaddev.artfoxlive.com%2FgetMatchesList%3FmatchesDate%3D%26auctionName%3D%25E8%2589%25BA%25E7%258B%2590%26matchesName%3D%26id%3D23295");
        assertThat(Urls.encodeURIComponent("wsaddev.artfoxlive.com/getMatchesList?matchesDate=&auctionName=艺狐&matchesName=&id=23295"))
                .isEqualTo("wsaddev.artfoxlive.com%2FgetMatchesList%3FmatchesDate%3D%26auctionName%3D%E8%89%BA%E7%8B%90%26matchesName%3D%26id%3D23295");

        assertThat(Urls.encodeURIComponent("http://192.168.3.28:801/")).isEqualTo("http%3A%2F%2F192.168.3.28%3A801%2F");
        assertThat(Urls.encodeURIComponent("http://192.168.3.28:801/getUserList?uid=37270"))
                .isEqualTo("http%3A%2F%2F192.168.3.28%3A801%2FgetUserList%3Fuid%3D37270");
        assertThat(Urls.encodeURIComponent("http://192.168.3.28:801/getMatchesList?matchesDate=&auctionName=%E8%89%BA%E7%8B%90&matchesName=&id=23295"))
                .isEqualTo("http%3A%2F%2F192.168.3.28%3A801%2FgetMatchesList%3FmatchesDate%3D%26auctionName%3D%25E8%2589%25BA%25E7%258B%2590%26matchesName%3D%26id%3D23295");
    }

    @Test
    void decodeURIComponent() {
        assertThat(Urls.decodeURIComponent("https%3A%2F%2Fwsaddev.artfoxlive.com%2F")).isEqualTo("https://wsaddev.artfoxlive.com/");
        assertThat(Urls.decodeURIComponent("https%3A%2F%2Fwsaddev.artfoxlive.com")).isEqualTo("https://wsaddev.artfoxlive.com");
        assertThat(Urls.decodeURIComponent("https%3A%2F%2Fwsaddev.artfoxlive.com%2FgetMatchesList%3Fid%3D23295"))
                .isEqualTo("https://wsaddev.artfoxlive.com/getMatchesList?id=23295");
        assertThat(Urls.decodeURIComponent("https%3A%2F%2Fwsaddev.artfoxlive.com%2FgetMatchesList%3FmatchesDate%3D%26auctionName%3D%25E8%2589%25BA%25E7%258B%2590%26matchesName%3D%26id%3D23295"))
                .isEqualTo("https://wsaddev.artfoxlive.com/getMatchesList?matchesDate=&auctionName=%E8%89%BA%E7%8B%90&matchesName=&id=23295");
        assertThat(Urls.decodeURIComponent("wsaddev.artfoxlive.com%2FgetMatchesList%3FmatchesDate%3D%26auctionName%3D%E8%89%BA%E7%8B%90%26matchesName%3D%26id%3D23295"))
                .isEqualTo("wsaddev.artfoxlive.com/getMatchesList?matchesDate=&auctionName=艺狐&matchesName=&id=23295");

        assertThat(Urls.decodeURIComponent("http%3A%2F%2F192.168.3.28%3A801%2F")).isEqualTo("http://192.168.3.28:801/");
        assertThat(Urls.decodeURIComponent("http%3A%2F%2F192.168.3.28%3A801%2FgetUserList%3Fuid%3D37270")).isEqualTo("http://192.168.3.28:801/getUserList?uid=37270");
        assertThat(Urls.decodeURIComponent("http%3A%2F%2F192.168.3.28%3A801%2FgetMatchesList%3FmatchesDate%3D%26auctionName%3D%25E8%2589%25BA%25E7%258B%2590%26matchesName%3D%26id%3D23295"))
                .isEqualTo("http://192.168.3.28:801/getMatchesList?matchesDate=&auctionName=%E8%89%BA%E7%8B%90&matchesName=&id=23295");

    }

    @Test
    void decodeURIComponent1(){
        assertThat(Urls.decodeURIComponent("https%3A%2F%2Fwsaddev.artfoxlive.com%2F")).isEqualTo("https://wsaddev.artfoxlive.com/");
        assertThat(Urls.decodeURIComponent("https%3A%2F%2Fwsaddev.artfoxlive.com")).isEqualTo("https://wsaddev.artfoxlive.com");
        assertThat(Urls.decodeURIComponent("https%3A%2F%2Fwsaddev.artfoxlive.com%2FgetMatchesList%3Fid%3D23295"))
                .isEqualTo("https://wsaddev.artfoxlive.com/getMatchesList?id=23295");
        assertThat(Urls.decodeURIComponent("https%3A%2F%2Fwsaddev.artfoxlive.com%2FgetMatchesList%3FmatchesDate%3D%26auctionName%3D%25E8%2589%25BA%25E7%258B%2590%26matchesName%3D%26id%3D23295"))
                .isEqualTo("https://wsaddev.artfoxlive.com/getMatchesList?matchesDate=&auctionName=%E8%89%BA%E7%8B%90&matchesName=&id=23295");
        assertThat(Urls.decodeURIComponent("wsaddev.artfoxlive.com%2FgetMatchesList%3FmatchesDate%3D%26auctionName%3D%E8%89%BA%E7%8B%90%26matchesName%3D%26id%3D23295"))
                .isEqualTo("wsaddev.artfoxlive.com/getMatchesList?matchesDate=&auctionName=艺狐&matchesName=&id=23295");

        assertThat(Urls.decodeURIComponent("http%3A%2F%2F192.168.3.28%3A801%2F")).isEqualTo("http://192.168.3.28:801/");
        assertThat(Urls.decodeURIComponent("http%3A%2F%2F192.168.3.28%3A801%2FgetUserList%3Fuid%3D37270")).isEqualTo("http://192.168.3.28:801/getUserList?uid=37270");
        assertThat(Urls.decodeURIComponent("http%3A%2F%2F192.168.3.28%3A801%2FgetMatchesList%3FmatchesDate%3D%26auctionName%3D%25E8%2589%25BA%25E7%258B%2590%26matchesName%3D%26id%3D23295"))
                .isEqualTo("http://192.168.3.28:801/getMatchesList?matchesDate=&auctionName=%E8%89%BA%E7%8B%90&matchesName=&id=23295");
    }
}