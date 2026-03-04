package net.chrisrichardson.ftgo.errorhandling.constants;

/**
 * Centralized error code constants for all FTGO microservices.
 *
 * <p>Error codes follow the pattern: {@code CATEGORY_DESCRIPTION}.
 * Each code maps to a specific HTTP status and error scenario.</p>
 *
 * @see <a href="../../../../../../../../docs/error-code-catalog.md">Error Code Catalog</a>
 */
public final class FtgoErrorCodes {

    private FtgoErrorCodes() {
        // Prevent instantiation
    }

    // =========================================================================
    // General Errors (GEN_*)
    // =========================================================================

    /** Generic internal server error — no details leaked to client. */
    public static final String INTERNAL_ERROR = "GEN_INTERNAL_ERROR";

    /** The requested resource was not found. */
    public static final String RESOURCE_NOT_FOUND = "GEN_RESOURCE_NOT_FOUND";

    /** The HTTP method is not supported for this endpoint. */
    public static final String METHOD_NOT_ALLOWED = "GEN_METHOD_NOT_ALLOWED";

    /** The request body could not be parsed (malformed JSON, etc.). */
    public static final String BAD_REQUEST = "GEN_BAD_REQUEST";

    /** The requested functionality is not yet implemented. */
    public static final String NOT_IMPLEMENTED = "GEN_NOT_IMPLEMENTED";

    // =========================================================================
    // Validation Errors (VAL_*)
    // =========================================================================

    /** One or more request fields failed Bean Validation constraints. */
    public static final String VALIDATION_FAILED = "VAL_VALIDATION_FAILED";

    /** One or more request parameters failed constraint validation. */
    public static final String CONSTRAINT_VIOLATION = "VAL_CONSTRAINT_VIOLATION";

    // =========================================================================
    // Order Errors (ORD_*)
    // =========================================================================

    /** The requested order was not found. */
    public static final String ORDER_NOT_FOUND = "ORD_NOT_FOUND";

    /** The order total does not meet the restaurant minimum. */
    public static final String ORDER_MINIMUM_NOT_MET = "ORD_MINIMUM_NOT_MET";

    /** The requested state transition is not valid for the current order state. */
    public static final String ORDER_INVALID_STATE_TRANSITION = "ORD_INVALID_STATE_TRANSITION";

    // =========================================================================
    // Consumer Errors (CON_*)
    // =========================================================================

    /** The requested consumer was not found. */
    public static final String CONSUMER_NOT_FOUND = "CON_NOT_FOUND";

    // =========================================================================
    // Restaurant Errors (RST_*)
    // =========================================================================

    /** The requested restaurant was not found. */
    public static final String RESTAURANT_NOT_FOUND = "RST_NOT_FOUND";

    // =========================================================================
    // Courier Errors (CUR_*)
    // =========================================================================

    /** The requested courier was not found. */
    public static final String COURIER_NOT_FOUND = "CUR_NOT_FOUND";

    // =========================================================================
    // Security Errors (SEC_*)
    // =========================================================================

    /** Authentication is required but was not provided or is invalid. */
    public static final String UNAUTHORIZED = "SEC_UNAUTHORIZED";

    /** The authenticated user does not have permission for this operation. */
    public static final String ACCESS_DENIED = "SEC_ACCESS_DENIED";

    // =========================================================================
    // Inter-Service Communication Errors (SVC_*)
    // =========================================================================

    /** A downstream service returned an error response. */
    public static final String SERVICE_COMMUNICATION_ERROR = "SVC_COMMUNICATION_ERROR";

    /** A downstream service did not respond in time. */
    public static final String SERVICE_TIMEOUT = "SVC_TIMEOUT";

    /** A downstream service is currently unavailable. */
    public static final String SERVICE_UNAVAILABLE = "SVC_UNAVAILABLE";

    // =========================================================================
    // State Transition Errors (STATE_*)
    // =========================================================================

    /** The requested state transition is not supported for the current entity state. */
    public static final String UNSUPPORTED_STATE_TRANSITION = "STATE_UNSUPPORTED_TRANSITION";
}
