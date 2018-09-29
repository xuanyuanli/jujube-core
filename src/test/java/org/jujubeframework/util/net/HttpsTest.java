package org.jujubeframework.util.net;

import com.mashape.unirest.http.Unirest;
import org.assertj.core.api.Assertions;
import static org.junit.Assert.*;
import org.junit.Test;

public class HttpsTest {

    @Test
    public void getStatusCode() {
    }

    @Test
    public void getAsString() {
    }

    @Test
    public void postAsString() {
    }

    @Test
    public void setTimeouts() {
        Https.setTimeouts(2000,2000);
    }

    @Test
    public void setConcurrency() {
        Https.setConcurrency(20,20);
    }

    @Test
    public void getAsStream() {
    }
}