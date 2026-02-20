package com.ftgo.security.authorization;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FtgoPermissionTest {

    @Test
    void adminHasAllPermissions() {
        Set<FtgoPermission> permissions = FtgoPermission.getPermissionsForRole(FtgoRole.ADMIN);
        assertThat(permissions).containsExactlyInAnyOrder(FtgoPermission.values());
    }

    @Test
    void managerHasElevatedPermissions() {
        Set<FtgoPermission> permissions = FtgoPermission.getPermissionsForRole(FtgoRole.MANAGER);
        assertThat(permissions).contains(
                FtgoPermission.ORDER_READ, FtgoPermission.ORDER_CREATE,
                FtgoPermission.ORDER_UPDATE, FtgoPermission.ORDER_CANCEL,
                FtgoPermission.RESTAURANT_READ, FtgoPermission.RESTAURANT_CREATE,
                FtgoPermission.RESTAURANT_UPDATE,
                FtgoPermission.USER_READ, FtgoPermission.USER_CREATE, FtgoPermission.USER_UPDATE
        );
        assertThat(permissions).doesNotContain(
                FtgoPermission.ADMIN_ACCESS, FtgoPermission.SYSTEM_CONFIG,
                FtgoPermission.ORDER_DELETE, FtgoPermission.RESTAURANT_DELETE, FtgoPermission.USER_DELETE
        );
    }

    @Test
    void userHasBasicPermissions() {
        Set<FtgoPermission> permissions = FtgoPermission.getPermissionsForRole(FtgoRole.USER);
        assertThat(permissions).containsExactlyInAnyOrder(
                FtgoPermission.ORDER_READ, FtgoPermission.ORDER_CREATE,
                FtgoPermission.RESTAURANT_READ,
                FtgoPermission.MENU_READ,
                FtgoPermission.DELIVERY_READ
        );
    }

    @Test
    void userDoesNotHaveWritePermissions() {
        Set<FtgoPermission> permissions = FtgoPermission.getPermissionsForRole(FtgoRole.USER);
        assertThat(permissions).doesNotContain(
                FtgoPermission.ORDER_UPDATE, FtgoPermission.ORDER_DELETE,
                FtgoPermission.RESTAURANT_CREATE, FtgoPermission.RESTAURANT_UPDATE,
                FtgoPermission.ADMIN_ACCESS
        );
    }

    @Test
    void serviceHasInterServicePermissions() {
        Set<FtgoPermission> permissions = FtgoPermission.getPermissionsForRole(FtgoRole.SERVICE);
        assertThat(permissions).contains(
                FtgoPermission.ORDER_READ, FtgoPermission.ORDER_CREATE,
                FtgoPermission.ORDER_UPDATE, FtgoPermission.ORDER_CANCEL,
                FtgoPermission.DELIVERY_READ, FtgoPermission.DELIVERY_UPDATE
        );
        assertThat(permissions).doesNotContain(
                FtgoPermission.ADMIN_ACCESS, FtgoPermission.SYSTEM_CONFIG,
                FtgoPermission.USER_DELETE
        );
    }

    @Test
    void fromStringResolvesCorrectly() {
        assertThat(FtgoPermission.fromString("order:read")).isEqualTo(FtgoPermission.ORDER_READ);
        assertThat(FtgoPermission.fromString("admin:access")).isEqualTo(FtgoPermission.ADMIN_ACCESS);
    }

    @Test
    void fromStringThrowsForUnknown() {
        assertThatThrownBy(() -> FtgoPermission.fromString("unknown:permission"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unknown permission");
    }

    @ParameterizedTest
    @EnumSource(FtgoPermission.class)
    void allPermissionsHaveDescription(FtgoPermission permission) {
        assertThat(permission.getDescription()).isNotEmpty();
    }

    @ParameterizedTest
    @EnumSource(FtgoPermission.class)
    void allPermissionsHavePermissionString(FtgoPermission permission) {
        assertThat(permission.getPermission()).isNotEmpty();
        assertThat(permission.getPermission()).contains(":");
    }
}
