package com.kould.exception;

public class KacheBuildException extends RuntimeException{

    private static final long serialVersionUID = 3506744387536225384L;

    public KacheBuildException() {
    }

    public KacheBuildException(String message) {
        super(message);
    }

    public KacheBuildException(String message, Throwable cause) {
        super(message, cause);
    }

    public KacheBuildException(Throwable cause) {
        super(cause);
    }

    public KacheBuildException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
