package org.jujubeframework.util;

import com.google.common.collect.Maps;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.ConvertUtilsBean;
import org.apache.commons.beanutils.Converter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.asm.Type;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.util.ClassUtils;
import sun.reflect.MethodAccessor;
import sun.reflect.ReflectionFactory;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 关于类操作的，都在这里<br>
 * 用到工具栏BeanUtils，把抛出的异常屏蔽了<br>
 * 其他类操作工具类，参考：FieldUtils、MethodUtils等。如果不能满足需求，可以自己实现
 * <br>
 * 补充：Java反射的性能比直接调用在JDK8中慢了40倍，这里做了性能方面的大量优化
 *
 * @author John Li Email：jujubeframework@163.com
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Beans {

    private static final Logger logger = LoggerFactory.getLogger(Beans.class);

    /**
     * PropertyDescriptor的缓存。key为classname+fieldName
     */
    private static final ConcurrentMap<String, AtomicReference<PropertyDescriptor>> PROPERTY_DESCRIPTOR_CACHE = new ConcurrentHashMap<>();

    private static final ConcurrentMap<String, AtomicReference<Method>> METHOD_CACHE = new ConcurrentHashMap<>();

    private static final ConcurrentMap<String, MethodAccessor> METHOD_ACCESSOR_CACHE = new ConcurrentHashMap<>();

    private static final ConcurrentMap<String, Object> DEFAULT_METHOD_PROXY_CACHE = new ConcurrentHashMap<>();

    private static final ConcurrentMap<String, MethodHandle> DEFAULT_METHOD_HANDLE_CACHE = new ConcurrentHashMap<>();

    private static final ConcurrentMap<String, BeanInfo> BEANINFO_CACHE = new ConcurrentHashMap<>();

    private static final ConcurrentMap<String, List<String>> FIELDNAMES_CACHE = new ConcurrentHashMap<>();

    private static final ConcurrentMap<String, Class<?>> CLASSGENERICTYPE_CACHE = new ConcurrentHashMap<>();

    private static final ConcurrentMap<String, AtomicReference<Field>> FIELD_CACHE = new ConcurrentHashMap<>();

    /**
     * ParameterNameDiscoverer的对象
     */
    private final static ParameterNameDiscoverer DISCOVERER = new DefaultParameterNameDiscoverer();

    private static final ReflectionFactory REFLECTION_FACTORY = ReflectionFactory.getReflectionFactory();

    /**
     * 基本类型封装类列表
     */
    private final static List<Class<?>> BASIC_TYPE = Dynamics.listOf(Double.class, String.class, Float.class, Byte.class, Integer.class, Character.class, Long.class, Short.class);

    /**
     * 把对象转换为map
     */
    public static Map<String, Object> beanToMap(Object obj) {
        return beanToMap(obj, false);
    }

    /**
     * 把对象转换为map（Cglib的BeanMap性能最高）
     */
    public static Map<String, Object> beanToMap(Object obj, boolean filterNull) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof Map) {
            return (Map<String, Object>) obj;
        }
        org.springframework.cglib.beans.BeanMap beanMap = org.springframework.cglib.beans.BeanMap.create(obj);
        HashMap hashMap = new HashMap<>(beanMap.size());
        for (Object key : beanMap.keySet()) {
            Object value = beanMap.get(key);
            if (filterNull) {
                if (value != null) {
                    hashMap.put(key, value);
                }
            } else {
                hashMap.put(key, value);
            }
        }
        return hashMap;
    }

    /**
     * 根据类获得实例
     */
    public static <T> T getInstance(Class<T> cl) {
        try {
            return cl.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 根据Class的完整限定名装配Class
     */
    public static <T> void forName(String className) {
        try {
            Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 自己实现的set方法(解决链式调用后setProperty不管用的情况)
     */
    public static void setProperty(Object bean, String name, Object value) {
        PropertyDescriptor descriptor = getPropertyDescriptor(bean.getClass(), name);
        if (descriptor == null) {
            throw new RuntimeException(Texts.format("类中[{}]没有找到此属性[{}]", bean.getClass(), name));
        }
        Class<?> type = descriptor.getPropertyType();
        Method writeMethod = descriptor.getWriteMethod();
        if (writeMethod != null) {
            invoke(writeMethod, bean, getExpectTypeValue(value, type));
        }
    }

    /**
     * 值类型转换，copy自BeanUtils.convert
     */
    private static <T> T convert(final Object value, final Class<T> type) {
        T newValue = null;
        ConvertUtilsBean convertUtilsBean = BeanUtilsBean.getInstance().getConvertUtils();
        if (value instanceof String) {
            newValue = (T) convertUtilsBean.convert((String) value, type);
        } else {
            final Converter converter = convertUtilsBean.lookup(type);
            if (converter != null) {
                newValue = converter.convert(type, value);
            } else if (type.isAssignableFrom(value.getClass())) {
                newValue = (T) value;
            }
        }
        return newValue;
    }

    /**
     * 自己实现的getter方法(解决字段第二个字母为大写的情况)
     */
    public static Object getProperty(Object bean, String name) {
        if (Map.class.isAssignableFrom(bean.getClass())) {
            return ((Map) bean).get(name);
        }
        PropertyDescriptor propertyDescriptor = Beans.getPropertyDescriptor(bean.getClass(), name);
        if (propertyDescriptor != null && propertyDescriptor.getReadMethod() != null) {
            return invoke(propertyDescriptor.getReadMethod(), bean);
        }
        return null;
    }

    /**
     * 通过getter方法来获取转换为String后的指
     */
    public static String getPropertyAsString(Object bean, String name) {
        return convert(getProperty(bean, name), String.class);
    }

    /**
     * 获得所有的public方法
     */
    public static Method getMethod(Class<?> cl, String methodName, Class<?>... parameterTypes) {
        String key = cl.getName() + "." + methodName + "(" + StringUtils.join(parameterTypes, ",") + ")";
        AtomicReference<Method> reference = METHOD_CACHE.get(key);
        if (reference == null) {
            Method method = null;
            try {
                method = cl.getMethod(methodName, parameterTypes);
            } catch (Exception ignored) {
            }
            reference = new AtomicReference<>(method);
            METHOD_CACHE.put(key, reference);
        }
        return reference.get();
    }

    /**
     * 获得类的所有声明方法，包括父类中的
     */
    public static Method getDeclaredMethod(Class<?> cl, String methodName, Class<?>... parameterTypes) {
        String key = cl.getName() + "." + methodName + "(" + StringUtils.join(parameterTypes, ",") + ")";
        AtomicReference<Method> reference = METHOD_CACHE.get(key);
        if (reference == null) {
            Method method = null;
            try {
                method = getSelfDeclaredMethod(cl, methodName, parameterTypes);
                for (; cl != Object.class && method == null; cl = cl.getSuperclass()) {
                    method = getSelfDeclaredMethod(cl, methodName, parameterTypes);
                }
                return method;
            } catch (Exception ignored) {
            }
            reference = new AtomicReference<>(method);
            METHOD_CACHE.put(key, reference);
        }
        return reference.get();
    }

    /**
     * 获得类的所有声明方法，不包括父类中的
     */
    public static Method getSelfDeclaredMethod(Class<?> cl, String methodName, Class<?>... parameterTypes) {
        String key = cl.getName() + "." + methodName + "(" + StringUtils.join(parameterTypes, ",") + ")";
        AtomicReference<Method> reference = METHOD_CACHE.get(key);
        if (reference == null) {
            Method method = null;
            try {
                method = cl.getDeclaredMethod(methodName, parameterTypes);
            } catch (Exception ignored) {
            }
            reference = new AtomicReference<>(method);
            METHOD_CACHE.put(key, reference);
        }
        return reference.get();
    }

    /**
     * 反射调用方法
     */
    public static Object invoke(Method method, Object obj, Object... args) {
        try {
            return getMethodAccessor(method).invoke(obj, args);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获得方法访问器
     */
    private static MethodAccessor getMethodAccessor(Method method) {
        String key = method.toString();
        MethodAccessor accessor = METHOD_ACCESSOR_CACHE.get(key);
        if (accessor == null) {
            accessor = REFLECTION_FACTORY.newMethodAccessor(method);
            METHOD_ACCESSOR_CACHE.put(key, accessor);
        }
        return accessor;
    }

    /**
     * 反射调用default方法
     *
     * @param method 方法
     * @param args   方法参数
     */
    public static Object invokeDefaultMethod(Method method, Object... args) {
        try {
            return invokeDefaultMethod(getDefaultMethodProxy(method), method, args);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获得默认方法调用对象
     */
    private static Object getDefaultMethodProxy(Method method) {
        String key = method.toString();
        Object obj = DEFAULT_METHOD_PROXY_CACHE.get(key);
        if (obj == null) {
            Class<?>[] classes = {method.getDeclaringClass()};
            obj = Proxy.newProxyInstance(method.getDeclaringClass().getClassLoader(), classes, new InterfaceDefaultHandler());
            DEFAULT_METHOD_PROXY_CACHE.put(key, obj);
        }
        return obj;
    }

    /**
     * 反射调用default方法
     *
     * @param proxy  一般为接口子对象
     * @param method 方法
     * @param args   方法参数
     */
    public static Object invokeDefaultMethod(Object proxy, Method method, Object... args) throws Throwable {
        return getDefaultMethodHandle(method).bindTo(proxy).invokeWithArguments(args);
    }

    /**
     * 获得默认方法调用Handle
     */
    private static MethodHandle getDefaultMethodHandle(Method method) throws Throwable {
        String key = method.toString();
        MethodHandle methodHandle = DEFAULT_METHOD_HANDLE_CACHE.get(key);
        if (methodHandle == null) {
            final Constructor<MethodHandles.Lookup> constructor = MethodHandles.Lookup.class.getDeclaredConstructor(Class.class, int.class);
            if (!constructor.isAccessible()) {
                constructor.setAccessible(true);
            }
            final Class<?> declaringClass = method.getDeclaringClass();
            methodHandle = constructor.newInstance(declaringClass, MethodHandles.Lookup.PRIVATE).unreflectSpecial(method, declaringClass);
            DEFAULT_METHOD_HANDLE_CACHE.put(key, methodHandle);
        }
        return methodHandle;
    }

    /**
     * 获得类的所有声明字段，包括父类中的
     */
    public static Field getDeclaredField(Class<?> cl, String fieldName) {
        String key = cl.getName() + "." + fieldName;
        AtomicReference<Field> reference = FIELD_CACHE.get(key);
        if (reference == null) {
            Field field = null;
            try {
                field = getSelfDeclaredField(cl, fieldName);
                for (; cl != Object.class && field == null; cl = cl.getSuperclass()) {
                    field = getSelfDeclaredField(cl, fieldName);
                }
            } catch (Exception ignored) {
            }
            reference = new AtomicReference<>(field);
            FIELD_CACHE.put(key, reference);
        }
        return reference.get();
    }

    /**
     * 获得类的所有声明字段，不包括父类中的
     */
    public static Field getSelfDeclaredField(Class<?> cl, String fieldName) {
        String key = cl.getName() + "#" + fieldName;
        AtomicReference<Field> reference = FIELD_CACHE.get(key);
        if (reference == null) {
            Field field = null;
            try {
                field = cl.getDeclaredField(fieldName);
            } catch (Exception ignored) {
            }
            reference = new AtomicReference<>(field);
            FIELD_CACHE.put(key, reference);
        }
        return reference.get();
    }

    /**
     * 根据Class获得类信息
     */
    private static BeanInfo getBeanInfo(Class<?> targetClass) {
        String key = targetClass.getName();
        BeanInfo beanInfo = BEANINFO_CACHE.get(key);
        if (beanInfo == null) {
            try {
                beanInfo = Introspector.getBeanInfo(targetClass);
            } catch (final IntrospectionException e) {
                throw new RuntimeException(e);
            }
            BEANINFO_CACHE.put(key, beanInfo);
        }
        return beanInfo;
    }

    /**
     * 获得类的某个字段属性描述
     */
    public static PropertyDescriptor getPropertyDescriptor(Class<?> targetClass, String fieldName) {
        String key = targetClass.getName() + "#" + fieldName;
        AtomicReference<PropertyDescriptor> reference = PROPERTY_DESCRIPTOR_CACHE.get(key);
        if (reference == null) {
            reference = new AtomicReference<>();
            PropertyDescriptor descriptor;
            BeanInfo beanInfo = getBeanInfo(targetClass);
            descriptor = getPropertyDescriptorFromBeanInfo(beanInfo, fieldName);
            // 解决第二个字母为大写的情况（第二个字母为大写的话，propertyDescriptor有时会出现前两个字母都为大写的情况）
            if (descriptor == null && fieldName.length() >= 2 && Character.isUpperCase(fieldName.charAt(1))) {
                descriptor = getPropertyDescriptorFromBeanInfo(beanInfo, Texts.capitalize(fieldName));
            }
            if (descriptor != null) {
                // 如果用lombok的@Accessors(chain=true)注解的话(链式操作)，writeMethod会为空
                if (descriptor.getWriteMethod() == null) {
                    String methodName = "set" + StringUtils.capitalize(fieldName);
                    Method writeMethod = getDeclaredMethod(targetClass, methodName, descriptor.getPropertyType());
                    try {
                        descriptor.setWriteMethod(writeMethod);
                    } catch (IntrospectionException e) {
                        throw new RuntimeException(e);
                    }
                }
                reference.set(descriptor);
            }
            PROPERTY_DESCRIPTOR_CACHE.putIfAbsent(key, reference);
        }
        return reference.get();
    }

    /**
     * 从BeanInfo中获取字段属性描述器
     */
    private static PropertyDescriptor getPropertyDescriptorFromBeanInfo(BeanInfo beanInfo, String fieldName) {
        for (PropertyDescriptor propertyDescriptor : beanInfo.getPropertyDescriptors()) {
            if (propertyDescriptor.getName().equals(fieldName)) {
                return propertyDescriptor;
            }
        }
        return null;
    }

    /**
     * 获得形参名和形参值的简单对照表（name-value）
     *
     * @param method 方法
     * @param args   实参集合(可为空，MethodParam的value也为空)
     */
    public static Map<String, Object> getFormalParamSimpleMapping(Method method, Object... args) {
        Map<String, Object> result = Maps.newHashMap();
        String[] names = getMethodParamNames(method);
        if (names == null || names.length == 0) {
            return result;
        }
        Class<?>[] types = method.getParameterTypes();
        boolean existValue = args != null && args.length > 0;
        for (int i = 0; i < types.length; i++) {
            String fname = names[i];
            Object value = null;
            if (existValue) {
                value = args[i];
            }
            result.put(fname, value);
        }
        return result;
    }

    /**
     * 比较参数类型是否一致
     *
     * @param types   asm的类型({@link Type})
     * @param clazzes java 类型({@link Class})
     */
    private static boolean sameType(Type[] types, Class<?>[] clazzes) {
        // 个数不同
        if (types.length != clazzes.length) {
            return false;
        }

        for (int i = 0; i < types.length; i++) {
            if (!Type.getType(clazzes[i]).equals(types[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * 获取方法的形参名集合
     */
    public static String[] getMethodParamNames(final Method method) {
        return DISCOVERER.getParameterNames(method);
    }

    /**
     * 获得所有可访问的字段名（包括父类）集合
     */
    public static List<String> getAllDeclaredFieldNames(Class<?> clazz) {
        String key = clazz.getName();
        List<String> fields = FIELDNAMES_CACHE.get(key);
        if (fields == null) {
            BeanInfo beanInfo = getBeanInfo(clazz);
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
            fields = new ArrayList<>(propertyDescriptors.length);
            for (PropertyDescriptor descriptor : propertyDescriptors) {
                String fieldName = descriptor.getName();
                // 去除class字段
                if (!"class".equals(fieldName)) {
                    fields.add(fieldName);
                }
            }
            FIELDNAMES_CACHE.put(key, fields);
        }
        return fields;
    }

    /**
     * 对比两个对象，获取差异字段集合
     *
     * @param oldObject 旧对象
     * @param newObject 新对象
     */
    public static List<FieldDidderence> contrastObject(Object oldObject, Object newObject) {
        List<FieldDidderence> result = new ArrayList<>();
        Field[] noFields = newObject.getClass().getDeclaredFields();
        for (Field noField : noFields) {
            String fieldName = noField.getName();
            Object noValue = getProperty(newObject, fieldName);
            // 如果字段不为空，则表示该字段修改
            if (noValue != null) {
                Object oldValue = getProperty(oldObject, fieldName);
                if (!(noValue.equals(oldValue))) {
                    FieldDidderence didderence = new FieldDidderence();
                    didderence.setFiledName(fieldName);
                    didderence.setNewValue(noValue.toString());
                    if (oldValue != null) {
                        didderence.setOldValue(oldValue.toString());
                    } else {
                        didderence.setOldValue("");
                    }
                    result.add(didderence);
                }
            }
        }
        return result;
    }

    /**
     * 通过反射, 获得Class定义中声明的泛型参数的类型。如无法找到, 返回Object.class.
     *
     * @param clazz The class to introspect
     * @return the first generic declaration, or Object.class if cannot be
     * determined
     */
    public static <T> Class<T> getClassGenericType(final Class<?> clazz) {
        return (Class<T>) getClassGenericType(clazz, 0);
    }

    /**
     * 通过反射, 获得Class定义中声明的父类(或接口,如果是接口的话，默认获得第一个泛型接口)的泛型参数的类型。如无法找到, 返回Object.class.
     *
     * @param clazz clazz The class to introspect
     * @param index the Index of the generic ddeclaration,start from 0.
     * @return the index generic declaration, or Object.class if cannot be
     * determined
     */
    public static Class<?> getClassGenericType(final Class<?> clazz, final int index) {
        String key = clazz.getName() + index;
        Class<?> cl = CLASSGENERICTYPE_CACHE.get(key);
        if (cl == null) {
            java.lang.reflect.Type genType = clazz.getGenericSuperclass();
            java.lang.reflect.Type[] genericInterfaces = clazz.getGenericInterfaces();
            if (genType == null && genericInterfaces != null && genericInterfaces.length > 0) {
                genType = genericInterfaces[0];
            }
            if (!(genType instanceof ParameterizedType)) {
                logger.warn(clazz.getSimpleName() + "'s superclass not ParameterizedType");
                cl = Object.class;
            } else {
                java.lang.reflect.Type[] params = ((ParameterizedType) genType).getActualTypeArguments();
                if ((index >= params.length) || (index < 0)) {
                    logger.warn("Index: " + index + ", Size of " + clazz.getSimpleName() + "'s Parameterized Type: " + params.length);
                    cl = Object.class;
                } else if (!(params[index] instanceof Class)) {
                    logger.warn(clazz.getSimpleName() + " not set the actual class on superclass generic parameter");
                    cl = Object.class;
                } else {
                    cl = (Class<?>) params[index];
                }
            }
            CLASSGENERICTYPE_CACHE.put(key, cl);
        }
        return cl;
    }

    /**
     * 获得当前项目（jar）的ClassLoader
     */
    public static ClassLoader getDefaultClassLoader() {
        return ClassUtils.getDefaultClassLoader();
    }

    /**
     * 是否是基本数据类型
     */
    public static boolean isBasicType(Class<?> cl) {
        return cl.isPrimitive() || BASIC_TYPE.contains(cl);
    }

    /**
     * 从方法实参中获得对应类型的对象
     */
    public static <T> T getObjcetFromMethodArgs(Object[] methodArgs, Class<T> clazz) {
        return (T) Arrays.stream(methodArgs).filter(o -> clazz.isAssignableFrom(o.getClass())).findFirst().orElse(null);
    }

    /**
     * 获得预期类型的值
     */
    public static <T> T getExpectTypeValue(Object o, Class<T> returnType) {
        if (o == null) {
            return null;
        }
        if (returnType.equals(o.getClass())) {
            return (T) o;
        } else {
            return convert(o, returnType);
        }
    }

    /**
     * 字段差异
     */
    public static class FieldDidderence {
        /**
         * 字段名称
         */
        private String filedName;
        /**
         * 字段修改前的值
         */
        private String oldValue;
        /**
         * 字段修改后的值
         */
        private String newValue;

        public String getFiledName() {
            return filedName;
        }

        public void setFiledName(String filedName) {
            this.filedName = filedName;
        }

        public String getOldValue() {
            return oldValue;
        }

        public void setOldValue(String oldValue) {
            this.oldValue = oldValue;
        }

        public String getNewValue() {
            return newValue;
        }

        public void setNewValue(String newValue) {
            this.newValue = newValue;
        }

    }

    /**
     * default方法的InvocationHandler
     */
    public static class InterfaceDefaultHandler implements InvocationHandler {
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.isDefault()) {
                Constructor<MethodHandles.Lookup> constructor = MethodHandles.Lookup.class.getDeclaredConstructor(Class.class, int.class);
                constructor.setAccessible(true);
                Class<?> declaringClass = method.getDeclaringClass();
                int allModes = MethodHandles.Lookup.PUBLIC | MethodHandles.Lookup.PRIVATE | MethodHandles.Lookup.PROTECTED | MethodHandles.Lookup.PACKAGE;
                return constructor.newInstance(declaringClass, allModes).unreflectSpecial(method, declaringClass).bindTo(proxy).invokeWithArguments(args);
            }
            throw new RuntimeException("必须是interface的default方法调用");
        }
    }
}
