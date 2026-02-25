package com.ftgo.security.authorization;

/**
 * Defines fine-grained permissions for the FTGO platform, organized
 * by bounded context.
 * <p>
 * Each permission follows the pattern {@code <context>:<operation>}
 * (e.g. {@code order:create}, {@code restaurant:update}).
 * Permissions are carried in the JWT {@code permissions} claim and
 * can be checked via {@link com.ftgo.security.util.SecurityUtils#hasPermission(String)}
 * or via SpEL expressions in {@code @PreAuthorize} annotations.
 * </p>
 *
 * @see FtgoRole
 * @see FtgoPermissionEvaluator
 */
public enum FtgoPermission {

    // ---------------------------------------------------------------
    // Consumer Service Permissions
    // ---------------------------------------------------------------
    /** Create a new consumer (self-registration or admin). */
    CONSUMER_CREATE("consumer:create"),
    /** View consumer details. */
    CONSUMER_VIEW("consumer:view"),
    /** Update consumer information. */
    CONSUMER_UPDATE("consumer:update"),
    /** Delete a consumer (admin only). */
    CONSUMER_DELETE("consumer:delete"),

    // ---------------------------------------------------------------
    // Order Service Permissions
    // ---------------------------------------------------------------
    /** Create a new order. */
    ORDER_CREATE("order:create"),
    /** View order details. */
    ORDER_VIEW("order:view"),
    /** Cancel an order. */
    ORDER_CANCEL("order:cancel"),
    /** Update order status (restaurant/courier). */
    ORDER_UPDATE("order:update"),
    /** Delete an order (admin only). */
    ORDER_DELETE("order:delete"),

    // ---------------------------------------------------------------
    // Restaurant Service Permissions
    // ---------------------------------------------------------------
    /** Create a new restaurant. */
    RESTAURANT_CREATE("restaurant:create"),
    /** View restaurant details and menus. */
    RESTAURANT_VIEW("restaurant:view"),
    /** Update restaurant information or menu. */
    RESTAURANT_UPDATE("restaurant:update"),
    /** Delete a restaurant (admin only). */
    RESTAURANT_DELETE("restaurant:delete"),

    // ---------------------------------------------------------------
    // Courier / Delivery Service Permissions
    // ---------------------------------------------------------------
    /** View delivery details. */
    DELIVERY_VIEW("delivery:view"),
    /** Update delivery status (courier). */
    DELIVERY_UPDATE("delivery:update"),
    /** Plan a delivery route. */
    DELIVERY_PLAN("delivery:plan"),
    /** Delete a delivery record (admin only). */
    DELIVERY_DELETE("delivery:delete");

    private final String permission;

    FtgoPermission(String permission) {
        this.permission = permission;
    }

    /**
     * Returns the permission string (e.g. {@code order:create}).
     */
    public String getPermission() {
        return permission;
    }

    @Override
    public String toString() {
        return permission;
    }

    /**
     * Resolves a {@link FtgoPermission} from its string representation.
     *
     * @param permission the permission string (e.g. {@code order:create})
     * @return the matching permission
     * @throws IllegalArgumentException if no matching permission is found
     */
    public static FtgoPermission fromString(String permission) {
        for (FtgoPermission p : values()) {
            if (p.permission.equals(permission)) {
                return p;
            }
        }
        throw new IllegalArgumentException("Unknown permission: " + permission);
    }
}
