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
    @SuppressWarnings("unchecked")
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
                    if (paramClass.equals(String.class)) {
                        int i = Randoms.randomInt(1, 16);
                        method.invoke(t, i <= 8 ? Randoms.randomChinese(i) : Randoms.randomCodes(i));
                    } else if (paramClass.equals(Short.class) || paramClass.equals(Short.TYPE)) {
                        method.invoke(t, (short) new Random().nextInt(5));
                    } else if (paramClass.equals(Float.class) || paramClass.equals(Float.TYPE)) {
                        int i = new Random().nextInt(3);
                        method.invoke(t, Calcs.mul(new Random().nextFloat(), Math.pow(10, i), 2).floatValue());
                    } else if (paramClass.equals(Double.class) || paramClass.equals(Double.TYPE)) {
                        int i = new Random().nextInt(6);
                        method.invoke(t, Calcs.mul(new Random().nextDouble(), Math.pow(10, i), 2).floatValue());
                    } else if (paramClass.equals(Integer.class) || paramClass.equals(Integer.TYPE)) {
                        method.invoke(t, new Random().nextInt(999));
                    } else if (paramClass.equals(Long.class) || paramClass.equals(Long.TYPE)) {
                        method.invoke(t, (long) new Random().nextInt(99999999));
                    } else if (paramClass.equals(Date.class)) {
                        method.invoke(t, new Date());
                    } else if (paramClass.equals(Timestamp.class)) {
                        method.invoke(t, new Timestamp(System.currentTimeMillis()));
                    } else if (paramClass.equals(java.sql.Date.class)) {
                        method.invoke(t, new java.sql.Date(System.currentTimeMillis()));
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return t;
    }

    /**
     * 填充一个Map（一般用于测试）
     */
    public static Map<String, Object> fullMap() {
        Map<String, Object> map = new HashMap<String, Object>(8);
        int i = Randoms.randomInt(1, 16);
        String str = i <= 8 ? Randoms.randomChinese(i) : Randoms.randomCodes(i);
        map.put("string", str);

        short nShort = (short) new Random().nextInt(5);
        map.put("short", nShort);

        i = new Random().nextInt(3);
        float nFloat = Calcs.mul(new Random().nextFloat(), Math.pow(10, i), 2).floatValue();
        map.put("float", nFloat);

        i = new Random().nextInt(6);
        double nDouble = Calcs.mul(new Random().nextDouble(), Math.pow(10, i), 2).doubleValue();
        map.put("double", nDouble);

        i = new Random().nextInt(999);
        map.put("int", i);

        long l = (long) new Random().nextInt(99999999);
        map.put("long", l);

        map.put("date", new Date());

        map.put("array", Arrays.asList(1, 2, 3, 4, 5));
        return map;
    }

    public static <T> List<T> fullListBean(Class<T> cl, int size) {
        List<T> list = new ArrayList<T>(size);
        for (int i = 0; i < size; i++) {
            list.add(fullObject(cl));
        }
        return list;
    }

}
