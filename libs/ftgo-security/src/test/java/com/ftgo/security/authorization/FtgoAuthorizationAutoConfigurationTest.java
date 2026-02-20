package com.ftgo.security.authorization;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = RbacTestApplication.class)
class FtgoAuthorizationAutoConfigurationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void roleHierarchyBeanIsRegistered() {
        assertThat(applicationContext.containsBean("roleHierarchy")).isTrue();
        assertThat(applicationContext.getBean(RoleHierarchy.class)).isNotNull();
    }

    @Test
    void methodSecurityExpressionHandlerBeanIsRegistered() {
        assertThat(applicationContext.getBean(MethodSecurityExpressionHandler.class)).isNotNull();
    }

    @Test
    void roleAuthorizationServiceBeanIsRegistered() {
        assertThat(applicationContext.getBean(RoleAuthorizationService.class)).isNotNull();
    }
}
