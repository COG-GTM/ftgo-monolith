package net.chrisrichardson.ftgo.security.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * CORS configuration for FTGO microservices.
 *
 * <p>Reads CORS settings from {@link FtgoSecurityProperties} and creates
 * a {@link CorsConfigurationSource} bean used by the security filter chain.
 */
@Configuration
@EnableConfigurationProperties(FtgoSecurityProperties.class)
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
