package org.jujubeframework.util;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import static org.assertj.core.api.Assertions.assertThat;
import org.jujubeframework.lang.Record;
import org.junit.Test;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

public class BeansTest {
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
    public static class ChildUser extends User {
        private Long cardId;
        private Long aPass;
        private Double price;
    }

    @Test
    public final void testBeanToMap() {
        ChildUser user = (ChildUser) new ChildUser().setName("abc").setAge(12);
        Map<String, Object> map = Beans.beanToMap(user);
        assertThat(map).hasSize(8);
        assertThat(map.get("name")).isEqualTo("abc");
        assertThat(map.get("id")).isNull();
    }

    @Test
    public final void testGetInstance() {
        assertThat(Beans.getInstance(User.class)).isNotNull();
    }

    @Test
    public final void testGetPropertyDescriptor() {
        PropertyDescriptor propertyDescriptor = Beans.getPropertyDescriptor(ChildUser.class, "name");
        assertThat(propertyDescriptor.getWriteMethod()).isNotNull();
        assertThat(propertyDescriptor.getPropertyType()).isEqualTo(String.class);

        propertyDescriptor = Beans.getPropertyDescriptor(User.class, "name1");
        assertThat(propertyDescriptor).isNull();

        propertyDescriptor = Beans.getPropertyDescriptor(User.class, "aPass");
        assertThat(propertyDescriptor).isNull();
    }

    @Test
    public final void testGetAllDeclaredFields() {
        List<String> names = Beans.getAllDeclaredFieldNames(ChildUser.class);
        assertThat(names).hasSize(8).contains("age", "blogType", "cardId", "id", "log_type", "name", "price", "APass");
    }

    @Test
    public final void getMethodParamNames() {
        Method method = Beans.getMethod(User.class, "setT", String.class, int.class, double.class);
        assertThat(Beans.getMethodParamNames(method)).hasSize(3).contains("tname", "tage", "tprice");
    }

    @Test
    public final void getProperty() {
        Record record = new Record().set("id", 34L);
        assertThat(Beans.getProperty(record, "id")).isEqualTo(34L);
    }
}
