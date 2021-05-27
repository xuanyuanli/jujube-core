package org.jujubeframework.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.*;

/**
 * 数据生成者
 *
 * @author John Li
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DataGenerator {
    /**
     * 填充一个对象（一般用于测试）
     */
    public static <T> T fullObject(Class<T> cl) {
        if (cl == null) {
            throw new IllegalArgumentException("参数不能为null");
        }
        if (cl.isInstance(Map.class)) {
            return (T) fullMap();
        }
        T t = Beans.getInstance(cl);
        Method[] methods = cl.getMethods();
        for (Method method : methods) {
            // 如果是set方法,进行随机数据的填充
            if (method.getName().indexOf("set") == 0) {
                Class<?> paramClass = method.getParameterTypes()[0];
                try {
                    method.invoke(t, generateRandomValueByParamType(paramClass));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return t;
    }

    /** 根据类型生成对应类型的随机数据 */
    public static <T> T generateRandomValueByParamType(Class<T> paramClass) {
        Object result = null;
        if (paramClass.equals(String.class)) {
            int i = Randoms.randomInt(1, 16);
            result = i <= 8 ? Randoms.randomChinese(i) : Randoms.randomCodes(i);
        } else if (paramClass.equals(Short.class) || paramClass.equals(Short.TYPE)) {
            result = (short) new Random().nextInt(5);
        } else if (paramClass.equals(Boolean.class) || paramClass.equals(Boolean.TYPE)) {
            result = Randoms.randomInt(0, 1) > 0;
        } else if (paramClass.equals(Double.class) || paramClass.equals(Double.TYPE)) {
            int i = new Random().nextInt(6);
            result = Calcs.mul(new Random().nextDouble(), Math.pow(10, i), 2).doubleValue();
        } else if (paramClass.equals(Float.class) || paramClass.equals(Float.TYPE)) {
            int i = new Random().nextInt(3);
            result = Calcs.mul(new Random().nextFloat(), Math.pow(10, i), 2).floatValue();
        } else if (paramClass.equals(Long.class) || paramClass.equals(Long.TYPE)) {
            result = (long) new Random().nextInt(99999999);
        } else if (paramClass.equals(Integer.class) || paramClass.equals(Integer.TYPE)) {
            result = new Random().nextInt(999);
        } else if (paramClass.equals(Date.class)) {
            result = new Date();
        } else if (paramClass.equals(Timestamp.class)) {
            result = new Timestamp(System.currentTimeMillis());
        } else if (paramClass.equals(java.sql.Date.class)) {
            result = new java.sql.Date(System.currentTimeMillis());
        }
        return (T) result;
    }

    /** 根据类型生成对应类型的默认数据 */
    public static <T> T generateDefaultValueByParamType(Class<T> paramClass) {
        Object result = null;
        if (paramClass.equals(String.class)) {
            result = "";
        } else if (paramClass.equals(Short.class) || paramClass.equals(Short.TYPE)) {
            result = (short) 0;
        } else if (paramClass.equals(Boolean.class) || paramClass.equals(Boolean.TYPE)) {
            result = false;
        } else if (paramClass.equals(Double.class) || paramClass.equals(Double.TYPE)) {
            result = 0.0d;
        } else if (paramClass.equals(Float.class) || paramClass.equals(Float.TYPE)) {
            result = 0.0f;
        } else if (paramClass.equals(Long.class) || paramClass.equals(Long.TYPE)) {
            result = 0L;
        } else if (paramClass.equals(Integer.class) || paramClass.equals(Integer.TYPE)) {
            result = 0;
        }
        return (T) result;
    }

    /** 根据类型生成对应类型的数据 */
    public static <T> T generateValueByParamType(Class<T> paramClass, GenerateType type) {
        T result = null;
        if (type.equals(GenerateType.RANDOM)) {
            result = generateRandomValueByParamType(paramClass);
        } else if (type.equals(GenerateType.DEFAULT)) {
            result = generateDefaultValueByParamType(paramClass);
        }
        return result;
    }

    /**
     * 填充一个Map（一般用于测试）
     */
    public static Map<String, Object> fullMap() {
        Map<String, Object> map = new HashMap<>(8);
        map.put("string", generateRandomValueByParamType(String.class));
        map.put("short", generateRandomValueByParamType(short.class));
        map.put("float", generateRandomValueByParamType(float.class));
        map.put("double", generateRandomValueByParamType(double.class));
        map.put("int", generateRandomValueByParamType(int.class));
        map.put("long", generateRandomValueByParamType(long.class));
        map.put("date", generateRandomValueByParamType(Date.class));
        map.put("array", Arrays.asList(1, 2, 3, 4, 5));
        return map;
    }

    /** 填充一个对应类型的List */
    public static <T> List<T> fullListBean(Class<T> cl, int size) {
        List<T> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            list.add(fullObject(cl));
        }
        return list;
    }

    /** 生成数据方式 */
    public enum GenerateType {
        /** 随机值 */
        RANDOM,
        /** 默认值 */
        DEFAULT
    }
}
