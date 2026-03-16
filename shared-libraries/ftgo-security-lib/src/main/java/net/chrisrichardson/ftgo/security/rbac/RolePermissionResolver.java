package net.chrisrichardson.ftgo.security.rbac;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Resolves a collection of role strings into the complete set of
 * {@link Permission} authority strings.
 *
 * <p>This resolver is typically used at token-creation time to expand
 * the roles carried by a {@link net.chrisrichardson.ftgo.security.jwt.JwtAuthenticationToken}
 * into the full set of permissions the user is entitled to.
 *
 * <pre>
 * List&lt;String&gt; roles = Arrays.asList("ROLE_CUSTOMER");
 * Set&lt;String&gt; permissions = RolePermissionResolver.resolvePermissions(roles);
 * // permissions = ["order:create", "order:read", "order:cancel", ...]
 * </pre>
 */
public final class RolePermissionResolver {

    private RolePermissionResolver() {
        // utility class
    }

    /**
     * Resolves all permissions for the given role authority strings.
     *
     * <p>Unknown role strings are silently ignored so that the resolver
     * remains forward-compatible with custom roles added outside this enum.
     *
     * @param roleAuthorities role authority strings (e.g. {@code ["ROLE_CUSTOMER", "ROLE_ADMIN"]})
     * @return an unmodifiable set of permission authority strings
     */
    public static Set<String> resolvePermissions(Collection<String> roleAuthorities) {
        if (roleAuthorities == null || roleAuthorities.isEmpty()) {
            return Collections.emptySet();
        }

        Set<Permission> resolved = EnumSet.noneOf(Permission.class);
        for (String authority : roleAuthorities) {
            try {
                Role role = Role.fromAuthority(authority);
                resolved.addAll(RolePermissionMapping.getPermissions(role));
            } catch (IllegalArgumentException ignored) {
                // unknown role — skip
            }
        }

        Set<String> authorities = new LinkedHashSet<>();
        for (Permission p : resolved) {
            authorities.add(p.getAuthority());
        }
        return Collections.unmodifiableSet(authorities);
    }

    /**
     * Resolves all permissions for a single {@link Role}.
     *
     * @param role the role to resolve
     * @return an unmodifiable set of permission authority strings
     */
    public static Set<String> resolvePermissions(Role role) {
        return resolvePermissions(Collections.singletonList(role.getAuthority()));
    }
}
