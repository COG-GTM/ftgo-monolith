package net.chrisrichardson.ftgo.openapi.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configures URL path versioning for all FTGO REST APIs.
 *
 * <p>API versioning strategy: URL path versioning ({@code /api/v1/...}).
 * This approach was chosen for its simplicity, visibility, and ease of
 * debugging compared to header-based or query-parameter versioning.</p>
 *
 * <p>When enabled via {@code ftgo.api.versioning.enabled=true}, all
 * controller endpoints will be prefixed with {@code /api/v1}.</p>
 *
 * <h3>Version Deprecation Policy</h3>
 * <ul>
 *   <li>Deprecated versions are announced at least 6 months before removal</li>
 *   <li>Maximum of 2 concurrent API versions supported</li>
 *   <li>Deprecated versions return a {@code Sunset} header with the removal date</li>
 *   <li>A {@code Deprecation} header is included with the deprecation date</li>
 * </ul>
 */
@Configuration
public class ApiVersioningConfiguration implements WebMvcConfigurer {

    private static final String API_V1_PREFIX = "/api/v1";

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.addPathPrefix(API_V1_PREFIX,
                clazz -> clazz.isAnnotationPresent(
                        org.springframework.web.bind.annotation.RestController.class));
    }
}
