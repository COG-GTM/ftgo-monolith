package com.ftgo.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "ftgo.security")
public class SecurityProperties {

    private boolean csrfEnabled = false;
    private List<String> publicPaths = List.of("/actuator/**", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html");

    public boolean isCsrfEnabled() {
        return csrfEnabled;
    }

    public void setCsrfEnabled(boolean csrfEnabled) {
        this.csrfEnabled = csrfEnabled;
    }

    public List<String> getPublicPaths() {
        return publicPaths;
    }

    public void setPublicPaths(List<String> publicPaths) {
        this.publicPaths = publicPaths;
    }
}
