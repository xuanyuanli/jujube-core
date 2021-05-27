package org.jujubeframework.jdbc.support.entity;

import com.google.common.collect.Lists;
import lombok.Data;
import org.assertj.core.api.Assertions;
import org.jujubeframework.lang.Record;
import org.junit.jupiter.api.Test;

import java.util.List;

public class BaseEntityTest {

    @Test
    public void toRecord() {
        TestEntity entity = new TestEntity();
        Assertions.assertThat(entity.toRecord()).isEqualTo(new Record());
    }

    @Test
    public void toMap() {
    }

    @Test
    public void toMapFilterNull() {
    }

    @Test
    public void toBO() {
    }

    @Test
    public void cloneSelf() {
        CloneEntity cloneEntity = new CloneEntity();
        cloneEntity.setAge(12);
        cloneEntity.setName("abc");
        TestBO bo = new TestBO();
        bo.setName("efg");
        cloneEntity.setBo(bo);
        cloneEntity.setList(Lists.newArrayList("7"));
        CloneEntity target = cloneEntity.cloneSelf();
        Assertions.assertThat(target.getAge()).isEqualTo(cloneEntity.getAge());
        Assertions.assertThat(target.getName()).isEqualTo(cloneEntity.getName());
        Assertions.assertThat(target.getBo()).isEqualTo(cloneEntity.getBo());
        Assertions.assertThat(target.getList()).isEqualTo(cloneEntity.getList());
    }

    @Data
    public static class TestBO implements BaseEntity {
        private String name;
    }

    @Data
    public static class TestEntity implements BaseEntity {
        private String name;
        private Integer age;
        private List<String> list;
        private TestBO bo;
    }

    @Data
    public static class CloneEntity extends TestEntity {
        private Integer type;
    }
}