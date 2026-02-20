package com.ftgo.security.authorization;

public final class RoleConstants {

    private RoleConstants() {
    }

    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String ROLE_MANAGER = "ROLE_MANAGER";
    public static final String ROLE_USER = "ROLE_USER";
    public static final String ROLE_SERVICE = "ROLE_SERVICE";

    public static final String HAS_ROLE_ADMIN = "hasRole('ADMIN')";
    public static final String HAS_ROLE_MANAGER = "hasRole('MANAGER')";
    public static final String HAS_ROLE_USER = "hasRole('USER')";
    public static final String HAS_ROLE_SERVICE = "hasRole('SERVICE')";

    public static final String HAS_ANY_ROLE_ADMIN_MANAGER = "hasAnyRole('ADMIN', 'MANAGER')";
    public static final String HAS_ANY_ROLE_ADMIN_SERVICE = "hasAnyRole('ADMIN', 'SERVICE')";

    public static final String IS_AUTHENTICATED = "isAuthenticated()";
    public static final String PERMIT_ALL = "permitAll()";
}
