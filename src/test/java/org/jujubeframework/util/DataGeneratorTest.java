package org.jujubeframework.util;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;

public class DataGeneratorTest {

    @Test
    public void generateRandomValueByParamType() {
        String str = DataGenerator.generateRandomValueByParamType(String.class);
        Assertions.assertThat(str.length()).isGreaterThanOrEqualTo(1).isLessThanOrEqualTo(16);

        Short aShort = DataGenerator.generateRandomValueByParamType(Short.class);
        Assertions.assertThat(aShort).isGreaterThan(Short.MIN_VALUE).isLessThanOrEqualTo(Short.MAX_VALUE);

        Boolean aBoolean = DataGenerator.generateRandomValueByParamType(Boolean.class);
        Assertions.assertThat(aBoolean).isIn(true, false);

        Double aDouble = DataGenerator.generateRandomValueByParamType(Double.class);
        Assertions.assertThat(aDouble).isGreaterThan(Double.MIN_VALUE).isLessThanOrEqualTo(Double.MAX_VALUE);

        Float aFloat = DataGenerator.generateRandomValueByParamType(Float.class);
        Assertions.assertThat(aFloat).isGreaterThan(Float.MIN_VALUE).isLessThanOrEqualTo(Float.MAX_VALUE);

        Long aLong = DataGenerator.generateRandomValueByParamType(Long.class);
        Assertions.assertThat(aLong).isGreaterThan(Long.MIN_VALUE).isLessThanOrEqualTo(Long.MAX_VALUE);

        Integer integer = DataGenerator.generateRandomValueByParamType(Integer.class);
        Assertions.assertThat(integer).isGreaterThan(Integer.MIN_VALUE).isLessThanOrEqualTo(Integer.MAX_VALUE);

        Type type = DataGenerator.generateRandomValueByParamType(Type.class);
        Assertions.assertThat(type).isNull();
    }

    @Test
    public void generateDefaultValueByParamType() {
        Assertions.assertThat(DataGenerator.generateDefaultValueByParamType(String.class)).isEqualTo("");
        Assertions.assertThat(DataGenerator.generateDefaultValueByParamType(Short.class)).isEqualTo((short) 0);
        Assertions.assertThat(DataGenerator.generateDefaultValueByParamType(short.class)).isEqualTo((short) 0);
        Assertions.assertThat(DataGenerator.generateDefaultValueByParamType(Boolean.class)).isEqualTo(false);
        Assertions.assertThat(DataGenerator.generateDefaultValueByParamType(boolean.class)).isEqualTo(false);
        Assertions.assertThat(DataGenerator.generateDefaultValueByParamType(Double.class)).isEqualTo(0.0d);
        Assertions.assertThat(DataGenerator.generateDefaultValueByParamType(double.class)).isEqualTo(0.0d);
        Assertions.assertThat(DataGenerator.generateDefaultValueByParamType(Float.class)).isEqualTo(0.0f);
        Assertions.assertThat(DataGenerator.generateDefaultValueByParamType(float.class)).isEqualTo(0.0f);
        Assertions.assertThat(DataGenerator.generateDefaultValueByParamType(Long.class)).isEqualTo(0L);
        Assertions.assertThat(DataGenerator.generateDefaultValueByParamType(long.class)).isEqualTo(0L);
        Assertions.assertThat(DataGenerator.generateDefaultValueByParamType(Integer.class)).isEqualTo(0);
        Assertions.assertThat(DataGenerator.generateDefaultValueByParamType(int.class)).isEqualTo(0);
        Assertions.assertThat(DataGenerator.generateDefaultValueByParamType(Type.class)).isNull();
    }
}