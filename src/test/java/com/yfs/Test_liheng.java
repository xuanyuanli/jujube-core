package com.yfs;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;

public class Test_liheng {
    public static void main(String[] args) throws Exception {
        HttpResponse<String> httpResponse = Unirest.get("https://www.baidu.com/").asString();
        String bodyText = httpResponse.getBody();
        System.out.print(bodyText);
    }

}
