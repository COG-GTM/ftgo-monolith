/**
 * Role-Based Access Control (RBAC) framework for the FTGO platform.
 * <p>
 * This package provides:
 * <ul>
 *   <li>{@link com.ftgo.security.authorization.FtgoRole} — role enumeration
 *       (CUSTOMER, RESTAURANT_OWNER, COURIER, ADMIN)</li>
 *   <li>{@link com.ftgo.security.authorization.FtgoPermission} — fine-grained
 *       permissions per bounded context</li>
 *   <li>{@link com.ftgo.security.authorization.FtgoRoleHierarchyConfiguration} —
 *       role hierarchy (ADMIN &gt; RESTAURANT_OWNER &gt; CUSTOMER)</li>
 *   <li>{@link com.ftgo.security.authorization.FtgoMethodSecurityConfiguration} —
 *       enables {@code @PreAuthorize}, {@code @Secured}, and JSR-250 annotations</li>
 *   <li>{@link com.ftgo.security.authorization.FtgoPermissionEvaluator} —
 *       custom permission evaluator for ownership checks</li>
 *   <li>{@link com.ftgo.security.authorization.ResourceOwnershipStrategy} —
 *       strategy interface for service-specific ownership validation</li>
 * </ul>
 *
 * @see com.ftgo.security.config.FtgoSecurityAutoConfiguration
 */
package com.ftgo.security.authorization;
