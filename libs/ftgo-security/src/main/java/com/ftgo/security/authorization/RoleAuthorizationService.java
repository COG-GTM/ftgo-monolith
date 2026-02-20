package com.ftgo.security.authorization;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RoleAuthorizationService {

    public boolean hasRole(FtgoRole role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority -> authority.equals(role.getAuthority()));
    }

    public boolean hasAnyRole(FtgoRole... roles) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        Set<String> authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
        for (FtgoRole role : roles) {
            if (authorities.contains(role.getAuthority())) {
                return true;
            }
        }
        return false;
    }

    public boolean hasAllRoles(FtgoRole... roles) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        Set<String> authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
        for (FtgoRole role : roles) {
            if (!authorities.contains(role.getAuthority())) {
                return false;
            }
        }
        return true;
    }

    public boolean hasPermission(FtgoPermission permission) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        Set<String> authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
        for (FtgoRole role : FtgoRole.values()) {
            if (authorities.contains(role.getAuthority())) {
                Set<FtgoPermission> rolePermissions = FtgoPermission.getPermissionsForRole(role);
                if (rolePermissions.contains(permission)) {
                    return true;
                }
            }
        }
        return false;
    }

    public Set<FtgoRole> getCurrentRoles() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Collections.emptySet();
        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> authority.startsWith("ROLE_"))
                .map(FtgoRole::fromAuthority)
                .collect(Collectors.toSet());
    }

    public Set<FtgoPermission> getCurrentPermissions() {
        return getCurrentRoles().stream()
                .flatMap(role -> FtgoPermission.getPermissionsForRole(role).stream())
                .collect(Collectors.toSet());
    }

    public boolean isServiceAccount() {
        return hasRole(FtgoRole.SERVICE);
    }

    public boolean isAdmin() {
        return hasRole(FtgoRole.ADMIN);
    }

    public String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        return authentication.getName();
    }

    public Collection<? extends GrantedAuthority> getCurrentAuthorities() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return Collections.emptyList();
        }
        return authentication.getAuthorities();
    }
}
