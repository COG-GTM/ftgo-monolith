package net.chrisrichardson.ftgo.authorization.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Defines the mapping between roles and their granted permissions.
 *
 * <p>This class provides the canonical permission matrix for the FTGO system.
 * Each role is assigned a set of permissions that determine which operations
 * the role holder can perform. Resource ownership checks are handled separately
 * by the {@link net.chrisrichardson.ftgo.authorization.evaluator.FtgoPermissionEvaluator}.</p>
 *
 * <h3>Permission Matrix</h3>
 * <pre>
 * | Role             | Permissions                                                       |
 * |------------------|-------------------------------------------------------------------|
 * | CUSTOMER         | consumer:create/read, order:create/read/cancel,                   |
 * |                  | restaurant:read, delivery:read                                   |
 * | RESTAURANT_OWNER | restaurant:create/read/update/delete, order:read                  |
 * | COURIER          | courier:read/update, order:read, delivery:read/update             |
 * | ADMIN            | All permissions                                                   |
 * </pre>
 *
 * @see FtgoRole
 * @see FtgoPermission
 */
public final class RolePermissionMapping {

    private static final Map<FtgoRole, Set<FtgoPermission>> ROLE_PERMISSIONS;

    static {
        Map<FtgoRole, Set<FtgoPermission>> map = new EnumMap<>(FtgoRole.class);

        // CUSTOMER: Create/view own consumers, create/view/cancel own orders,
        // view restaurants/menus, track own deliveries
        map.put(FtgoRole.CUSTOMER, Collections.unmodifiableSet(EnumSet.of(
                FtgoPermission.CONSUMER_CREATE,
                FtgoPermission.CONSUMER_READ,
                FtgoPermission.ORDER_CREATE,
                FtgoPermission.ORDER_READ,
                FtgoPermission.ORDER_CANCEL,
                FtgoPermission.RESTAURANT_READ,
                FtgoPermission.DELIVERY_READ
        )));

        // RESTAURANT_OWNER: Full CRUD on own restaurants, view related orders
        map.put(FtgoRole.RESTAURANT_OWNER, Collections.unmodifiableSet(EnumSet.of(
                FtgoPermission.RESTAURANT_CREATE,
                FtgoPermission.RESTAURANT_READ,
                FtgoPermission.RESTAURANT_UPDATE,
                FtgoPermission.RESTAURANT_DELETE,
                FtgoPermission.ORDER_READ
        )));

        // COURIER: View assigned orders, update delivery status,
        // manage own courier profile
        map.put(FtgoRole.COURIER, Collections.unmodifiableSet(EnumSet.of(
                FtgoPermission.COURIER_READ,
                FtgoPermission.COURIER_UPDATE,
                FtgoPermission.ORDER_READ,
                FtgoPermission.DELIVERY_READ,
                FtgoPermission.DELIVERY_UPDATE
        )));

        // ADMIN: All permissions
        map.put(FtgoRole.ADMIN, Collections.unmodifiableSet(
                EnumSet.allOf(FtgoPermission.class)
        ));

        ROLE_PERMISSIONS = Collections.unmodifiableMap(map);
    }

    private RolePermissionMapping() {
        // Utility class - prevent instantiation
    }

    /**
     * Returns the permissions granted to the specified role.
     *
     * @param role the role to look up
     * @return an unmodifiable set of permissions for the role
     */
    public static Set<FtgoPermission> getPermissionsForRole(FtgoRole role) {
        Set<FtgoPermission> permissions = ROLE_PERMISSIONS.get(role);
        return permissions != null ? permissions : Collections.<FtgoPermission>emptySet();
    }

    /**
     * Returns the complete role-to-permissions mapping.
     *
     * @return an unmodifiable map of roles to their permission sets
     */
    public static Map<FtgoRole, Set<FtgoPermission>> getAllMappings() {
        return ROLE_PERMISSIONS;
    }

    /**
     * Checks if the specified role has the given permission.
     *
     * @param role the role to check
     * @param permission the permission to verify
     * @return true if the role has the permission
     */
    public static boolean roleHasPermission(FtgoRole role, FtgoPermission permission) {
        Set<FtgoPermission> permissions = ROLE_PERMISSIONS.get(role);
        return permissions != null && permissions.contains(permission);
    }

    /**
     * Returns all permissions for the given roles (union of all role permissions).
     *
     * @param roles the roles to aggregate permissions for
     * @return a set containing all permissions from all specified roles
     */
    public static Set<FtgoPermission> getPermissionsForRoles(Iterable<FtgoRole> roles) {
        EnumSet<FtgoPermission> aggregated = EnumSet.noneOf(FtgoPermission.class);
        for (FtgoRole role : roles) {
            Set<FtgoPermission> perms = ROLE_PERMISSIONS.get(role);
            if (perms != null) {
                aggregated.addAll(perms);
            }
        }
        return Collections.unmodifiableSet(aggregated);
    }
}
