package org.jujubeframework.util;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.math.NumberUtils;
import org.jujubeframework.lang.Record;
import org.jujubeframework.util.Pojos.FieldMapping;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class PojosTests {

    @Test
    public void mapping() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", "123");
        map.put("name", "abc");
        map.put("age", "12");
        map.put("btype", "1");
        map.put("ltype", "2");

        // 笨方法实现Bean复制
        ChildUser user = new ChildUser();
        user.setId(NumberUtils.toLong(String.valueOf(map.get("id"))));
        user.setAge(NumberUtils.toInt(String.valueOf(map.get("age"))));
        user.setName(String.valueOf(map.get("name")));
        user.setBlogType(String.valueOf(map.get("btype")));
        user.setLog_type(String.valueOf(map.get("ltype")));
        assertThat(user.getBlogType()).isEqualTo(map.get("btype"));
        assertThat(user.getLog_type()).isEqualTo(map.get("ltype"));

        // 升级版
        ChildUser user1 = Pojos.mapping(map, ChildUser.class);
        user1.setBlogType(String.valueOf(map.get("btype")));
        user1.setLog_type(String.valueOf(map.get("ltype")));
        assertThat(user1.getBlogType()).isEqualTo(map.get("btype"));
        assertThat(user1.getLog_type()).isEqualTo(map.get("ltype"));

        // 高端版
        ChildUser user2 = Pojos.mapping(map, ChildUser.class, new FieldMapping().field("btype", "blogType").field("ltype", "log_type"));
        assertThat(user2.getBlogType()).isEqualTo(map.get("btype"));
        assertThat(user2.getLog_type()).isEqualTo(map.get("ltype"));
        assertThat(user2.getAge()).isEqualTo(12);
    }

    @Test
    public void mapping2() {
        Record record = new Record().set("a_pass_password", "123").set("a_pass", 0).set("nameType", 1);
        Material material = Pojos.mapping(record, Material.class);
        assertThat(material.getAPass()).isEqualTo(0);
        assertThat(material.getNameType()).isEqualTo(1);
        assertThat(material.getAPassPassword()).isEqualTo("123");
    }

    @Test
    public void mapping3() {
        Record record = new Record().set("a_pass_password_", "123").set("a_pass_", 0);
        Material material = Pojos.mapping(record, Material.class);
        assertThat(material.getAPass()).isEqualTo(0);
        assertThat(material.getAPassPassword()).isEqualTo("123");
    }

    @Test
    public void mapping4() {
        Record record = new Record().set("id", 1).set("pid", 256);
        User user = Pojos.mapping(record, User.class, new FieldMapping().field("pid", "id"));
        assertThat(user.getId()).isEqualTo(256);
    }

    @Test
    public void mapping5() {
        Record record = new Record().set("blog_type", 1);
        ChildUser user = Pojos.mapping(record, ChildUser.class, new FieldMapping().field("blog_type", "blogType"));
        assertThat(user.getBlogType()).isEqualTo("1");
    }

    @Test
    public void mapping6() {
        List<User> users = Pojos.mappingArray(null, User.class);
        assertThat(users).isEqualTo(null);
    }

    @Test
    public void mapping7() {
        Record record = new Record().set("id", "1,2").set("age", "abc");
        User user = Pojos.mapping(record, User.class);
        assertThat(user.getId()).isEqualTo(0L);
        assertThat(user.getAge()).isEqualTo(0);
    }

    @Test
    public void copy1() {
        ChildUser source = new ChildUser();
        source.setName("5");

        User destObj = new User();
        Pojos.copy(source, destObj,false);
        assertThat(destObj.getName()).isEqualTo("5");
        assertThat(destObj.getId()).isNull();
    }

    @Test
    public void copy2() {
        ChildUser source = new ChildUser();
        source.setName("5");

        User destObj = new User().setName("4").setAge(3);
        Pojos.copy(source, destObj,false);
        assertThat(destObj.getName()).isEqualTo("4");
        assertThat(destObj.getId()).isNull();
        assertThat(destObj.getAge()).isEqualTo(3);
    }

    @Test
    public void copy3() {
        ChildUser source = new ChildUser();
        source.setName("5");

        User destObj = new User().setName("4").setAge(3);
        Pojos.copy(source, destObj, true);
        assertThat(destObj.getName()).isEqualTo("5");
        assertThat(destObj.getId()).isNull();
        assertThat(destObj.getAge()).isEqualTo(3);
    }

    @Test
    public void copy4() {
        ChildUser source = new ChildUser();

        User destObj = new User().setName("4").setAge(3);
        Pojos.copy(source, destObj, true);
        assertThat(destObj.getName()).isEqualTo("4");
        assertThat(destObj.getId()).isNull();
        assertThat(destObj.getAge()).isEqualTo(3);
    }

    @Getter
    @Setter
    @Accessors(chain = true)
    public static class User {
        private Long id;
        private String name;
        private Integer age;

    }

    @Getter
    @Setter
    @Accessors(chain = true)
    public static class ChildUser extends User {
        private String blogType;
        private String log_type;

    }

    @Getter
    @Setter
    @Accessors(chain = true)
    @ToString
    public static class Material {
        private Integer aPass;
        private Integer nameType;
        private String aPassPassword;
    }

}
