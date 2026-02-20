package com.ftgo.api.standards.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass(OpenAPI.class)
@EnableConfigurationProperties(FtgoApiProperties.class)
public class FtgoOpenApiAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public OpenAPI ftgoOpenAPI(FtgoApiProperties properties) {
        OpenAPI openAPI = new OpenAPI()
                .info(buildInfo(properties))
                .servers(buildServers(properties));

        if (properties.getSecurity().isEnabled()) {
            String schemeName = properties.getSecurity().getSchemeName();
            openAPI.components(new Components()
                    .addSecuritySchemes(schemeName, new SecurityScheme()
                            .type(SecurityScheme.Type.HTTP)
                            .scheme(properties.getSecurity().getScheme())
                            .bearerFormat(properties.getSecurity().getBearerFormat())));
            openAPI.addSecurityItem(new SecurityRequirement().addList(schemeName));
        }

        return openAPI;
    }

    private Info buildInfo(FtgoApiProperties properties) {
        return new Info()
                .title(properties.getTitle())
                .description(properties.getDescription())
                .version(properties.getVersion())
                .contact(new Contact()
                        .name(properties.getContactName())
                        .email(properties.getContactEmail())
                        .url(properties.getContactUrl()))
                .license(new License()
                        .name(properties.getLicenseName())
                        .url(properties.getLicenseUrl()));
    }

    private List<Server> buildServers(FtgoApiProperties properties) {
        Server server = new Server()
                .url(properties.getServerUrl())
                .description(properties.getServerDescription());
        return List.of(server);
    }
}
