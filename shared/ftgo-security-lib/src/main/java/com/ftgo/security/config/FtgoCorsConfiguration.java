package com.ftgo.security.config;

import com.ftgo.security.properties.FtgoSecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * CORS configuration for FTGO microservices.
 * <p>
 * Configures Cross-Origin Resource Sharing based on {@link FtgoSecurityProperties}.
 * By default allows all origins for development; in production, configure
 * specific API gateway origins via {@code ftgo.security.cors.allowed-origins}.
 * </p>
 */
@Configuration
public class FtgoCorsConfiguration {

    private final FtgoSecurityProperties securityProperties;

    public FtgoCorsConfiguration(FtgoSecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        FtgoSecurityProperties.Cors corsProps = securityProperties.getCors();

        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(corsProps.getAllowedOrigins());
        configuration.setAllowedMethods(corsProps.getAllowedMethods());
        configuration.setAllowedHeaders(corsProps.getAllowedHeaders());
        configuration.setExposedHeaders(corsProps.getExposedHeaders());
        configuration.setAllowCredentials(corsProps.isAllowCredentials());
        configuration.setMaxAge(corsProps.getMaxAge());

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
