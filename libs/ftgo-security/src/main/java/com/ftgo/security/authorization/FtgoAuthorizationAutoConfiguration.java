package com.ftgo.security.authorization;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@AutoConfiguration
@ConditionalOnClass(EnableWebSecurity.class)
@Import({
        RoleHierarchyConfiguration.class,
        FtgoMethodSecurityConfiguration.class,
        RoleAuthorizationService.class,
        RequireRoleAspect.class
})
public class FtgoAuthorizationAutoConfiguration {
}
