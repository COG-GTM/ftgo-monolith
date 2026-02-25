package com.ftgo.common.error.exception;

import com.ftgo.common.error.code.ErrorCode;

/**
 * Base exception for all FTGO application exceptions.
 *
 * <p>All custom exceptions in the FTGO platform should extend this class.
 * Each exception carries an {@link ErrorCode} that determines the HTTP status
 * code and error code in the response.</p>
 *
 * @see ErrorCode
 */
public abstract class FtgoException extends RuntimeException {

    private final ErrorCode errorCode;

    protected FtgoException(ErrorCode errorCode) {
        super(errorCode.getDefaultMessage());
        this.errorCode = errorCode;
    }

    protected FtgoException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    protected FtgoException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    protected FtgoException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getDefaultMessage(), cause);
        this.errorCode = errorCode;
    }

    /**
     * Returns the error code associated with this exception.
     *
     * @return the {@link ErrorCode}
     */
    public ErrorCode getErrorCode() {
        return errorCode;
    }

    /**
     * Returns the HTTP status code for this exception.
     *
     * @return the HTTP status code
     */
    public int getHttpStatus() {
        return errorCode.getHttpStatus();
    }
}
