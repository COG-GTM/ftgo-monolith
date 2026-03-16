package net.chrisrichardson.ftgo.openapi.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Individual error detail, typically representing a single field-level
 * validation failure or a specific sub-error within a broader error response.
 *
 * <p>Example:
 * <pre>
 * {
 *   "field": "consumerId",
 *   "message": "must not be null",
 *   "code": "NotNull"
 * }
 * </pre>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Individual error detail")
public class ErrorDetail {

    @Schema(description = "Field that caused the error (null for non-field errors)", example = "consumerId")
    private String field;

    @Schema(description = "Human-readable error message", example = "must not be null")
    private String message;

    @Schema(description = "Machine-readable error code", example = "NotNull")
    private String code;

    public ErrorDetail() {
    }

    public ErrorDetail(String field, String message, String code) {
        this.field = field;
        this.message = message;
        this.code = code;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
