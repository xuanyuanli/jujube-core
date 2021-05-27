package org.jujubeframework.exception;

/**
 * Dao初始化异常
 *
 * @author John Li
 */
public class DaoInitializeException extends  RuntimeException{

    public DaoInitializeException() {
    }

    public DaoInitializeException(String message) {
        super(message);
    }

    public DaoInitializeException(String message, Throwable cause) {
        super(message, cause);
    }
}
