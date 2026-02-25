package com.ftgo.common.error.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for FTGO error handling.
 *
 * <h3>Properties</h3>
 * <pre>
 * ftgo.error-handling.enabled=true          # Enable/disable error handling auto-configuration
 * ftgo.error-handling.include-stacktrace=false  # Include stack trace in error responses (dev only)
 * </pre>
 */
@ConfigurationProperties(prefix = "ftgo.error-handling")
public class FtgoErrorHandlingProperties {

    /**
     * Whether error handling auto-configuration is enabled.
     */
    private boolean enabled = true;

    /**
     * Whether to include stack traces in error responses.
     * Should only be enabled in development environments.
     */
    private boolean includeStacktrace = false;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isIncludeStacktrace() {
        return includeStacktrace;
    }

    public void setIncludeStacktrace(boolean includeStacktrace) {
        this.includeStacktrace = includeStacktrace;
    }
}
