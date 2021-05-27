package org.jujubeframework.jdbc.support.annotation;

import java.lang.annotation.*;

/**
 * @author John Li
 */
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Column {
    String value() default "";
}
