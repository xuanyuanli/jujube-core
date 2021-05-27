package org.jujubeframework.util;

import org.assertj.core.api.Assertions;
import org.jujubeframework.jdbc.support.pagination.PageableRequest;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class SpElsTest {

    @Test
    public void parseMap() {
        Map<String, Object> root = new HashMap<>(1);
        root.put("map", Collections3.newHashMap("_id", 12));
        Integer result = SpEls.parse("{#map[_id]}", root, Integer.class);
        Assertions.assertThat(result).isEqualTo(12);
    }

    @Test
    public void parseEntity() {
        PageableRequest request = new PageableRequest(1, 10);
        Map<String, Object> root = new HashMap<>(1);
        root.put("request", request);
        Integer result = SpEls.parse("{#request.index}", root, Integer.class);
        Assertions.assertThat(result).isEqualTo(1);
    }

}