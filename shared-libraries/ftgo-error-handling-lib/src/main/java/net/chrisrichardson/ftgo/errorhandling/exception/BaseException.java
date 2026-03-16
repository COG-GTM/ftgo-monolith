package net.chrisrichardson.ftgo.errorhandling.exception;

import net.chrisrichardson.ftgo.errorhandling.ErrorCode;

/**
 * Abstract root of the FTGO exception hierarchy.
 *
 * <p>Every application-specific exception should extend this class so the
 * {@link net.chrisrichardson.ftgo.errorhandling.handler.GlobalExceptionHandler}
 * can map it to a consistent {@link net.chrisrichardson.ftgo.openapi.model.ErrorResponse}.
 */
public abstract class BaseException extends RuntimeException {

    private final ErrorCode errorCode;

    protected BaseException(ErrorCode errorCode) {
        super(errorCode.getDefaultMessage());
        this.errorCode = errorCode;
    }

    protected BaseException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    protected BaseException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
