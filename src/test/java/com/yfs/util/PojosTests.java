package com.yfs.util;

import com.yfs.lang.Record;
import com.yfs.util.Pojos.FieldMapping;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.math.NumberUtils;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

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
        Record record = new Record().set("a_pass_password", "123").set("a_pass", 0);
        Material material = Pojos.mapping(record, Material.class);
        assertThat(material.getAPass()).isEqualTo(0);
        assertThat(material.getAPassPassword()).isEqualTo("123");
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
        private String aPassPassword;
    }

}
