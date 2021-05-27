package org.jujubeframework.util;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.junit.jupiter.api.Test;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

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

    public interface IUser {
        long queryAgeCount(long age, long departmentId);
    }

    @Getter
    @Setter
    @Accessors(chain = true)
    public static class User2 {

        private Long id;
        private String name;
        private Integer age;
        private String blogType;
        private String log_type;
        private Integer fInfoId;
        private ChildUser childUser;
    }

    @Getter
    @Setter
    @Accessors(chain = true)
    public static class User3 {
        private User childUser;
    }

    public interface GrClass {
        List<IUser> test();
    }

    @Test
    public final void beanToMap() {
        ChildUser user = (ChildUser) new ChildUser().setName("abc").setAge(12);
        Map<String, Object> map = Beans.beanToMap(user);
        assertThat(map).hasSize(8);
        assertThat(map.get("name")).isEqualTo("abc");
        assertThat(map.get("age")).isEqualTo(12);
        assertThat(map.get("id")).isNull();
    }

    @Test
    public final void getInstance() {
        assertThat(Beans.getInstance(User.class)).isNotNull();
    }

    @Test
    public final void getPropertyDescriptor() {
        PropertyDescriptor propertyDescriptor = Beans.getPropertyDescriptor(ChildUser.class, "name");
        assertThat(propertyDescriptor.getWriteMethod()).isNotNull();
        assertThat(propertyDescriptor.getPropertyType()).isEqualTo(String.class);

        propertyDescriptor = Beans.getPropertyDescriptor(User.class, "name1");
        assertThat(propertyDescriptor).isNull();

        propertyDescriptor = Beans.getPropertyDescriptor(User.class, "aPass");
        assertThat(propertyDescriptor).isNull();
    }

    @Test
    public final void getAllDeclaredFields() {
        List<String> names = Beans.getAllDeclaredFieldNames(ChildUser.class);
        assertThat(names).hasSize(8).contains("age", "blogType", "cardId", "id", "log_type", "name", "price", "APass");
    }

    @Test
    public final void getMethodParamNames() {
        Method method = Beans.getMethod(User.class, "setT", String.class, int.class, double.class);
        assertThat(Beans.getMethodParamNames(method)).hasSize(3).contains("tname", "tage", "tprice");

        method = Beans.getSelfDeclaredMethod(IUser.class, "queryAgeCount", long.class, long.class);
        assertThat(Beans.getMethodParamNames(method)).hasSize(2).contains("age", "departmentId");
    }

    @Test
    public final void getProperty() {
        User2 user = new User2().setFInfoId(123).setAge(2).setBlogType("t").setLog_type("y");
        assertThat(Beans.getProperty(user, "fInfoId")).isEqualTo(123);
        assertThat(Beans.getProperty(user, "age")).isEqualTo(2);
        assertThat(Beans.getProperty(user, "blogType")).isEqualTo("t");
        assertThat(Beans.getProperty(user, "log_type")).isEqualTo("y");
    }

    @Test
    public final void setProperty() {
        ChildUser childUser = new ChildUser();
        childUser.setId(12L);
        User3 user = new User3();
        Beans.setProperty(user, "childUser", childUser);
        assertThat(user.getChildUser().getId()).isEqualTo(12L);

        User2 user2 = new User2();
        Beans.setProperty(user, "childUser", new User().setId(13L));
        assertThat(user2.getChildUser()).isNull();
    }

    @Test
    public final void getFormalParamSimpleMapping() {
        Method method = Beans.getSelfDeclaredMethod(IUser.class, "queryAgeCount", long.class, long.class);
        Map<String, Object> methodParamNames = Beans.getFormalParamSimpleMapping(method, 1, 2);
        assertThat(Jsons.toJson(methodParamNames)).isEqualTo("{\"departmentId\":2,\"age\":1}");
    }

    @Test
    public final void getGenericReturnType() throws NoSuchMethodException {
        Method test = GrClass.class.getMethod("test");
        assertThat(test.getReturnType()).isEqualTo(List.class);
        ParameterizedType genericReturnType = (ParameterizedType) test.getGenericReturnType();
        assertThat(genericReturnType.getActualTypeArguments()[0]).isEqualTo(IUser.class);
    }

    @Test
    public final void getExpectTypeValue() {
        assertThat(Beans.getExpectTypeValue(90, Long.class)).isEqualTo(90L);
        assertThat(Beans.getExpectTypeValue(90, long.class)).isEqualTo(90L);
        assertThat(Beans.getExpectTypeValue(90L, Integer.class)).isEqualTo(90);
        assertThat(Beans.getExpectTypeValue(90L, int.class)).isEqualTo(90);
        assertThat(Beans.getExpectTypeValue(90, double.class)).isEqualTo(90D);
        assertThat(Beans.getExpectTypeValue(90, Double.class)).isEqualTo(90D);
        assertThat(Beans.getExpectTypeValue(90D, int.class)).isEqualTo(90);
        assertThat(Beans.getExpectTypeValue(90D, long.class)).isEqualTo(90L);
        assertThat(Beans.getExpectTypeValue(90, String.class)).isEqualTo("90");
        assertThat(Beans.getExpectTypeValue(null, String.class)).isNull();
    }

    @Test
    public void isPrimitive() {
        assertThat(int.class.isPrimitive()).isTrue();
        assertThat(double.class.isPrimitive()).isTrue();
        assertThat(Integer.class.isPrimitive()).isFalse();
        assertThat(String.class.isPrimitive()).isFalse();
    }
}
