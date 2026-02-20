package com.ftgo.api.standards.config;

import static org.assertj.core.api.Assertions.assertThat;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class FtgoOpenApiAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(FtgoOpenApiAutoConfiguration.class));

    @Test
    void autoConfigurationCreatesOpenAPIBean() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(OpenAPI.class);
            OpenAPI openAPI = context.getBean(OpenAPI.class);
            assertThat(openAPI.getInfo().getTitle()).isEqualTo("FTGO Service API");
            assertThat(openAPI.getInfo().getVersion()).isEqualTo("1.0.0");
        });
    }

    @Test
    void customPropertiesApplied() {
        contextRunner
                .withPropertyValues(
                        "ftgo.api.title=Order Service API",
                        "ftgo.api.description=Order management endpoints",
                        "ftgo.api.version=2.0.0")
                .run(context -> {
                    OpenAPI openAPI = context.getBean(OpenAPI.class);
                    assertThat(openAPI.getInfo().getTitle()).isEqualTo("Order Service API");
                    assertThat(openAPI.getInfo().getDescription()).isEqualTo("Order management endpoints");
                    assertThat(openAPI.getInfo().getVersion()).isEqualTo("2.0.0");
                });
    }

    @Test
    void securitySchemeConfigured() {
        contextRunner.run(context -> {
            OpenAPI openAPI = context.getBean(OpenAPI.class);
            assertThat(openAPI.getComponents().getSecuritySchemes()).containsKey("bearerAuth");
            assertThat(openAPI.getSecurity()).isNotEmpty();
        });
    }

    @Test
    void securityCanBeDisabled() {
        contextRunner
                .withPropertyValues("ftgo.api.security.enabled=false")
                .run(context -> {
                    OpenAPI openAPI = context.getBean(OpenAPI.class);
                    assertThat(openAPI.getComponents()).isNull();
                });
    }

    @Test
    void serverConfigured() {
        contextRunner
                .withPropertyValues(
                        "ftgo.api.server-url=https://api.ftgo.com",
                        "ftgo.api.server-description=Production")
                .run(context -> {
                    OpenAPI openAPI = context.getBean(OpenAPI.class);
                    assertThat(openAPI.getServers()).hasSize(1);
                    assertThat(openAPI.getServers().get(0).getUrl()).isEqualTo("https://api.ftgo.com");
                    assertThat(openAPI.getServers().get(0).getDescription()).isEqualTo("Production");
                });
    }

    @Test
    void existingOpenAPIBeanNotOverridden() {
        contextRunner
                .withBean(OpenAPI.class, () -> new OpenAPI().info(
                        new io.swagger.v3.oas.models.info.Info().title("Custom")))
                .run(context -> {
                    assertThat(context).hasSingleBean(OpenAPI.class);
                    OpenAPI openAPI = context.getBean(OpenAPI.class);
                    assertThat(openAPI.getInfo().getTitle()).isEqualTo("Custom");
                });
    }
}
