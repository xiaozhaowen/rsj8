package com.ab.reactor.exception;

/**
 * @author xiaozhao
 * @date 2019/4/309:48 AM
 */
public class AppException extends RuntimeException {
    public AppException(String message) {
        super(message);
    }

    public AppException(String message, Throwable cause) {
        super(message + "--" + cause.getMessage(), cause);
    }
}
