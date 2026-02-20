package com.ftgo.api.standards.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.ftgo.api.standards.exception.GlobalExceptionHandler;
import com.ftgo.api.standards.filter.CorrelationIdFilter;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.boot.web.servlet.FilterRegistrationBean;

class FtgoErrorHandlingAutoConfigurationTest {

    private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(FtgoErrorHandlingAutoConfiguration.class));

    @Test
    void registersGlobalExceptionHandler() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(GlobalExceptionHandler.class);
        });
    }

    @Test
    void registersCorrelationIdFilter() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(CorrelationIdFilter.class);
        });
    }

    @Test
    void registersFilterRegistrationBean() {
        contextRunner.run(context -> {
            assertThat(context).hasBean("correlationIdFilterRegistration");
            FilterRegistrationBean<?> registration = context.getBean(
                    "correlationIdFilterRegistration", FilterRegistrationBean.class);
            assertThat(registration.getOrder()).isEqualTo(Integer.MIN_VALUE);
        });
    }

    @Test
    void doesNotOverrideCustomExceptionHandler() {
        contextRunner
                .withBean("globalExceptionHandler", GlobalExceptionHandler.class, GlobalExceptionHandler::new)
                .run(context -> {
                    assertThat(context).hasSingleBean(GlobalExceptionHandler.class);
                });
    }

    @Test
    void doesNotOverrideCustomCorrelationIdFilter() {
        contextRunner
                .withBean("correlationIdFilter", CorrelationIdFilter.class, CorrelationIdFilter::new)
                .run(context -> {
                    assertThat(context).hasSingleBean(CorrelationIdFilter.class);
                });
    }
}
