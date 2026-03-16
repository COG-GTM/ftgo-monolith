package net.chrisrichardson.ftgo.security.rbac;

/**
 * Enumerates all fine-grained permissions recognised by the FTGO platform,
 * organised by bounded context.
 *
 * <p>Permissions follow the {@code <context>:<action>} naming convention
 * (e.g.&nbsp;{@code order:create}, {@code restaurant:update}). They are
 * stored as Spring Security authorities in the {@link
 * net.chrisrichardson.ftgo.security.jwt.JwtAuthenticationToken} and can be
 * referenced in {@code @PreAuthorize} expressions:
 *
 * <pre>
 * &#64;PreAuthorize("hasAuthority('order:create')")
 * public Order createOrder(CreateOrderRequest request) { ... }
 * </pre>
 *
 * <p>The mapping from {@link Role} to a set of permissions is defined in
 * {@link RolePermissionMapping}.
 */
public enum Permission {

    // -----------------------------------------------------------------------
    // Order bounded context
    // -----------------------------------------------------------------------

    /** Create a new order. */
    ORDER_CREATE("order:create"),

    /** Read order details. */
    ORDER_READ("order:read"),

    /** Update an existing order (e.g. accept, mark ready). */
    ORDER_UPDATE("order:update"),

    /** Cancel an order. */
    ORDER_CANCEL("order:cancel"),

    /** Full administrative management of orders. */
    ORDER_MANAGE("order:manage"),

    // -----------------------------------------------------------------------
    // Consumer bounded context
    // -----------------------------------------------------------------------

    /** Read consumer profile information. */
    CONSUMER_READ("consumer:read"),

    /** Update own consumer profile. */
    CONSUMER_UPDATE("consumer:update"),

    /** Full administrative management of consumers. */
    CONSUMER_MANAGE("consumer:manage"),

    // -----------------------------------------------------------------------
    // Restaurant bounded context
    // -----------------------------------------------------------------------

    /** Read restaurant information. */
    RESTAURANT_READ("restaurant:read"),

    /** Update restaurant details. */
    RESTAURANT_UPDATE("restaurant:update"),

    /** Full administrative management of restaurants. */
    RESTAURANT_MANAGE("restaurant:manage"),

    /** Update menu items for a restaurant. */
    MENU_UPDATE("menu:update"),

    // -----------------------------------------------------------------------
    // Courier / Delivery bounded context
    // -----------------------------------------------------------------------

    /** Read courier information. */
    COURIER_READ("courier:read"),

    /** Update own courier profile or availability. */
    COURIER_UPDATE("courier:update"),

    /** Full administrative management of couriers. */
    COURIER_MANAGE("courier:manage"),

    /** Update delivery status (e.g. picked up, delivered). */
    DELIVERY_UPDATE("delivery:update"),

    /** Full administrative management of deliveries. */
    DELIVERY_MANAGE("delivery:manage");

    private final String authority;

    Permission(String authority) {
        this.authority = authority;
    }

    /**
     * Returns the Spring Security authority string for this permission
     * (e.g.&nbsp;{@code order:create}).
     */
    public String getAuthority() {
        return authority;
    }

    /**
     * Resolves a {@link Permission} from its authority string.
     *
     * @param authority the authority string (e.g. {@code "order:create"})
     * @return the matching {@link Permission}
     * @throws IllegalArgumentException if no permission matches
     */
    public static Permission fromAuthority(String authority) {
        for (Permission permission : values()) {
            if (permission.authority.equals(authority)) {
                return permission;
            }
        }
        throw new IllegalArgumentException("Unknown permission authority: " + authority);
    }
}
