package org.jujubeframework.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.experimental.Accessors;

import java.lang.reflect.Type;

public class JsonsTest {
    @Accessors(chain = true)
    @Data
    public static class User {

        private Long id;
        @JsonProperty("_widget_1513666823960")
        private String name;
    }

    @Test
    public void jsonProperty() {
        String text = "{\"id\":null,\"_widget_1513666823960\":\"90\"}";
        assertThat(Jsons.toJson(new User().setName("90"))).isEqualTo(text);
        User user = Jsons.parseJson(text, User.class);
        assertThat(user.getName()).isEqualTo("90");
    }

    @Accessors(chain = true)
    @Data
    public static class User2 {

        @JsonProperty("_widget_1541348776165")
        private User2Value name;

        @Accessors(chain = true)
        @Data
        public static class User2Value {
            private String value;

            public static User2Value value(String value) {
                return new User2Value().setValue(value);
            }
        }
    }

    @Test
    public void testParseJsonToList() {
        String json = "{\"_widget_1541348776165\":{\"value\":\"abc\"}}";
        assertThat(Jsons.toJson(new User2().setName(v("abc")))).isEqualTo(json);
        User2 user = Jsons.parseJson(json, User2.class);
        assertThat(user.getName().getValue()).isEqualTo("abc");
    }

    private User2.User2Value v(String abc) {
        return User2.User2Value.value(abc);
    }

    @Test
    public void toJons() {
        String json = Jsons.toJson("1");
        String expected = "\"1\"";
        Assertions.assertThat(json).isEqualTo(expected);

        json = Jsons.parseJson(expected, String.class);
        Assertions.assertThat(json).isEqualTo("1");

        json = Jsons.parseJson(expected, (Type)String.class);
        Assertions.assertThat(json).isEqualTo("1");
    }
}
