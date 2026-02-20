package com.ftgo.security.authorization;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FtgoRoleTest {

    @Test
    void adminAuthorityIsCorrect() {
        assertThat(FtgoRole.ADMIN.getAuthority()).isEqualTo("ROLE_ADMIN");
    }

    @Test
    void managerAuthorityIsCorrect() {
        assertThat(FtgoRole.MANAGER.getAuthority()).isEqualTo("ROLE_MANAGER");
    }

    @Test
    void userAuthorityIsCorrect() {
        assertThat(FtgoRole.USER.getAuthority()).isEqualTo("ROLE_USER");
    }

    @Test
    void serviceAuthorityIsCorrect() {
        assertThat(FtgoRole.SERVICE.getAuthority()).isEqualTo("ROLE_SERVICE");
    }

    @Test
    void getRoleNameStripsPrefix() {
        assertThat(FtgoRole.ADMIN.getRoleName()).isEqualTo("ADMIN");
        assertThat(FtgoRole.MANAGER.getRoleName()).isEqualTo("MANAGER");
        assertThat(FtgoRole.USER.getRoleName()).isEqualTo("USER");
        assertThat(FtgoRole.SERVICE.getRoleName()).isEqualTo("SERVICE");
    }

    @Test
    void adminIncludesManagerAndUser() {
        Set<FtgoRole> included = FtgoRole.ADMIN.getIncludedRoles();
        assertThat(included).containsExactlyInAnyOrder(FtgoRole.ADMIN, FtgoRole.MANAGER, FtgoRole.USER);
    }

    @Test
    void managerIncludesUser() {
        Set<FtgoRole> included = FtgoRole.MANAGER.getIncludedRoles();
        assertThat(included).containsExactlyInAnyOrder(FtgoRole.MANAGER, FtgoRole.USER);
    }

    @Test
    void userIncludesOnlyItself() {
        Set<FtgoRole> included = FtgoRole.USER.getIncludedRoles();
        assertThat(included).containsExactly(FtgoRole.USER);
    }

    @Test
    void serviceIncludesOnlyItself() {
        Set<FtgoRole> included = FtgoRole.SERVICE.getIncludedRoles();
        assertThat(included).containsExactly(FtgoRole.SERVICE);
    }

    @Test
    void serviceDoesNotIncludeUser() {
        assertThat(FtgoRole.SERVICE.includes(FtgoRole.USER)).isFalse();
    }

    @Test
    void adminIncludesUser() {
        assertThat(FtgoRole.ADMIN.includes(FtgoRole.USER)).isTrue();
    }

    @Test
    void userDoesNotIncludeAdmin() {
        assertThat(FtgoRole.USER.includes(FtgoRole.ADMIN)).isFalse();
    }

    @Test
    void fromAuthorityResolvesCorrectly() {
        assertThat(FtgoRole.fromAuthority("ROLE_ADMIN")).isEqualTo(FtgoRole.ADMIN);
        assertThat(FtgoRole.fromAuthority("ROLE_MANAGER")).isEqualTo(FtgoRole.MANAGER);
        assertThat(FtgoRole.fromAuthority("ROLE_USER")).isEqualTo(FtgoRole.USER);
        assertThat(FtgoRole.fromAuthority("ROLE_SERVICE")).isEqualTo(FtgoRole.SERVICE);
    }

    @Test
    void fromAuthorityThrowsForUnknown() {
        assertThatThrownBy(() -> FtgoRole.fromAuthority("ROLE_UNKNOWN"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unknown authority");
    }

    @Test
    void fromRoleNameResolvesCorrectly() {
        assertThat(FtgoRole.fromRoleName("ADMIN")).isEqualTo(FtgoRole.ADMIN);
        assertThat(FtgoRole.fromRoleName("manager")).isEqualTo(FtgoRole.MANAGER);
    }

    @Test
    void fromRoleNameThrowsForUnknown() {
        assertThatThrownBy(() -> FtgoRole.fromRoleName("UNKNOWN"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unknown role name");
    }

    @ParameterizedTest
    @EnumSource(FtgoRole.class)
    void allRolesHaveDescription(FtgoRole role) {
        assertThat(role.getDescription()).isNotEmpty();
    }

    @ParameterizedTest
    @EnumSource(FtgoRole.class)
    void allRolesIncludeThemselves(FtgoRole role) {
        assertThat(role.includes(role)).isTrue();
    }
}
