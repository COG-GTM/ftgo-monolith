/**
 * Role-Based Access Control (RBAC) framework for FTGO microservices.
 *
 * <p>This package provides the authorization model including:
 * <ul>
 *   <li>{@link com.ftgo.security.authorization.FtgoRole} — role enumeration</li>
 *   <li>{@link com.ftgo.security.authorization.FtgoPermission} — permission enumeration</li>
 *   <li>{@link com.ftgo.security.authorization.FtgoRoleHierarchyConfig} — role hierarchy</li>
 *   <li>{@link com.ftgo.security.authorization.FtgoPermissionEvaluator} — ownership validation</li>
 *   <li>{@link com.ftgo.security.authorization.ResourceOwner} — resource ownership interface</li>
 * </ul>
 *
 * @see com.ftgo.security.config.FtgoBaseSecurityConfig
 */
package com.ftgo.security.authorization;
