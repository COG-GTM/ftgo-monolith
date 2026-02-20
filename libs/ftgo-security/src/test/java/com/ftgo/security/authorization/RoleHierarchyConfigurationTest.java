package com.ftgo.security.authorization;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = RbacTestApplication.class)
class RoleHierarchyConfigurationTest {

    @Autowired
    private RoleHierarchy roleHierarchy;

    @Test
    void roleHierarchyBeanExists() {
        assertThat(roleHierarchy).isNotNull();
    }

    @Test
    void adminInheritsManagerAuthority() {
        Collection<?> reachable = roleHierarchy.getReachableGrantedAuthorities(
                AuthorityUtils.createAuthorityList("ROLE_ADMIN"));
        assertThat(reachable).extracting("authority")
                .contains("ROLE_ADMIN", "ROLE_MANAGER", "ROLE_USER");
    }

    @Test
    void managerInheritsUserAuthority() {
        Collection<?> reachable = roleHierarchy.getReachableGrantedAuthorities(
                AuthorityUtils.createAuthorityList("ROLE_MANAGER"));
        assertThat(reachable).extracting("authority")
                .contains("ROLE_MANAGER", "ROLE_USER");
    }

    @Test
    void managerDoesNotInheritAdmin() {
        Collection<?> reachable = roleHierarchy.getReachableGrantedAuthorities(
                AuthorityUtils.createAuthorityList("ROLE_MANAGER"));
        assertThat(reachable).extracting("authority")
                .doesNotContain("ROLE_ADMIN");
    }

    @Test
    void userDoesNotInheritOtherRoles() {
        Collection<?> reachable = roleHierarchy.getReachableGrantedAuthorities(
                AuthorityUtils.createAuthorityList("ROLE_USER"));
        assertThat(reachable).extracting("authority")
                .containsExactly("ROLE_USER");
    }

    @Test
    void serviceRoleIsIndependent() {
        Collection<?> reachable = roleHierarchy.getReachableGrantedAuthorities(
                AuthorityUtils.createAuthorityList("ROLE_SERVICE"));
        assertThat(reachable).extracting("authority")
                .containsExactly("ROLE_SERVICE");
    }
}
