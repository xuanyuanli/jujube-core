package org.jujubeframework.jdbc.support.entity;

import org.jujubeframework.lang.Record;
import org.jujubeframework.util.Beans;
import org.jujubeframework.util.Jsons;
import org.jujubeframework.util.Pojos;

import java.io.Serializable;
import java.util.Map;

/**
 * entity的基类，所有entity都要继承这个类
 *
 * @author John Li jujubeframework@163.com
 */
public interface BaseEntity extends Serializable {

    /**
     * String的null值
     */
    String STRING_NULL = "(JUJUBE-JDBC-NULL)";

    /**
     * clone自己
     *
     * @param <T> 泛型
     * @return self type object
     */
    default <T> T cloneSelf() {
        try {
            return (T) Pojos.mapping(this, this.getClass());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * to json
     *
     * @return json
     */
    default String toJson() {
        return Jsons.toJson(this);
    }

    /**
     * 将Bean转换为Record，会把Bean中驼峰命名的字段转为下划线命名
     *
     * @return Record
     */
    default Record toRecord() {
        return Record.valueOf(this);
    }

    /**
     * 将Bean转换为Map，直接转换，不涉及到命名方式的变动
     *
     * @return Map
     */
    default Map<String, Object> toMap() {
        return Beans.beanToMap(this);
    }

    /**
     * 将Bean转换为Map，直接转换，不涉及到命名方式的变动.过滤null值的key
     *
     * @return Map
     */
    default Map<String, Object> toMapFilterNull() {
        return Beans.beanToMap(this, true);
    }

    /**
     * 将Bean赋值给对象类型的BO
     *
     * @param cl  class
     * @param <T> 要转换的类型
     * @return bo
     */
    default <T> T toBO(Class<T> cl) {
        return Pojos.mapping(this, cl);
    }
}
