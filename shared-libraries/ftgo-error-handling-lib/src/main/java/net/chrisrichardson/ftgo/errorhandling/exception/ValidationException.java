package net.chrisrichardson.ftgo.errorhandling.exception;

import net.chrisrichardson.ftgo.errorhandling.ErrorCode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Thrown when request validation fails outside the standard Bean Validation path.
 *
 * <p>Carries a list of {@link FieldError} objects describing each invalid field.
 * Maps to HTTP 400 Bad Request.
 */
public class ValidationException extends BaseException {

    private final List<FieldError> fieldErrors;

    public ValidationException(String message) {
        super(ErrorCode.VALIDATION_FAILED, message);
        this.fieldErrors = Collections.emptyList();
    }

    public ValidationException(String message, List<FieldError> fieldErrors) {
        super(ErrorCode.VALIDATION_FAILED, message);
        this.fieldErrors = fieldErrors != null
                ? Collections.unmodifiableList(new ArrayList<>(fieldErrors))
                : Collections.emptyList();
    }

    public List<FieldError> getFieldErrors() {
        return fieldErrors;
    }

    /**
     * Represents a single field-level validation failure.
     */
    public static class FieldError {
        private final String field;
        private final String message;
        private final String code;

        public FieldError(String field, String message, String code) {
            this.field = field;
            this.message = message;
            this.code = code;
        }

        public String getField() {
            return field;
        }

        public String getMessage() {
            return message;
        }

        public String getCode() {
            return code;
        }
    }
}
