package com.ftgo.security.authorization;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public enum FtgoRole {

    ADMIN("ROLE_ADMIN", "Administrator with full system access"),
    MANAGER("ROLE_MANAGER", "Manager with elevated access"),
    USER("ROLE_USER", "Standard authenticated user"),
    SERVICE("ROLE_SERVICE", "Inter-service communication role");

    private final String authority;
    private final String description;

    FtgoRole(String authority, String description) {
        this.authority = authority;
        this.description = description;
    }

    public String getAuthority() {
        return authority;
    }

    public String getDescription() {
        return description;
    }

    public String getRoleName() {
        return authority.substring("ROLE_".length());
    }

    public Set<FtgoRole> getIncludedRoles() {
        switch (this) {
            case ADMIN:
                return Collections.unmodifiableSet(EnumSet.of(ADMIN, MANAGER, USER));
            case MANAGER:
                return Collections.unmodifiableSet(EnumSet.of(MANAGER, USER));
            case USER:
                return Collections.unmodifiableSet(EnumSet.of(USER));
            case SERVICE:
                return Collections.unmodifiableSet(EnumSet.of(SERVICE));
            default:
                return Collections.unmodifiableSet(EnumSet.of(this));
        }
    }

    public boolean includes(FtgoRole other) {
        return getIncludedRoles().contains(other);
    }

    public static FtgoRole fromAuthority(String authority) {
        return Arrays.stream(values())
                .filter(role -> role.getAuthority().equals(authority))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown authority: " + authority));
    }

    public static FtgoRole fromRoleName(String roleName) {
        return Arrays.stream(values())
                .filter(role -> role.getRoleName().equalsIgnoreCase(roleName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown role name: " + roleName));
    }
}
