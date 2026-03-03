package net.chrisrichardson.ftgo.authorization.model;

/**
 * Defines fine-grained permissions for FTGO service operations.
 *
 * <p>Permissions follow the pattern {@code <service>:<action>} and are mapped
 * to specific API operations. They are stored in JWT claims and evaluated
 * by the {@link net.chrisrichardson.ftgo.authorization.evaluator.FtgoPermissionEvaluator}.</p>
 *
 * <h3>Permission Matrix</h3>
 * <table>
 *   <tr><th>Permission</th><th>CUSTOMER</th><th>RESTAURANT_OWNER</th><th>COURIER</th><th>ADMIN</th></tr>
 *   <tr><td>consumer:create</td><td>Y</td><td></td><td></td><td>Y</td></tr>
 *   <tr><td>consumer:read</td><td>Own</td><td></td><td></td><td>Y</td></tr>
 *   <tr><td>order:create</td><td>Y</td><td></td><td></td><td>Y</td></tr>
 *   <tr><td>order:read</td><td>Own</td><td>Related</td><td>Assigned</td><td>Y</td></tr>
 *   <tr><td>order:cancel</td><td>Own</td><td></td><td></td><td>Y</td></tr>
 *   <tr><td>restaurant:create</td><td></td><td>Y</td><td></td><td>Y</td></tr>
 *   <tr><td>restaurant:read</td><td>Y</td><td>Y</td><td></td><td>Y</td></tr>
 *   <tr><td>restaurant:update</td><td></td><td>Own</td><td></td><td>Y</td></tr>
 *   <tr><td>restaurant:delete</td><td></td><td>Own</td><td></td><td>Y</td></tr>
 *   <tr><td>courier:read</td><td></td><td></td><td>Own</td><td>Y</td></tr>
 *   <tr><td>courier:update</td><td></td><td></td><td>Own</td><td>Y</td></tr>
 *   <tr><td>delivery:read</td><td>Own</td><td></td><td>Assigned</td><td>Y</td></tr>
 *   <tr><td>delivery:update</td><td></td><td></td><td>Assigned</td><td>Y</td></tr>
 * </table>
 *
 * @see FtgoRole
 */
public enum FtgoPermission {

    // -------------------------------------------------------------------------
    // Consumer Service Permissions
    // -------------------------------------------------------------------------
    CONSUMER_CREATE("consumer:create", "Create a new consumer"),
    CONSUMER_READ("consumer:read", "View consumer details"),

    // -------------------------------------------------------------------------
    // Order Service Permissions
    // -------------------------------------------------------------------------
    ORDER_CREATE("order:create", "Create a new order"),
    ORDER_READ("order:read", "View order details"),
    ORDER_CANCEL("order:cancel", "Cancel an existing order"),

    // -------------------------------------------------------------------------
    // Restaurant Service Permissions
    // -------------------------------------------------------------------------
    RESTAURANT_CREATE("restaurant:create", "Create a new restaurant"),
    RESTAURANT_READ("restaurant:read", "View restaurant details and menus"),
    RESTAURANT_UPDATE("restaurant:update", "Update restaurant information"),
    RESTAURANT_DELETE("restaurant:delete", "Delete a restaurant"),

    // -------------------------------------------------------------------------
    // Courier Service Permissions
    // -------------------------------------------------------------------------
    COURIER_READ("courier:read", "View courier details"),
    COURIER_UPDATE("courier:update", "Update courier availability/status"),

    // -------------------------------------------------------------------------
    // Delivery Permissions
    // -------------------------------------------------------------------------
    DELIVERY_READ("delivery:read", "View delivery status"),
    DELIVERY_UPDATE("delivery:update", "Update delivery status");

    private final String value;
    private final String description;

    FtgoPermission(String value, String description) {
        this.value = value;
        this.description = description;
    }

    /**
     * Returns the permission string in {@code service:action} format.
     *
     * @return the permission value
     */
    public String getValue() {
        return value;
    }

    /**
     * Returns a human-readable description of the permission.
     *
     * @return the permission description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Finds an FtgoPermission by its value string.
     *
     * @param value the permission value (e.g., "order:create")
     * @return the matching FtgoPermission
     * @throws IllegalArgumentException if no matching permission is found
     */
    public static FtgoPermission fromValue(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Permission value cannot be null");
        }
        for (FtgoPermission permission : values()) {
            if (permission.value.equals(value)) {
                return permission;
            }
        }
        throw new IllegalArgumentException("Unknown permission: " + value);
    }
}
