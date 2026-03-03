package net.chrisrichardson.ftgo.jwt.model;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Represents the authenticated user context extracted from a JWT token.
 *
 * <p>This immutable object holds the user identity information propagated
 * through the security context, including user ID, roles, and permissions.
 * It is populated by the {@link net.chrisrichardson.ftgo.jwt.filter.JwtTokenAuthenticationFilter}
 * and made available via {@link net.chrisrichardson.ftgo.jwt.util.JwtUserContextHolder}.</p>
 *
 * <p>Example usage in a service layer:</p>
 * <pre>
 * FtgoUserContext user = JwtUserContextHolder.getCurrentUser();
 * String userId = user.getUserId();
 * boolean isAdmin = user.hasRole("ADMIN");
 * </pre>
 */
public class FtgoUserContext implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String userId;
    private final String username;
    private final Set<String> roles;
    private final Set<String> permissions;
    private final String tokenId;
    private final String issuer;

    private FtgoUserContext(Builder builder) {
        this.userId = builder.userId;
        this.username = builder.username;
        this.roles = Collections.unmodifiableSet(new HashSet<>(builder.roles));
        this.permissions = Collections.unmodifiableSet(new HashSet<>(builder.permissions));
        this.tokenId = builder.tokenId;
        this.issuer = builder.issuer;
    }

    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public Set<String> getPermissions() {
        return permissions;
    }

    public String getTokenId() {
        return tokenId;
    }

    public String getIssuer() {
        return issuer;
    }

    /**
     * Checks if the user has the specified role.
     *
     * @param role the role to check (without ROLE_ prefix)
     * @return true if the user has the role
     */
    public boolean hasRole(String role) {
        return roles.contains(role);
    }

    /**
     * Checks if the user has the specified permission.
     *
     * @param permission the permission to check
     * @return true if the user has the permission
     */
    public boolean hasPermission(String permission) {
        return permissions.contains(permission);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FtgoUserContext that = (FtgoUserContext) o;
        return Objects.equals(userId, that.userId)
                && Objects.equals(username, that.username)
                && Objects.equals(roles, that.roles)
                && Objects.equals(permissions, that.permissions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, username, roles, permissions);
    }

    @Override
    public String toString() {
        return "FtgoUserContext{" +
                "userId='" + userId + '\'' +
                ", username='" + username + '\'' +
                ", roles=" + roles +
                ", permissions=" + permissions +
                '}';
    }

    /**
     * Creates a new builder for constructing FtgoUserContext instances.
     *
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for constructing {@link FtgoUserContext} instances.
     */
    public static class Builder {
        private String userId;
        private String username;
        private Set<String> roles = new HashSet<>();
        private Set<String> permissions = new HashSet<>();
        private String tokenId;
        private String issuer;

        private Builder() {
        }

        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder roles(Collection<String> roles) {
            this.roles = new HashSet<>(roles);
            return this;
        }

        public Builder addRole(String role) {
            this.roles.add(role);
            return this;
        }

        public Builder permissions(Collection<String> permissions) {
            this.permissions = new HashSet<>(permissions);
            return this;
        }

        public Builder addPermission(String permission) {
            this.permissions.add(permission);
            return this;
        }

        public Builder tokenId(String tokenId) {
            this.tokenId = tokenId;
            return this;
        }

        public Builder issuer(String issuer) {
            this.issuer = issuer;
            return this;
        }

        public FtgoUserContext build() {
            return new FtgoUserContext(this);
        }
    }
}
