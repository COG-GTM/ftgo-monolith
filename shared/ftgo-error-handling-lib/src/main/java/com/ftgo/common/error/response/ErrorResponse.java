package com.ftgo.common.error.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.time.Instant;
import java.util.List;

/**
 * Standardized error response following RFC 7807 Problem Details.
 *
 * <p>All FTGO microservices return this format for error responses,
 * providing consistent error information for API consumers.</p>
 *
 * <h3>Example JSON</h3>
 * <pre>
 * {
 *   "error": {
 *     "code": "ORDER_MINIMUM_NOT_MET",
 *     "message": "Order total must be at least $10.00",
 *     "status": 422,
 *     "details": [
 *       {
 *         "field": "orderTotal",
 *         "message": "Must be at least 10.00",
 *         "rejectedValue": "5.00"
 *       }
 *     ],
 *     "timestamp": "2024-01-15T10:30:00Z",
 *     "traceId": "abc123",
 *     "instance": "/api/orders"
 *   }
 * }
 * </pre>
 *
 * @see ErrorDetail
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"code", "message", "status", "details", "timestamp", "traceId", "instance"})
public class ErrorResponse {

    private String code;
    private String message;
    private int status;
    private List<ErrorDetail> details;
    private Instant timestamp;
    private String traceId;
    private String instance;

    /**
     * Default constructor for Jackson deserialization.
     */
    public ErrorResponse() {
    }

    private ErrorResponse(Builder builder) {
        this.code = builder.code;
        this.message = builder.message;
        this.status = builder.status;
        this.details = builder.details;
        this.timestamp = builder.timestamp;
        this.traceId = builder.traceId;
        this.instance = builder.instance;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public int getStatus() {
        return status;
    }

    public List<ErrorDetail> getDetails() {
        return details;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public String getTraceId() {
        return traceId;
    }

    public String getInstance() {
        return instance;
    }

    /**
     * Creates a new builder for constructing {@link ErrorResponse} instances.
     *
     * @return a new {@link Builder}
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for constructing {@link ErrorResponse} instances.
     */
    public static class Builder {
        private String code;
        private String message;
        private int status;
        private List<ErrorDetail> details;
        private Instant timestamp = Instant.now();
        private String traceId;
        private String instance;

        public Builder code(String code) {
            this.code = code;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder status(int status) {
            this.status = status;
            return this;
        }

        public Builder details(List<ErrorDetail> details) {
            this.details = details;
            return this;
        }

        public Builder timestamp(Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder traceId(String traceId) {
            this.traceId = traceId;
            return this;
        }

        public Builder instance(String instance) {
            this.instance = instance;
            return this;
        }

        public ErrorResponse build() {
            return new ErrorResponse(this);
        }
    }
}
