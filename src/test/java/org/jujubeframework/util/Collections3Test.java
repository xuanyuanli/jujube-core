package org.jujubeframework.util;

import com.google.common.collect.Lists;
import org.jujubeframework.lang.Record;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

import java.util.List;

public class Collections3Test {

    @Getter
    @Setter
    @Accessors(chain = true)
    public static class User {

        private Long id;
        private String name;
        private Integer age;
        private String blogType;
        private String log_type;

        public void setT(String tname, int tage, double tprice) {
        }
    }

    @Getter
    @Setter
    @Accessors(chain = true)
    @ToString
    public static class ChildUser extends User {
        private Long cardId;
        private Double price;
    }

    @Test
    public void testExtractToListString() {
        ChildUser user = (ChildUser) new ChildUser().setCardId(123L).setName("bc");
        ChildUser user2 = (ChildUser) new ChildUser().setCardId(12L).setName("ef");
        ChildUser user3 = (ChildUser) new ChildUser().setCardId(36L).setName("df");
        List<ChildUser> data = Lists.newArrayList(user, user2, user3);
        List<String> list = Collections3.extractToListString(data, "name");
        assertThat(list).hasSize(3).contains("bc", "ef", "df");
    }

    @Test
    public void testSort() {
        ChildUser user = (ChildUser) new ChildUser().setCardId(123L).setName("bc");
        ChildUser user2 = (ChildUser) new ChildUser().setCardId(12L).setName("ef");
        ChildUser user3 = (ChildUser) new ChildUser().setCardId(36L).setName("df");
        List<ChildUser> data = Lists.newArrayList(user, user2, user3);
        Collections3.sort(data, "name", true);
        List<String> list = Collections3.extractToListString(data, "name");
        assertThat(list).hasSize(3).containsSequence("bc", "df", "ef");
    }

    @Test
    public void testGetOne() {
        ChildUser user = (ChildUser) new ChildUser().setCardId(123L).setName("bc");
        ChildUser user2 = (ChildUser) new ChildUser().setCardId(12L).setName("ef");
        ChildUser user3 = (ChildUser) new ChildUser().setCardId(36L).setName("df");
        List<ChildUser> data = Lists.newArrayList(user, user2, user3);
        assertThat(Collections3.getOne(data, "cardId", 123L)).isEqualTo(user);

        Record record = new Record().set("id", 34L);
        List<Record> list = Lists.newArrayList(record);
        long id = 34;
        assertThat(Collections3.getOne(list, "id", id)).hasSize(1);

    }

    @Test
    public void testGetPart() {
        ChildUser user = (ChildUser) new ChildUser().setCardId(12L).setName("bc");
        ChildUser user2 = (ChildUser) new ChildUser().setCardId(12L).setName("ef");
        ChildUser user3 = (ChildUser) new ChildUser().setCardId(36L).setName("df");
        List<ChildUser> data = Lists.newArrayList(user, user2, user3);
        assertThat(Collections3.getPart(data, "cardId", 12L)).hasSize(2).contains(user, user2);
    }

    @Test
    public void testToDiffArray() {
        String[] arr = { "1", "1", "2" };
        assertThat(Collections3.toDiffArray(arr)).hasSize(2);
    }

    @Test
    public void testExtractToMap() {
        ChildUser user = (ChildUser) new ChildUser().setCardId(12L).setName("bc");
        ChildUser user2 = (ChildUser) new ChildUser().setCardId(123L).setName("ef");
        ChildUser user3 = (ChildUser) new ChildUser().setCardId(36L).setName("df");
        List<ChildUser> data = Lists.newArrayList(user, user2, user3);
        assertThat(Collections3.extractToMap(data, "cardId", "name")).hasSize(3).containsEntry(12L, "bc");
    }

    @Test
    public void testExtractToList() {
        ChildUser user = (ChildUser) new ChildUser().setCardId(12L).setName("bc");
        ChildUser user2 = (ChildUser) new ChildUser().setCardId(123L).setName("ef");
        ChildUser user3 = (ChildUser) new ChildUser().setCardId(36L).setName("df");
        List<ChildUser> data = Lists.newArrayList(user, user2, user3);
        assertThat(Collections3.extractToListString(data, "name")).hasSize(3).contains("bc", "df", "ef");
    }

    @Test
    public void testUnion() {
        ChildUser user = (ChildUser) new ChildUser().setCardId(12L).setName("bc");
        ChildUser user2 = (ChildUser) new ChildUser().setCardId(123L).setName("ef");
        ChildUser user3 = (ChildUser) new ChildUser().setCardId(36L).setName("df");
        List<ChildUser> data = Lists.newArrayList(user);
        List<ChildUser> data2 = Lists.newArrayList(user2, user3);
        assertThat(Collections3.union(data, data2)).hasSize(3).contains(user, user2, user3);
    }

    @Test
    public void testSubtract() {
        ChildUser user = (ChildUser) new ChildUser().setCardId(12L).setName("bc");
        ChildUser user2 = (ChildUser) new ChildUser().setCardId(123L).setName("ef");
        ChildUser user3 = (ChildUser) new ChildUser().setCardId(36L).setName("df");
        List<ChildUser> data = Lists.newArrayList(user, user2);
        List<ChildUser> data2 = Lists.newArrayList(user2, user3);
        assertThat(Collections3.subtract(data, data2)).hasSize(1).contains(user);
    }

    @Test
    public void testIntersection() {
        ChildUser user = (ChildUser) new ChildUser().setCardId(12L).setName("bc");
        ChildUser user2 = (ChildUser) new ChildUser().setCardId(123L).setName("ef");
        ChildUser user3 = (ChildUser) new ChildUser().setCardId(36L).setName("df");
        List<ChildUser> data = Lists.newArrayList(user, user2);
        List<ChildUser> data2 = Lists.newArrayList(user2, user3);
        assertThat(Collections3.intersection(data, data2)).hasSize(1).contains(user2);
    }

    @Test
    public void testContainsFieldValue() {
        ChildUser user = (ChildUser) new ChildUser().setCardId(12L).setName("bc");
        ChildUser user2 = (ChildUser) new ChildUser().setCardId(123L).setName("ef");
        ChildUser user3 = (ChildUser) new ChildUser().setCardId(36L).setName("df");
        List<ChildUser> data = Lists.newArrayList(user, user2, user3);
        assertThat(Collections3.containsFieldValue(data, "name", "df")).isTrue();
        assertThat(Collections3.containsFieldValue(data, "name", "df3")).isFalse();
    }

    @Test
    public void distinctByProperty() {
        ChildUser user = (ChildUser) new ChildUser().setCardId(12L).setName("bc");
        ChildUser user2 = (ChildUser) new ChildUser().setCardId(12L).setName("ef");
        ChildUser user3 = (ChildUser) new ChildUser().setCardId(123L).setName("df");
        List<ChildUser> data = Lists.newArrayList(user, user2, user3);
        List<ChildUser> list = Collections3.distinctByProperty(data, "cardId");
        assertThat(list).hasSize(2);
    }

}
