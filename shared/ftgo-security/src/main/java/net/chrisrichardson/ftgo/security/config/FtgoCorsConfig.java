package net.chrisrichardson.ftgo.security.config;

import net.chrisrichardson.ftgo.security.FtgoSecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * CORS (Cross-Origin Resource Sharing) configuration for FTGO microservices.
 *
 * <p>Configures CORS to allow the API gateway and frontend origins to make
 * cross-origin requests to the microservice APIs. All settings are
 * configurable via {@link FtgoSecurityProperties}.</p>
 *
 * <p>Default configuration allows:</p>
 * <ul>
 *   <li>Origins: localhost:3000 (frontend), localhost:8080 (gateway)</li>
 *   <li>Methods: GET, POST, PUT, DELETE, PATCH, OPTIONS</li>
 *   <li>Headers: Authorization, Content-Type, Accept, Origin, X-Requested-With, X-XSRF-TOKEN</li>
 *   <li>Credentials: enabled</li>
 *   <li>Max age: 3600 seconds (1 hour)</li>
 * </ul>
 */
@Configuration
public class FtgoCorsConfig {

    private final FtgoSecurityProperties securityProperties;

    public FtgoCorsConfig(FtgoSecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
    }

    /**
     * Creates a CORS configuration source based on application properties.
     *
     * @return the configured CorsConfigurationSource
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        FtgoSecurityProperties.Cors corsProperties = securityProperties.getCors();

        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(corsProperties.getAllowedOrigins());
        configuration.setAllowedMethods(corsProperties.getAllowedMethods());
        configuration.setAllowedHeaders(corsProperties.getAllowedHeaders());
        configuration.setExposedHeaders(corsProperties.getExposedHeaders());
        configuration.setAllowCredentials(corsProperties.isAllowCredentials());
        configuration.setMaxAge(corsProperties.getMaxAge());

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration(corsProperties.getPattern(), configuration);
        return source;
    }
}
