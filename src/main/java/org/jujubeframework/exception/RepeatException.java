package org.jujubeframework.exception;

/**
 * 元素重复异常
 *
 * @author John Li
 */
public class RepeatException extends RuntimeException{
    public RepeatException() {
    }

    public RepeatException(String message) {
        super(message);
    }

    public RepeatException(String message, Throwable cause) {
        super(message, cause);
    }
}
