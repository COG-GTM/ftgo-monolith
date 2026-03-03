package com.ftgo.errorhandling.constants;

/**
 * Centralized error code constants for all FTGO microservices.
 *
 * <p>Error codes follow the format: {@code FTGO-XXYYY} where:</p>
 * <ul>
 *   <li>{@code XX} - Category (00=General, 01=Validation, 02=Auth, 03=Order, 04=Consumer,
 *       05=Restaurant, 06=Courier, 07=Inter-Service)</li>
 *   <li>{@code YYY} - Specific error within the category</li>
 * </ul>
 *
 * <p>All services MUST use these constants instead of hardcoded strings to ensure
 * consistent error identification across the platform.</p>
 */
public final class ErrorCodes {

    private ErrorCodes() {
        // Utility class - prevent instantiation
    }

    // -------------------------------------------------------------------------
    // General Errors (FTGO-00xxx)
    // -------------------------------------------------------------------------

    /** Internal server error - unexpected failure. */
    public static final String INTERNAL_ERROR = "FTGO-00001";

    /** Resource not found. */
    public static final String RESOURCE_NOT_FOUND = "FTGO-00002";

    /** Method not allowed for the requested resource. */
    public static final String METHOD_NOT_ALLOWED = "FTGO-00003";

    /** Unsupported media type in request. */
    public static final String UNSUPPORTED_MEDIA_TYPE = "FTGO-00004";

    /** Request body is missing or malformed. */
    public static final String MALFORMED_REQUEST = "FTGO-00005";

    /** Request parameter is missing. */
    public static final String MISSING_PARAMETER = "FTGO-00006";

    /** Type mismatch in request parameter. */
    public static final String TYPE_MISMATCH = "FTGO-00007";

    // -------------------------------------------------------------------------
    // Validation Errors (FTGO-01xxx)
    // -------------------------------------------------------------------------

    /** Bean validation failed on request body. */
    public static final String VALIDATION_FAILED = "FTGO-01001";

    /** A required field is missing or null. */
    public static final String FIELD_REQUIRED = "FTGO-01002";

    /** A field value is out of the allowed range. */
    public static final String FIELD_OUT_OF_RANGE = "FTGO-01003";

    /** A field value does not match the expected pattern. */
    public static final String FIELD_INVALID_FORMAT = "FTGO-01004";

    /** A field value exceeds the maximum allowed length. */
    public static final String FIELD_TOO_LONG = "FTGO-01005";

    // -------------------------------------------------------------------------
    // Authentication & Authorization Errors (FTGO-02xxx)
    // -------------------------------------------------------------------------

    /** Authentication required - no valid credentials provided. */
    public static final String UNAUTHORIZED = "FTGO-02001";

    /** Access denied - insufficient permissions. */
    public static final String FORBIDDEN = "FTGO-02002";

    /** Authentication token has expired. */
    public static final String TOKEN_EXPIRED = "FTGO-02003";

    /** Authentication token is invalid or malformed. */
    public static final String TOKEN_INVALID = "FTGO-02004";

    // -------------------------------------------------------------------------
    // Order Errors (FTGO-03xxx)
    // -------------------------------------------------------------------------

    /** Order not found. */
    public static final String ORDER_NOT_FOUND = "FTGO-03001";

    /** Unsupported state transition for order. */
    public static final String ORDER_INVALID_STATE_TRANSITION = "FTGO-03002";

    /** Order minimum amount not met. */
    public static final String ORDER_MINIMUM_NOT_MET = "FTGO-03003";

    /** Order revision conflict. */
    public static final String ORDER_REVISION_CONFLICT = "FTGO-03004";

    // -------------------------------------------------------------------------
    // Consumer Errors (FTGO-04xxx)
    // -------------------------------------------------------------------------

    /** Consumer not found. */
    public static final String CONSUMER_NOT_FOUND = "FTGO-04001";

    /** Consumer validation failed for order. */
    public static final String CONSUMER_VALIDATION_FAILED = "FTGO-04002";

    // -------------------------------------------------------------------------
    // Restaurant Errors (FTGO-05xxx)
    // -------------------------------------------------------------------------

    /** Restaurant not found. */
    public static final String RESTAURANT_NOT_FOUND = "FTGO-05001";

    /** Menu item not found in restaurant menu. */
    public static final String MENU_ITEM_NOT_FOUND = "FTGO-05002";

    /** Restaurant is currently not accepting orders. */
    public static final String RESTAURANT_UNAVAILABLE = "FTGO-05003";

    // -------------------------------------------------------------------------
    // Courier Errors (FTGO-06xxx)
    // -------------------------------------------------------------------------

    /** Courier not found. */
    public static final String COURIER_NOT_FOUND = "FTGO-06001";

    /** No available couriers for delivery. */
    public static final String NO_AVAILABLE_COURIER = "FTGO-06002";

    // -------------------------------------------------------------------------
    // Inter-Service Communication Errors (FTGO-07xxx)
    // -------------------------------------------------------------------------

    /** Downstream service is unavailable. */
    public static final String SERVICE_UNAVAILABLE = "FTGO-07001";

    /** Downstream service request timed out. */
    public static final String SERVICE_TIMEOUT = "FTGO-07002";

    /** Downstream service returned an unexpected error. */
    public static final String SERVICE_ERROR = "FTGO-07003";

    /** Circuit breaker is open - service calls are being rejected. */
    public static final String CIRCUIT_BREAKER_OPEN = "FTGO-07004";

    /** Service communication failure - connection refused or reset. */
    public static final String SERVICE_CONNECTION_FAILURE = "FTGO-07005";
}
