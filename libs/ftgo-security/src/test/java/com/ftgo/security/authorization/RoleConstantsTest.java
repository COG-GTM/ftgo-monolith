package com.ftgo.security.authorization;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RoleConstantsTest {

    @Test
    void roleAdminConstant() {
        assertThat(RoleConstants.ROLE_ADMIN).isEqualTo("ROLE_ADMIN");
    }

    @Test
    void roleManagerConstant() {
        assertThat(RoleConstants.ROLE_MANAGER).isEqualTo("ROLE_MANAGER");
    }

    @Test
    void roleUserConstant() {
        assertThat(RoleConstants.ROLE_USER).isEqualTo("ROLE_USER");
    }

    @Test
    void roleServiceConstant() {
        assertThat(RoleConstants.ROLE_SERVICE).isEqualTo("ROLE_SERVICE");
    }

    @Test
    void hasRoleExpressions() {
        assertThat(RoleConstants.HAS_ROLE_ADMIN).isEqualTo("hasRole('ADMIN')");
        assertThat(RoleConstants.HAS_ROLE_MANAGER).isEqualTo("hasRole('MANAGER')");
        assertThat(RoleConstants.HAS_ROLE_USER).isEqualTo("hasRole('USER')");
        assertThat(RoleConstants.HAS_ROLE_SERVICE).isEqualTo("hasRole('SERVICE')");
    }

    @Test
    void hasAnyRoleExpressions() {
        assertThat(RoleConstants.HAS_ANY_ROLE_ADMIN_MANAGER).isEqualTo("hasAnyRole('ADMIN', 'MANAGER')");
        assertThat(RoleConstants.HAS_ANY_ROLE_ADMIN_SERVICE).isEqualTo("hasAnyRole('ADMIN', 'SERVICE')");
    }

    @Test
    void genericExpressions() {
        assertThat(RoleConstants.IS_AUTHENTICATED).isEqualTo("isAuthenticated()");
        assertThat(RoleConstants.PERMIT_ALL).isEqualTo("permitAll()");
    }
}
