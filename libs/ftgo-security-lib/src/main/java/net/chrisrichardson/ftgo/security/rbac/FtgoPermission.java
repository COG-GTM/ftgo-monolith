package net.chrisrichardson.ftgo.security.rbac;

/**
 * Fine-grained permissions for FTGO platform operations.
 * Used in conjunction with roles for method-level authorization.
 */
public final class FtgoPermission {

    private FtgoPermission() {}

    // Order permissions
    public static final String ORDER_CREATE = "order:create";
    public static final String ORDER_READ = "order:read";
    public static final String ORDER_UPDATE = "order:update";
    public static final String ORDER_CANCEL = "order:cancel";

    // Consumer permissions
    public static final String CONSUMER_READ = "consumer:read";
    public static final String CONSUMER_UPDATE = "consumer:update";

    // Restaurant permissions
    public static final String RESTAURANT_CREATE = "restaurant:create";
    public static final String RESTAURANT_READ = "restaurant:read";
    public static final String RESTAURANT_UPDATE = "restaurant:update";
    public static final String MENU_UPDATE = "menu:update";

    // Courier permissions
    public static final String COURIER_READ = "courier:read";
    public static final String COURIER_UPDATE_AVAILABILITY = "courier:update_availability";
    public static final String DELIVERY_ASSIGN = "delivery:assign";
    public static final String DELIVERY_UPDATE = "delivery:update";
}
