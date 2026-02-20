package com.ftgo.jwt;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class JwtAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(JwtAutoConfiguration.class));

    @Test
    void shouldCreateBeansWhenSecretIsConfigured() {
        contextRunner
                .withPropertyValues(
                        "ftgo.jwt.secret=this-is-a-very-secure-secret-key-for-testing-jwt-operations-256bit",
                        "ftgo.jwt.issuer=ftgo-test"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(JwtTokenProvider.class);
                    assertThat(context).hasSingleBean(JwtTokenRefreshService.class);
                    assertThat(context).hasSingleBean(JwtAuthenticationFilter.class);
                    assertThat(context).hasSingleBean(JwtProperties.class);
                });
    }

    @Test
    void shouldNotCreateBeansWhenSecretIsMissing() {
        contextRunner
                .run(context -> {
                    assertThat(context).doesNotHaveBean(JwtTokenProvider.class);
                    assertThat(context).doesNotHaveBean(JwtTokenRefreshService.class);
                    assertThat(context).doesNotHaveBean(JwtAuthenticationFilter.class);
                });
    }

    @Test
    void shouldRespectCustomProperties() {
        contextRunner
                .withPropertyValues(
                        "ftgo.jwt.secret=this-is-a-very-secure-secret-key-for-testing-jwt-operations-256bit",
                        "ftgo.jwt.expiration=7200000",
                        "ftgo.jwt.refresh-threshold=600000",
                        "ftgo.jwt.issuer=custom-issuer",
                        "ftgo.jwt.header=X-Auth-Token",
                        "ftgo.jwt.prefix=Token "
                )
                .run(context -> {
                    JwtProperties properties = context.getBean(JwtProperties.class);
                    assertThat(properties.getExpiration()).isEqualTo(7200000);
                    assertThat(properties.getRefreshThreshold()).isEqualTo(600000);
                    assertThat(properties.getIssuer()).isEqualTo("custom-issuer");
                    assertThat(properties.getHeader()).isEqualTo("X-Auth-Token");
                    assertThat(properties.getPrefix()).isEqualTo("Token ");
                });
    }

    @Test
    void shouldNotOverrideExistingBeans() {
        contextRunner
                .withPropertyValues(
                        "ftgo.jwt.secret=this-is-a-very-secure-secret-key-for-testing-jwt-operations-256bit"
                )
                .withBean(JwtTokenProvider.class, () -> {
                    JwtProperties props = new JwtProperties();
                    props.setSecret("this-is-a-very-secure-secret-key-for-testing-jwt-operations-256bit");
                    props.setIssuer("custom");
                    return new JwtTokenProvider(props);
                })
                .run(context -> {
                    assertThat(context).hasSingleBean(JwtTokenProvider.class);
                    JwtTokenProvider provider = context.getBean(JwtTokenProvider.class);
                    assertThat(provider.getIssuer()).isEqualTo("custom");
                });
    }
}
