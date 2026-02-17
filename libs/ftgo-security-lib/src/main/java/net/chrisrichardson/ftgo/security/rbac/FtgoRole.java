package net.chrisrichardson.ftgo.security.rbac;

/**
 * Standard roles for the FTGO platform.
 * Used with @PreAuthorize annotations for method-level security.
 */
public final class FtgoRole {

    private FtgoRole() {}

    public static final String ADMIN = "ADMIN";
    public static final String CONSUMER = "CONSUMER";
    public static final String RESTAURANT_OWNER = "RESTAURANT_OWNER";
    public static final String COURIER = "COURIER";
    public static final String SERVICE = "SERVICE";

    // SpEL expressions for @PreAuthorize
    public static final String HAS_ADMIN = "hasRole('ADMIN')";
    public static final String HAS_CONSUMER = "hasRole('CONSUMER')";
    public static final String HAS_RESTAURANT_OWNER = "hasRole('RESTAURANT_OWNER')";
    public static final String HAS_COURIER = "hasRole('COURIER')";
    public static final String HAS_SERVICE = "hasRole('SERVICE')";
    public static final String HAS_ANY_AUTHENTICATED = "isAuthenticated()";
}
