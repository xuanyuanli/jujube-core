package org.jujubeframework.exception;

/**
 * 查询异常
 *
 * @author John Li
 */
public class DaoQueryException extends RuntimeException {

    public DaoQueryException() {
    }

    public DaoQueryException(String message) {
        super(message);
    }

    public DaoQueryException(String message, Throwable cause) {
        super(message, cause);
    }
}
