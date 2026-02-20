package com.ftgo.security.authorization;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class RequireRoleAspect {

    private final RoleAuthorizationService authorizationService;

    public RequireRoleAspect(RoleAuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
    }

    @Before("@annotation(requireRole)")
    public void checkRole(JoinPoint joinPoint, RequireRole requireRole) {
        FtgoRole[] roles = requireRole.value();
        boolean allRequired = requireRole.allRequired();

        if (allRequired) {
            if (!authorizationService.hasAllRoles(roles)) {
                throw new AccessDeniedException("Insufficient role privileges");
            }
        } else {
            if (!authorizationService.hasAnyRole(roles)) {
                throw new AccessDeniedException("Insufficient role privileges");
            }
        }
    }

    @Before("@annotation(requirePermission)")
    public void checkPermission(JoinPoint joinPoint, RequirePermission requirePermission) {
        FtgoPermission[] permissions = requirePermission.value();
        boolean allRequired = requirePermission.allRequired();

        if (allRequired) {
            for (FtgoPermission permission : permissions) {
                if (!authorizationService.hasPermission(permission)) {
                    throw new AccessDeniedException("Missing required permission: " + permission.getPermission());
                }
            }
        } else {
            boolean hasAny = false;
            for (FtgoPermission permission : permissions) {
                if (authorizationService.hasPermission(permission)) {
                    hasAny = true;
                    break;
                }
            }
            if (!hasAny) {
                throw new AccessDeniedException("Insufficient permissions");
            }
        }
    }
}
