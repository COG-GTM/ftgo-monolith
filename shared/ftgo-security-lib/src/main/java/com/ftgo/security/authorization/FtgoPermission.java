package com.ftgo.security.authorization;

/**
 * Enumeration of all granular permissions in the FTGO platform.
 *
 * <p>Permissions are grouped by service/resource and define what operations
 * a user can perform. They are used in {@code @PreAuthorize} expressions
 * to enforce method-level security.
 *
 * <p>Example usage in a controller:
 * <pre>
 * &#064;PreAuthorize("hasRole('ADMIN') or hasRole('CUSTOMER')")
 * &#064;GetMapping("/consumers/{id}")
 * public Consumer getConsumer(@PathVariable Long id) { ... }
 * </pre>
 *
 * @see FtgoRole
 */
public enum FtgoPermission {

    // =========================================================================
    // Consumer Service Permissions
    // =========================================================================

    /** Create a new consumer profile. Granted to: CUSTOMER, ADMIN */
    CONSUMER_CREATE("consumer:create"),

    /** View a consumer profile (own profile for CUSTOMER). Granted to: CUSTOMER, ADMIN */
    CONSUMER_VIEW("consumer:view"),

    // =========================================================================
    // Order Service Permissions
    // =========================================================================

    /** Create a new order. Granted to: CUSTOMER, ADMIN */
    ORDER_CREATE("order:create"),

    /** View order details (own orders for CUSTOMER). Granted to: CUSTOMER, RESTAURANT_OWNER, COURIER, ADMIN */
    ORDER_VIEW("order:view"),

    /** Cancel an order (own orders for CUSTOMER). Granted to: CUSTOMER, ADMIN */
    ORDER_CANCEL("order:cancel"),

    // =========================================================================
    // Restaurant Service Permissions
    // =========================================================================

    /** View restaurant details. Granted to: CUSTOMER, RESTAURANT_OWNER, ADMIN */
    RESTAURANT_VIEW("restaurant:view"),

    /** Create a new restaurant. Granted to: RESTAURANT_OWNER, ADMIN */
    RESTAURANT_CREATE("restaurant:create"),

    /** Update restaurant details (own restaurant). Granted to: RESTAURANT_OWNER, ADMIN */
    RESTAURANT_UPDATE("restaurant:update"),

    /** Delete a restaurant (own restaurant). Granted to: RESTAURANT_OWNER, ADMIN */
    RESTAURANT_DELETE("restaurant:delete"),

    // =========================================================================
    // Delivery / Courier Permissions
    // =========================================================================

    /** Track delivery status (own delivery for CUSTOMER). Granted to: CUSTOMER, COURIER, ADMIN */
    DELIVERY_TRACK("delivery:track"),

    /** Update delivery status. Granted to: COURIER, ADMIN */
    DELIVERY_UPDATE_STATUS("delivery:update_status");

    private final String permissionString;

    FtgoPermission(String permissionString) {
        this.permissionString = permissionString;
    }

    /**
     * Returns the permission string used in security expressions.
     *
     * @return the permission string (e.g., {@code order:create})
     */
    public String getPermissionString() {
        return permissionString;
    }

    @Override
    public String toString() {
        return permissionString;
    }
}
