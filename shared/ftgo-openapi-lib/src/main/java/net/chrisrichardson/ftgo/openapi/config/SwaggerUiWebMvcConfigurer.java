package net.chrisrichardson.ftgo.openapi.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configures a redirect from {@code /swagger-ui.html} to the SpringDoc Swagger UI.
 *
 * <p>SpringDoc serves Swagger UI at {@code /swagger-ui/index.html} by default.
 * This configurer adds a redirect from the conventional {@code /swagger-ui.html}
 * path so that existing bookmarks and documentation references continue to work.</p>
 */
@Configuration
public class SwaggerUiWebMvcConfigurer implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addRedirectViewController("/swagger-ui.html", "/swagger-ui/index.html");
    }
}
