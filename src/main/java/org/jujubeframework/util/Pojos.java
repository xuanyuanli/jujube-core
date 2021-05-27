package org.jujubeframework.util;

import com.google.common.collect.Lists;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jujubeframework.lang.Record;

import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 简单Java对象 转换工具类，主要用于把源对象Bean转换为Pojo
 *
 * <pre>
 *     1、针对字段名，驼峰命名和下划线命名可以完成自动转换并赋值。例如A对象到B对象，A中有user_type字段，B中有userType，可以完成user_type-&gt;userType的字段赋值;
 *     2、如果类型不一致，也会自动转换。例如int到string，string到double等
 *     3、支持父类属性的获取和赋值
 * </pre>
 *
 * @author John Li
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Pojos {

    private static final ConcurrentMap<String, FieldMapping> CLASS_MAP = new ConcurrentHashMap<>();

    /**
     * 把原始对象映射为对应类型的Pojo
     */
    public static <T> T mapping(Object sourceObj, Class<T> clazz) {
        if (clazz == null || sourceObj == null) {
            return null;
        }
        if (Record.class.isAssignableFrom(clazz) && Map.class.isAssignableFrom(sourceObj.getClass())) {
            return (T) new Record((Map<String, Object>) sourceObj);
        }
        return mapping(sourceObj, clazz, null);
    }

    /**
     * 获得缓存的FieldMapping
     */
    private static FieldMapping getCacheFieldMapping(Object sourceObj, Class<?> destClass, FieldMapping fieldMapping) {
        if (fieldMapping == null) {
            fieldMapping = new FieldMapping();
        }
        String cacheKey = sourceObj.getClass().getName() + ":" + destClass.getName();
        if (sourceObj instanceof Map) {
            List<String> fieldNames = getFieldNameList(sourceObj);
            fieldNames.addAll(fieldMapping.getFieldMapping().keySet());
            fieldNames.addAll(fieldMapping.getFieldMapping().values());
            cacheKey = fieldNames + ":" + destClass.getName();
        }
        if (CLASS_MAP.containsKey(cacheKey)) {
            return CLASS_MAP.get(cacheKey);
        } else {
            List<String> fieldNames = getFieldNameList(sourceObj);
            Map<String, String> mapping = fieldMapping.getFieldMapping();
            for (String fieldName : fieldNames) {
                if (!mapping.containsKey(fieldName)) {
                    PropertyDescriptor field = Beans.getPropertyDescriptor(destClass, fieldName);
                    if (field == null) {
                        String camelCase = CamelCase.toCamelCase(fieldName);
                        field = Beans.getPropertyDescriptor(destClass, camelCase);
                        if (field == null) {
                            field = Beans.getPropertyDescriptor(destClass, CamelCase.toUnderlineName(fieldName));
                        }
                    }
                    // 如果fieldMapping包含了映射关系，那么以他为准。所以这里加个!mapping.containsValue(field.getName())的判断
                    if (field != null && !mapping.containsValue(field.getName())) {
                        fieldMapping.field(fieldName, field.getName());
                    }
                }
            }
            CLASS_MAP.put(cacheKey, fieldMapping);
        }
        return fieldMapping;
    }

    /**
     * 获得字段名称集合
     */
    private static List<String> getFieldNameList(Object sourceObj) {
        List<String> fieldNames;
        if (sourceObj instanceof Map) {
            Map<String, ?> map = (Map<String, ?>) sourceObj;
            fieldNames = Lists.newArrayList(map.keySet());
        } else {
            fieldNames = Beans.getAllDeclaredFieldNames(sourceObj.getClass());
        }
        return fieldNames;
    }

    /**
     * 复制一个对象的值到另一个对象
     *
     * @param cover
     *            是否覆盖destObj字段的值,如果原字段已有值的话
     */
    private static <T> T copyObj(Object sourceObj, T destObj, FieldMapping fieldMapping, boolean cover) {
        Class<T> destClass = (Class<T>) destObj.getClass();
        if (Map.class.isAssignableFrom(destClass)) {
            throw new IllegalArgumentException("destClass不能为Map");
        }
        Map<String, String> mapping = getCacheFieldMapping(sourceObj, destClass, fieldMapping).getFieldMapping();
        for (Entry<String, String> entry : mapping.entrySet()) {
            String destFieldName = entry.getValue();
            Object value = Beans.getProperty(sourceObj, entry.getKey());
            if (value != null) {
                if (cover) {
                    Beans.setProperty(destObj, destFieldName, value);
                } else if (Beans.getProperty(destObj, destFieldName) == null) {
                    Beans.setProperty(destObj, destFieldName, value);
                }
            }
        }
        return destObj;
    }

    /**
     * 把原始对象映射为对应类型的Pojo
     *
     * @param fieldMapping
     *            字段映射
     */
    public static <T> T mapping(Object sourceObj, Class<T> destClass, FieldMapping fieldMapping) {
        if (destClass == null || sourceObj == null) {
            return null;
        }
        return copyObj(sourceObj, Beans.getInstance(destClass), fieldMapping, true);
    }

    /**
     * 对值进行过滤
     */
    static Object valueFilter(Object sourceValue, Class<?> destClass, String destFieldName) {
        // double转换为string时，有可能值是科学计数法。现在暂时没有这个问题，以后出现了，这里需要添加逻辑
        Class<?> sourceFieldClass = sourceValue.getClass();
        PropertyDescriptor destField = Beans.getPropertyDescriptor(destClass, destFieldName);
        Class<?> destFieldClass = destField.getPropertyType();
        boolean bool = (Double.class.equals(sourceFieldClass) || double.class.equals(sourceFieldClass) || Float.class.equals(sourceFieldClass)
                || float.class.equals(sourceFieldClass)) && String.class.equals(destFieldClass);
        if (bool) {
            Number number = (Number) sourceValue;
            return Numbers.numberToString(number.doubleValue());
        }
        return sourceValue;
    }

    /**
     * 复制一个对象的值到另一个对象
     *
     * @param cover
     *            是否覆盖destObj字段的值。<br>
     *            如果为true，则destObj中对应字段已有值且sourceObj对应字段也有值，进行覆盖；destObj中对应字段为空而sourceObj对应字段有值，则不覆盖
     */
    public static <T> void copy(Object sourceObj, Object destObj, boolean cover) {
        if (sourceObj == null || destObj == null) {
            throw new NullPointerException();
        }
        copyObj(sourceObj, destObj, null, cover);
    }

    /**
     * 字段对应类(key-value: sourceFieldName-destFieldName)
     */
    public static class FieldMapping {
        private final Map<String, String> mapping = new HashMap<>();

        public FieldMapping field(String sourceField, String destField) {
            mapping.put(sourceField, destField);
            return this;
        }

        /** 获得字段对应表(key-value: sourceFieldName-destFieldName) */
        Map<String, String> getFieldMapping() {
            return mapping;
        }

    }

    /**
     * 把原始对象集合映射为对应类型的Pojo集合
     */
    public static <T> List<T> mappingArray(List<?> source, Class<T> class1) {
        return mappingArray(source, class1, null);
    }

    /**
     * 把原始对象集合映射为对应类型的Pojo集合
     *
     * @param fieldMapping
     *            字段映射
     */
    public static <T> List<T> mappingArray(List<?> source, Class<T> class1, FieldMapping fieldMapping) {
        if (source == null) {
            return null;
        }
        List<T> list = new ArrayList<>(source.size());
        for (Object obj : source) {
            list.add(mapping(obj, class1, fieldMapping));
        }
        return list;
    }
}
