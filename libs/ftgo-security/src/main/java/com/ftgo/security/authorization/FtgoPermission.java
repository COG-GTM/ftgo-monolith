package com.ftgo.security.authorization;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public enum FtgoPermission {

    ORDER_READ("order:read", "Read order information"),
    ORDER_CREATE("order:create", "Create new orders"),
    ORDER_UPDATE("order:update", "Update existing orders"),
    ORDER_DELETE("order:delete", "Delete orders"),
    ORDER_CANCEL("order:cancel", "Cancel orders"),

    RESTAURANT_READ("restaurant:read", "Read restaurant information"),
    RESTAURANT_CREATE("restaurant:create", "Create new restaurants"),
    RESTAURANT_UPDATE("restaurant:update", "Update existing restaurants"),
    RESTAURANT_DELETE("restaurant:delete", "Delete restaurants"),

    MENU_READ("menu:read", "Read menu information"),
    MENU_UPDATE("menu:update", "Update menu items"),

    USER_READ("user:read", "Read user information"),
    USER_CREATE("user:create", "Create new users"),
    USER_UPDATE("user:update", "Update user information"),
    USER_DELETE("user:delete", "Delete users"),

    DELIVERY_READ("delivery:read", "Read delivery information"),
    DELIVERY_UPDATE("delivery:update", "Update delivery status"),

    ADMIN_ACCESS("admin:access", "Access admin functionality"),
    SYSTEM_CONFIG("system:config", "Modify system configuration");

    private final String permission;
    private final String description;

    FtgoPermission(String permission, String description) {
        this.permission = permission;
        this.description = description;
    }

    public String getPermission() {
        return permission;
    }

    public String getDescription() {
        return description;
    }

    public static Set<FtgoPermission> getPermissionsForRole(FtgoRole role) {
        switch (role) {
            case ADMIN:
                return Collections.unmodifiableSet(EnumSet.allOf(FtgoPermission.class));
            case MANAGER:
                return Collections.unmodifiableSet(EnumSet.of(
                        ORDER_READ, ORDER_CREATE, ORDER_UPDATE, ORDER_CANCEL,
                        RESTAURANT_READ, RESTAURANT_CREATE, RESTAURANT_UPDATE,
                        MENU_READ, MENU_UPDATE,
                        USER_READ, USER_CREATE, USER_UPDATE,
                        DELIVERY_READ, DELIVERY_UPDATE
                ));
            case USER:
                return Collections.unmodifiableSet(EnumSet.of(
                        ORDER_READ, ORDER_CREATE,
                        RESTAURANT_READ,
                        MENU_READ,
                        DELIVERY_READ
                ));
            case SERVICE:
                return Collections.unmodifiableSet(EnumSet.of(
                        ORDER_READ, ORDER_CREATE, ORDER_UPDATE, ORDER_CANCEL,
                        RESTAURANT_READ,
                        MENU_READ,
                        DELIVERY_READ, DELIVERY_UPDATE
                ));
            default:
                return Collections.emptySet();
        }
    }

    public static FtgoPermission fromString(String permission) {
        return Arrays.stream(values())
                .filter(p -> p.getPermission().equals(permission))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown permission: " + permission));
    }
}
