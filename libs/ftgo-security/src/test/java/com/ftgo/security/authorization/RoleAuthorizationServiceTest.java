package com.ftgo.security.authorization;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class RoleAuthorizationServiceTest {

    private final RoleAuthorizationService service = new RoleAuthorizationService();

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    private void setAuthentication(String username, String... roles) {
        List<SimpleGrantedAuthority> authorities = java.util.Arrays.stream(roles)
                .map(SimpleGrantedAuthority::new)
                .toList();
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(username, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void hasRoleReturnsTrueForMatchingRole() {
        setAuthentication("admin", "ROLE_ADMIN");
        assertThat(service.hasRole(FtgoRole.ADMIN)).isTrue();
    }

    @Test
    void hasRoleReturnsFalseForNonMatchingRole() {
        setAuthentication("user", "ROLE_USER");
        assertThat(service.hasRole(FtgoRole.ADMIN)).isFalse();
    }

    @Test
    void hasRoleReturnsFalseWhenNotAuthenticated() {
        assertThat(service.hasRole(FtgoRole.USER)).isFalse();
    }

    @Test
    void hasAnyRoleReturnsTrueWhenOneMatches() {
        setAuthentication("manager", "ROLE_MANAGER");
        assertThat(service.hasAnyRole(FtgoRole.ADMIN, FtgoRole.MANAGER)).isTrue();
    }

    @Test
    void hasAnyRoleReturnsFalseWhenNoneMatch() {
        setAuthentication("user", "ROLE_USER");
        assertThat(service.hasAnyRole(FtgoRole.ADMIN, FtgoRole.MANAGER)).isFalse();
    }

    @Test
    void hasAnyRoleReturnsFalseWhenNotAuthenticated() {
        assertThat(service.hasAnyRole(FtgoRole.USER)).isFalse();
    }

    @Test
    void hasAllRolesReturnsTrueWhenAllMatch() {
        setAuthentication("admin", "ROLE_ADMIN", "ROLE_SERVICE");
        assertThat(service.hasAllRoles(FtgoRole.ADMIN, FtgoRole.SERVICE)).isTrue();
    }

    @Test
    void hasAllRolesReturnsFalseWhenOneMissing() {
        setAuthentication("admin", "ROLE_ADMIN");
        assertThat(service.hasAllRoles(FtgoRole.ADMIN, FtgoRole.SERVICE)).isFalse();
    }

    @Test
    void hasAllRolesReturnsFalseWhenNotAuthenticated() {
        assertThat(service.hasAllRoles(FtgoRole.USER)).isFalse();
    }

    @Test
    void hasPermissionReturnsTrueForAdminPermission() {
        setAuthentication("admin", "ROLE_ADMIN");
        assertThat(service.hasPermission(FtgoPermission.ADMIN_ACCESS)).isTrue();
    }

    @Test
    void hasPermissionReturnsFalseForUserWithAdminPermission() {
        setAuthentication("user", "ROLE_USER");
        assertThat(service.hasPermission(FtgoPermission.ADMIN_ACCESS)).isFalse();
    }

    @Test
    void hasPermissionReturnsTrueForUserWithOrderRead() {
        setAuthentication("user", "ROLE_USER");
        assertThat(service.hasPermission(FtgoPermission.ORDER_READ)).isTrue();
    }

    @Test
    void hasPermissionReturnsFalseWhenNotAuthenticated() {
        assertThat(service.hasPermission(FtgoPermission.ORDER_READ)).isFalse();
    }

    @Test
    void getCurrentRolesReturnsCorrectRoles() {
        setAuthentication("admin", "ROLE_ADMIN", "ROLE_SERVICE");
        Set<FtgoRole> roles = service.getCurrentRoles();
        assertThat(roles).containsExactlyInAnyOrder(FtgoRole.ADMIN, FtgoRole.SERVICE);
    }

    @Test
    void getCurrentRolesReturnsEmptyWhenNotAuthenticated() {
        assertThat(service.getCurrentRoles()).isEmpty();
    }

    @Test
    void getCurrentPermissionsReturnsAllForAdmin() {
        setAuthentication("admin", "ROLE_ADMIN");
        Set<FtgoPermission> permissions = service.getCurrentPermissions();
        assertThat(permissions).containsExactlyInAnyOrder(FtgoPermission.values());
    }

    @Test
    void getCurrentPermissionsReturnsSubsetForUser() {
        setAuthentication("user", "ROLE_USER");
        Set<FtgoPermission> permissions = service.getCurrentPermissions();
        assertThat(permissions).contains(FtgoPermission.ORDER_READ, FtgoPermission.RESTAURANT_READ);
        assertThat(permissions).doesNotContain(FtgoPermission.ADMIN_ACCESS, FtgoPermission.SYSTEM_CONFIG);
    }

    @Test
    void isServiceAccountReturnsTrueForService() {
        setAuthentication("order-service", "ROLE_SERVICE");
        assertThat(service.isServiceAccount()).isTrue();
    }

    @Test
    void isServiceAccountReturnsFalseForUser() {
        setAuthentication("user", "ROLE_USER");
        assertThat(service.isServiceAccount()).isFalse();
    }

    @Test
    void isAdminReturnsTrueForAdmin() {
        setAuthentication("admin", "ROLE_ADMIN");
        assertThat(service.isAdmin()).isTrue();
    }

    @Test
    void isAdminReturnsFalseForManager() {
        setAuthentication("manager", "ROLE_MANAGER");
        assertThat(service.isAdmin()).isFalse();
    }

    @Test
    void getCurrentUsernameReturnsCorrectUsername() {
        setAuthentication("john.doe", "ROLE_USER");
        assertThat(service.getCurrentUsername()).isEqualTo("john.doe");
    }

    @Test
    void getCurrentUsernameReturnsNullWhenNotAuthenticated() {
        assertThat(service.getCurrentUsername()).isNull();
    }

    @Test
    void getCurrentAuthoritiesReturnsAuthorities() {
        setAuthentication("user", "ROLE_USER", "ROLE_MANAGER");
        assertThat(service.getCurrentAuthorities()).hasSize(2);
    }

    @Test
    void getCurrentAuthoritiesReturnsEmptyWhenNoAuth() {
        assertThat(service.getCurrentAuthorities()).isEmpty();
    }
}
