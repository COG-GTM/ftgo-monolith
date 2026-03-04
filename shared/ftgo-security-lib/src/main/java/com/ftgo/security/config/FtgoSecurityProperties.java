package com.ftgo.security.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Externalized security configuration properties for FTGO microservices.
 *
 * <p>Properties are prefixed with {@code ftgo.security} and can be set via
 * application.properties, application.yml, or environment variables.
 *
 * <p>Example configuration:
 * <pre>
 * ftgo.security.cors.allowed-origins=http://localhost:3000,https://api.ftgo.com
 * ftgo.security.cors.max-age=3600
 * ftgo.security.public-paths=/api/public/**,/webhooks/**
 * </pre>
 */
@Component
@ConfigurationProperties(prefix = "ftgo.security")
public class FtgoSecurityProperties {

    private String[] publicPaths = {};

    /**
     * Returns additional public paths that do not require authentication.
     * These are merged with the default public paths (health, swagger, etc.).
     *
     * @return array of public path patterns
     */
    public String[] getPublicPaths() {
        return publicPaths;
    }

    /**
     * Sets additional public paths that do not require authentication.
     *
     * @param publicPaths array of public path patterns
     */
    public void setPublicPaths(String[] publicPaths) {
        this.publicPaths = publicPaths;
    }
}
