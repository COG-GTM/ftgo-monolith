package net.chrisrichardson.ftgo.security.rbac;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Defines the default mapping from {@link Role} to a set of {@link Permission}s.
 *
 * <p>This mapping is the single source of truth for which permissions each role
 * carries. It is used by {@link RolePermissionResolver} at token-creation time
 * to expand roles into the full set of granted permissions, and by the custom
 * method-security expression {@code hasPermission} at request time.
 *
 * <p>The mapping is immutable once initialised and is safe for concurrent access.
 */
public final class RolePermissionMapping {

    private static final Map<Role, Set<Permission>> ROLE_PERMISSIONS;

    static {
        Map<Role, Set<Permission>> map = new EnumMap<>(Role.class);

        map.put(Role.CUSTOMER, Collections.unmodifiableSet(EnumSet.of(
                Permission.ORDER_CREATE,
                Permission.ORDER_READ,
                Permission.ORDER_CANCEL,
                Permission.CONSUMER_READ,
                Permission.CONSUMER_UPDATE,
                Permission.RESTAURANT_READ
        )));

        map.put(Role.RESTAURANT_OWNER, Collections.unmodifiableSet(EnumSet.of(
                Permission.ORDER_READ,
                Permission.ORDER_UPDATE,
                Permission.RESTAURANT_READ,
                Permission.RESTAURANT_UPDATE,
                Permission.MENU_UPDATE
        )));

        map.put(Role.COURIER, Collections.unmodifiableSet(EnumSet.of(
                Permission.ORDER_READ,
                Permission.COURIER_READ,
                Permission.COURIER_UPDATE,
                Permission.DELIVERY_UPDATE
        )));

        map.put(Role.ADMIN, Collections.unmodifiableSet(
                EnumSet.allOf(Permission.class)
        ));

        ROLE_PERMISSIONS = Collections.unmodifiableMap(map);
    }

    private RolePermissionMapping() {
        // utility class
    }

    /**
     * Returns the immutable set of permissions granted to the given role.
     *
     * @param role the role to look up
     * @return the set of permissions, never {@code null}
     */
    public static Set<Permission> getPermissions(Role role) {
        Set<Permission> permissions = ROLE_PERMISSIONS.get(role);
        return permissions != null ? permissions : Collections.<Permission>emptySet();
    }

    /**
     * Returns the complete role-to-permission mapping.
     *
     * @return an unmodifiable map of all role-permission associations
     */
    public static Map<Role, Set<Permission>> getAllMappings() {
        return ROLE_PERMISSIONS;
    }

    /**
     * Returns the permission authority strings for the given role.
     *
     * @param role the role to look up
     * @return an unmodifiable list of permission authority strings
     */
    public static List<String> getPermissionAuthorities(Role role) {
        Set<Permission> permissions = getPermissions(role);
        String[] authorities = new String[permissions.size()];
        int i = 0;
        for (Permission p : permissions) {
            authorities[i++] = p.getAuthority();
        }
        return Collections.unmodifiableList(Arrays.asList(authorities));
    }
}
