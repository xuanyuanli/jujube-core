package org.jujubeframework.lang;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * 关于bool判断的链式执行器
 *
 * @author John Li
 */
public class BoolChain<T> {

    private final T value;

    private boolean isEq;

    private BoolChain() {
        this.value = null;
    }

    private BoolChain(T value) {
        this.value = value;
    }

    private static final BoolChain<?> EMPTY = new BoolChain<>();

    /**
     * 初始化一个值到判断链中，可为null
     */
    public static <T> BoolChain<T> of(T t) {
        if (t == null) {
            return (BoolChain<T>) EMPTY;
        }
        return new BoolChain<>(t);
    }

    /**
     * 判断值是否等于第一个参数，如果等于，则执行第二个参数的逻辑
     */
    public BoolChain<T> eqThen(T t, VoidFunction voidFunction) {
        if (!isEq && Objects.equals(t, value)) {
            voidFunction.exec();
            isEq = true;
        }
        return this;
    }

    public void orElse(VoidFunction voidFunction) {
        if (!isEq) {
            voidFunction.exec();
        }
    }

    public BoolChain<T> then(Predicate<T> predicate, VoidFunction voidFunction) {
        if (predicate.test(value)) {
            voidFunction.exec();
        }
        return this;
    }
}
