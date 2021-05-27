package org.jujubeframework.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.beanutils.BeanComparator;
import org.apache.commons.collections.ComparatorUtils;
import org.apache.commons.collections.comparators.ComparableComparator;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * 集合工具类。区别于jdk的Collections和guava的Collections2
 *
 * @author John Li
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Collections3 {

    /**
     * 提取集合中的对象的一个属性(通过Getter函数), 组合成List&lt;String&gt;. <br>
     * 不同于Collections3，这里返回String集合
     *
     * @param collection
     *            来源集合.
     * @param propertyName
     *            要提取的属性名.
     */
    public static List<String> extractToListString(final Collection<?> collection, final String propertyName) {
        return collection.stream().map(obj -> Beans.getPropertyAsString(obj, propertyName)).collect(Collectors.toList());
    }

    /**
     * 依据某个字段对集合进行排序,获取字段值的时候采用getProperty方式
     *
     * @param list
     *            待排序的集合
     * @param fieldName
     *            依据这个字段进行排序
     * @param asc
     *            如果为true，是正序；为false，为倒序
     */
    public static void sort(List list, String fieldName, boolean asc) {
        if (list == null || list.size() == 0) {
            return;
        }
        if (StringUtils.isBlank(fieldName)) {
            throw new IllegalArgumentException("fieldName不能为空");
        }
        Comparator<?> mycmp = ComparableComparator.getInstance();
        // 允许null
        mycmp = ComparatorUtils.nullLowComparator(mycmp);
        if (!asc) {
            // 逆序
            mycmp = ComparatorUtils.reversedComparator(mycmp);
        }
        list.sort(new BeanComparator(fieldName, mycmp));
    }

    /**
     * 根据条件，从集合中取出一个
     *
     * @param coll
     *            集合
     * @param fieldName
     *            字段名
     * @param value
     *            字段值
     */
    public static <T> T getOne(Collection<T> coll, String fieldName, Object value) {
        Validate.notNull(coll);
        Optional<T> first = coll.stream().filter(t -> value.equals(Beans.getProperty(t, fieldName))).findFirst();
        return first.orElse(null);
    }

    /**
     * 根据条件，从集合中取出符合条件的部分
     *
     * @param coll
     *            集合
     * @param fieldName
     *            字段名
     * @param value
     *            字段值
     * @author John Li Email：jujubeframework@163.com
     */
    public static <T> List<T> getPart(Collection<T> coll, String fieldName, Object value) {
        Validate.notNull(coll);
        return coll.stream().filter(t -> value.equals(Beans.getProperty(t, fieldName))).collect(Collectors.toList());
    }

    /**
     * 字符串数组去重
     */
    public static String[] toDiffArray(String[] s) {
        Set<String> set = new LinkedHashSet<>();
        Collections.addAll(set, s);
        return set.toArray(new String[] {});
    }

    /**
     * 对数组进行trim，摒弃数组中的空值
     */
    public static String[] trim(String[] params) {
        return Arrays.stream(params).filter(StringUtils::isNotBlank).toArray(String[]::new);
    }

    /**
     * 提取集合中的对象的两个属性(通过Getter函数), 组合成Map.
     *
     * @param collection
     *            来源集合.
     * @param keyPropertyName
     *            要提取为Map中的Key值的属性名.
     * @param valuePropertyName
     *            要提取为Map中的Value值的属性名.
     */
    public static <T> Map<Object, Object> extractToMap(final Collection<T> collection, final String keyPropertyName, final String valuePropertyName) {
        Map<Object, Object> map = new HashMap<>(collection.size());
        try {
            for (Object obj : collection) {
                map.put(Beans.getProperty(obj, keyPropertyName), Beans.getProperty(obj, valuePropertyName));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return map;
    }

    /**
     * 提取集合中的对象的一个属性(通过Getter函数), 组合成List.
     *
     * @param collection
     *            来源集合.
     * @param propertyName
     *            要提取的属性名.
     */
    public static <T> List<T> extractToList(final Collection collection, final String propertyName) {
        return extractToList(collection, propertyName, null);
    }

    /**
     * 提取集合中的对象的一个属性(通过Getter函数), 组合成List.
     *
     * @param collection
     *            来源集合.
     * @param propertyName
     *            要提取的属性名.
     */
    public static <T> List<T> extractToList(final Collection collection, final String propertyName, Class<T> expectType) {
        if (collection == null) {
            return null;
        }
        List list = new ArrayList(collection.size());
        try {
            for (Object obj : collection) {
                Object property = Beans.getProperty(obj, propertyName);
                if (expectType != null) {
                    property = Beans.getExpectTypeValue(property, expectType);
                }
                list.add(property);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return list;
    }

    /**
     * 提取集合中的对象的一个属性(通过Getter函数), 组合成由分割符分隔的字符串.
     *
     * @param collection
     *            来源集合.
     * @param propertyName
     *            要提取的属性名.
     * @param separator
     *            分隔符.
     */
    public static String extractToString(final Collection collection, final String propertyName, final String separator) {
        List list = extractToList(collection, propertyName);
        return StringUtils.join(list, separator);
    }

    /**
     * 转换Collection所有元素(通过toString())为String, 中间以 separator分隔。
     */
    public static String convertToString(final Collection collection, final String separator) {
        return StringUtils.join(collection, separator);
    }

    /**
     * 转换Collection所有元素(通过toString())为String,
     * 每个元素的前面加入prefix，后面加入postfix，如<div>mymessage</div>。
     */
    public static String convertToString(final Collection collection, final String prefix, final String postfix) {
        StringBuilder builder = new StringBuilder();
        for (Object o : collection) {
            builder.append(prefix).append(o).append(postfix);
        }
        return builder.toString();
    }

    /**
     * 判断集合是否为空.
     */
    public static boolean isEmpty(Collection collection) {
        return (collection == null) || collection.isEmpty();
    }

    /**
     * 判断Map是否为空.
     */
    public static boolean isEmpty(Map map) {
        return (map == null) || map.isEmpty();
    }

    /**
     * 判断集合是否为非空.
     */
    public static boolean isNotEmpty(Collection collection) {
        return (collection != null) && !(collection.isEmpty());
    }

    /**
     * 返回a+b的新List.
     */
    public static <T> List<T> union(final Collection<T> a, final Collection<T> b) {
        List<T> result = new ArrayList<>(a);
        result.addAll(b);
        return result;
    }

    /**
     * 返回a-b(集合a中有，而b中没有)的新List.
     */
    public static <T> List<T> subtract(final Collection<T> a, final Collection<T> b) {
        return a.stream().filter(t -> !b.contains(t)).collect(Collectors.toList());
    }

    /**
     * 返回a与b的交集的新List.
     */
    public static <T> List<T> intersection(Collection<T> a, Collection<T> b) {
        List<T> list = new ArrayList<>();

        for (T element : a) {
            if (b.contains(element)) {
                list.add(element);
            }
        }
        return list;
    }

    public static <T> List<T> enumerationToList(Enumeration<T> enumeration) {
        List<T> list = new ArrayList<>();
        while (enumeration.hasMoreElements()) {
            list.add(enumeration.nextElement());
        }
        return list;
    }

    /**
     * 对Map键值进行反转
     */
    public static Map<?, ?> reversalMap(Map<?, ?> map) {
        Map<Object, Object> result = new HashMap<>(map.size());
        for (Object key : map.keySet()) {
            result.put(map.get(key), key);
        }
        return result;
    }

    /**
     * 根据key排序map
     */
    public static <K, V> Map<K, V> sortMapByKey(Map<K, V> map, Comparator<K> comparator) {
        Map<K, V> result = new LinkedHashMap<>();
        List<Map.Entry<K, V>> entryList = new ArrayList<>(map.entrySet());
        entryList.sort((o1, o2) -> comparator.compare(o1.getKey(), o2.getKey()));
        for (Entry<K, V> entry : entryList) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    /**
     * 根据value排序map
     */
    public static <K, V> Map<K, V> sortMapByValue(Map<K, V> map, Comparator<V> comparator) {
        Map<K, V> result = new LinkedHashMap<>();
        List<Map.Entry<K, V>> entryList = new ArrayList<>(map.entrySet());
        entryList.sort((o1, o2) -> comparator.compare(o1.getValue(), o2.getValue()));
        for (Entry<K, V> entry : entryList) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    /**
     * 是否存在集合中的字段值为 detectVal(只用于判断基本类型)
     */
    public static boolean containsFieldValue(Collection<?> source, String fieldName, Object detectVal) {
        if (source == null) {
            return false;
        }
        return source.stream().anyMatch(t -> detectVal.equals(Beans.getProperty(t, fieldName)));
    }

    /** 根据某个字段去重 */
    public static <T> List<T> deWeight(List<T> list, Function<T, ?> function) {
        Set<Object> uids = new HashSet<>();
        return list.stream().filter(u -> {
            if (uids.contains(function.apply(u))) {
                return false;
            }
            uids.add(function.apply(u));
            return true;
        }).collect(Collectors.toList());
    }

    /**
     * 分组某个集合中的字段值
     */
    public static <T> Set<String> groupBy(Collection<T> data, String fieldName) {
        Set<String> set = new LinkedHashSet<>();
        for (T t : data) {
            set.add(Beans.getPropertyAsString(t, fieldName));
        }
        return set;
    }

    public static <V> Map<String, V> newHashMap(Object... keyValue) {
        Map<String, V> map = new HashMap<>(16);
        if (keyValue == null || keyValue.length == 0) {
            return map;
        }
        if (keyValue.length % 2 != 0) {
            throw new IllegalArgumentException();
        }
        for (int i = 0; i < keyValue.length; i += 2) {
            map.put(String.valueOf(keyValue[i]), (V) keyValue[i + 1]);
        }
        return map;
    }


    /**
     * 迭代器转换为List
     */
    public static <T> List<T> getListFromIterator(Iterator<T> iterator) {
        // Convert iterator to iterable
        Iterable<T> iterable = () -> iterator;
        // Create a List from the Iterable
        // Return the List
        return StreamSupport.stream(iterable.spliterator(), false).collect(Collectors.toList());
    }
}
