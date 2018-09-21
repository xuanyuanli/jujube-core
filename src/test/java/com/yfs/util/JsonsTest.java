package com.yfs.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Test;

public class JsonsTest {

    @Test
    public void testParseJsonToList() {
        List<Long> ids = Jsons.parseJsonToListLong("[1,2,3]");
        assertThat(ids).hasSize(3).containsSequence(1L, 2L, 3L);
    }

}
