package org.jujubeframework.lang;

import org.apache.commons.lang3.math.NumberUtils;
import org.jujubeframework.util.Beans;
import org.jujubeframework.util.CamelCase;
import org.jujubeframework.util.Pojos;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.HashMap;
import java.util.Map;

import static org.jujubeframework.util.Dynamics.bool;

/**
 * 一个进阶版的Map，一般作为数据库表的一行数据存在。注意他与Map的最大区别是：如果把Bean转换为Record，则默认驼峰命名变为下划线命名方式
 *
 * @author John Li
 */
public class Record extends HashMap<String, Object> {

    private static final long serialVersionUID = -5745367570456272792L;

    public Record() {
    }

    public Record(Map<String, Object> map) {
        if (map != null) {
            putAll(map);
        }
    }

    public Record(int size) {
        super(size);
    }

    /**
     * Get column of mysql type: varchar, char, enum, set, text, tinytext,
     * mediumtext, longtext
     */
    public String getStr(String column) {
        Object val = get(column);
        if (val == null) {
            return null;
        }
        return String.valueOf(val);
    }

    public String getStr(String column, String defaultStr) {
        Object val = get(column);
        if (!bool(val)) {
            return defaultStr;
        }
        return String.valueOf(val);
    }

    /**
     * Get column of mysql type: int, integer, tinyint(n) n &gt; 1, smallint,
     * mediumint
     */
    public Integer getInt(String column) {
        return NumberUtils.toInt(String.valueOf(get(column)));
    }

    public Integer getInt(String column, int def) {
        return NumberUtils.toInt(String.valueOf(get(column)), def);
    }

    /**
     * Get column of mysql type: bigint
     */
    public Long getLong(String column) {
        return NumberUtils.toLong(String.valueOf(get(column)));
    }

    /**
     * Get column of mysql type: unsigned bigint
     */
    public java.math.BigInteger getBigInteger(String column) {
        return (java.math.BigInteger) get(column);
    }

    /**
     * Get column of mysql type: date, year
     */
    public java.util.Date getDate(String column) {
        Object val = get(column);
        if (val instanceof Date) {
            Date date = (Date) val;
            return new java.util.Date(date.getTime());
        } else if (val instanceof java.util.Date) {
            return (java.util.Date) val;
        }
        return null;
    }

    /**
     * Get column of mysql type: real, double
     */
    public Double getDouble(String column) {
        return NumberUtils.toDouble(String.valueOf(get(column)));
    }

    /**
     * Get column of mysql type: float
     */
    public Float getFloat(String column) {
        return NumberUtils.toFloat(String.valueOf(get(column)));
    }

    /**
     * Get column of mysql type: bit, tinyint(1)
     */
    public Boolean getBoolean(String column) {
        String value = String.valueOf(get(column));
        return !("0".equals(value) || "false".equals(value));
    }

    /**
     * Get column of mysql type: decimal, numeric
     */
    public java.math.BigDecimal getBigDecimal(String column) {
        return new BigDecimal(String.valueOf(get(column)));
    }

    /**
     * Get column of mysql type: binary, varbinary, tinyblob, blob, mediumblob,
     * longblob I have not finished the test.
     */
    public byte[] getBytes(String column) {
        return (byte[]) get(column);
    }

    /**
     * Get column of any type that extends from Number
     */
    public Number getNumber(String column) {
        return (Number) get(column);
    }

    public Long getId() {
        return getLong("id");
    }

    public Record set(String key, Object value) {
        put(key, value);
        return this;
    }

    /**
     * Bean对象转换为Record,所有字段名都由驼峰转为下划线格式
     */
    public static Record valueOf(Object obj) {
        return new Record(convertBeanToMap(obj));
    }

    public static Record valueOfNullable(Object obj) {
        if (obj == null) {
            return null;
        }
        return new Record(convertBeanToMap(obj));
    }

    public Record getRecord(String key) {
        return valueOf(get(key));
    }

    /**
     * Bean对象转换为Map,所有字段名都由驼峰转为下划线格式
     */
    private static Map<String, Object> convertBeanToMap(Object javaBean) {
        if (javaBean == null) {
            return null;
        }
        if (javaBean instanceof Map) {
            return (Map<String, Object>) javaBean;
        }
        Field[] fields = javaBean.getClass().getDeclaredFields();
        Map<String, Object> result = new HashMap<>(fields.length);
        for (Field field : fields) {
            Object value;
            try {
                value = Beans.getProperty(javaBean, field.getName());
            } catch (Exception e) {
                continue;
            }
            if (value == null) {
                continue;
            }
            String dbField = CamelCase.toUnderlineName(field.getName());
            result.put(dbField, value);
        }
        return result;
    }

    public <T> T toEntity(Class<T> tClass) {
        return Pojos.mapping(this, tClass);
    }

    @Deprecated
    public static <T> T toBean(Record record, Class<T> tClass) {
        return Pojos.mapping(record, tClass);
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
