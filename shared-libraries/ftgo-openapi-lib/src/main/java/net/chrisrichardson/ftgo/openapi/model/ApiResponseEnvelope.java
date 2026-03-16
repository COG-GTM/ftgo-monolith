package net.chrisrichardson.ftgo.openapi.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Standard response envelope for all FTGO REST API responses.
 *
 * <p>Every successful response is wrapped in this envelope to provide
 * a consistent structure across all services:
 * <pre>
 * {
 *   "status": "success",
 *   "data": { ... },
 *   "meta": { ... }
 * }
 * </pre>
 *
 * @param <T> the type of the response payload
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Standard API response envelope")
public class ApiResponseEnvelope<T> {

    @Schema(description = "Response status", example = "success", allowableValues = {"success", "error"})
    private String status;

    @Schema(description = "Response payload")
    private T data;

    @Schema(description = "Response metadata (pagination, timestamps, etc.)")
    private Object meta;

    public ApiResponseEnvelope() {
    }

    private ApiResponseEnvelope(String status, T data, Object meta) {
        this.status = status;
        this.data = data;
        this.meta = meta;
    }

    /**
     * Creates a successful response envelope wrapping the given data.
     *
     * @param data the response payload
     * @param <T>  the payload type
     * @return a new envelope with status "success"
     */
    public static <T> ApiResponseEnvelope<T> success(T data) {
        return new ApiResponseEnvelope<>("success", data, null);
    }

    /**
     * Creates a successful response envelope with data and metadata.
     *
     * @param data the response payload
     * @param meta additional metadata (e.g., {@link PaginationMeta})
     * @param <T>  the payload type
     * @return a new envelope with status "success"
     */
    public static <T> ApiResponseEnvelope<T> success(T data, Object meta) {
        return new ApiResponseEnvelope<>("success", data, meta);
    }

    /**
     * Creates an error response envelope.
     *
     * @param error the error details
     * @return a new envelope with status "error"
     */
    public static ApiResponseEnvelope<ErrorResponse> error(ErrorResponse error) {
        return new ApiResponseEnvelope<>("error", error, null);
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public Object getMeta() {
        return meta;
    }

    public void setMeta(Object meta) {
        this.meta = meta;
    }
}
