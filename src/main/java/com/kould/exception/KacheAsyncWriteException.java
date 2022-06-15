package com.kould.exception;

public class KacheAsyncWriteException extends RuntimeException {

    private static final long serialVersionUID = 3506744387536228284L;

    public KacheAsyncWriteException() {
    }

    public KacheAsyncWriteException(String message) {
        super(message);
    }

    public KacheAsyncWriteException(String message, Throwable cause) {
        super(message, cause);
    }

    public KacheAsyncWriteException(Throwable cause) {
        super(cause);
    }

    public KacheAsyncWriteException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
