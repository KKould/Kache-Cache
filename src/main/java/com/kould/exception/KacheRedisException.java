package com.kould.exception;

public class KacheRedisException extends RuntimeException {

    private static final long serialVersionUID = 3506744387534525384L;

    public KacheRedisException() {
    }

    public KacheRedisException(String message) {
        super(message);
    }

    public KacheRedisException(String message, Throwable cause) {
        super(message, cause);
    }

    public KacheRedisException(Throwable cause) {
        super(cause);
    }

    public KacheRedisException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
