package com.ftgo.common.error.response;

/**
 * Wrapper class that nests the {@link ErrorResponse} under an "error" key,
 * matching the standardized FTGO error response format.
 *
 * <h3>Example JSON</h3>
 * <pre>
 * {
 *   "error": {
 *     "code": "VALIDATION_ERROR",
 *     "message": "Request validation failed",
 *     "status": 400,
 *     ...
 *   }
 * }
 * </pre>
 */
public class ErrorResponseWrapper {

    private final ErrorResponse error;

    public ErrorResponseWrapper(ErrorResponse error) {
        this.error = error;
    }

    public ErrorResponse getError() {
        return error;
    }

    /**
     * Creates a wrapper around the given {@link ErrorResponse}.
     *
     * @param error the error response to wrap
     * @return a new {@link ErrorResponseWrapper}
     */
    public static ErrorResponseWrapper of(ErrorResponse error) {
        return new ErrorResponseWrapper(error);
    }
}
