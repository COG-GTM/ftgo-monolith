package com.ftgo.security.authorization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import jakarta.annotation.PostConstruct;
import java.util.List;

/**
 * Auto-configuration for the FTGO Role-Based Access Control (RBAC) framework.
 * <p>
 * Imports the role hierarchy, method security, and permission evaluator
 * configurations. Automatically discovers and registers any
 * {@link ResourceOwnershipStrategy} beans with the
 * {@link FtgoPermissionEvaluator}.
 * </p>
 * <p>
 * This configuration is activated by the
 * {@link com.ftgo.security.config.FtgoSecurityAutoConfiguration} and
 * provides the RBAC layer on top of the base security foundation.
 * </p>
 *
 * @see FtgoRoleHierarchyConfiguration
 * @see FtgoMethodSecurityConfiguration
 * @see FtgoPermissionEvaluator
 */
@Configuration
@Import({
        FtgoRoleHierarchyConfiguration.class,
        FtgoMethodSecurityConfiguration.class
})
public class FtgoAuthorizationAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(
            FtgoAuthorizationAutoConfiguration.class);

    @Autowired(required = false)
    private FtgoPermissionEvaluator permissionEvaluator;

    @Autowired(required = false)
    private List<ResourceOwnershipStrategy> ownershipStrategies;

    /**
     * Auto-registers all discovered {@link ResourceOwnershipStrategy} beans
     * with the {@link FtgoPermissionEvaluator} at startup.
     */
    @PostConstruct
    public void registerOwnershipStrategies() {
        if (permissionEvaluator == null || ownershipStrategies == null) {
            log.debug("No permission evaluator or ownership strategies to register");
            return;
        }

        for (ResourceOwnershipStrategy strategy : ownershipStrategies) {
            permissionEvaluator.registerOwnershipStrategy(
                    strategy.getResourceType(), strategy);
        }

        log.info("Registered {} resource ownership strategies",
                ownershipStrategies.size());
    }
}
