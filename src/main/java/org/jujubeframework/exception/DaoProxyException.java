package org.jujubeframework.exception;

/**
 * Dao代理异常
 *
 * @author John Li
 */
public class DaoProxyException extends  Exception{
    public DaoProxyException() {
    }

    public DaoProxyException(String message) {
        super(message);
    }

    public DaoProxyException(String message, Throwable cause) {
        super(message, cause);
    }
}
