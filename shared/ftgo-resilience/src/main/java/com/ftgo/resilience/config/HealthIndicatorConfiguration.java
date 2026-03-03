package com.ftgo.resilience.config;

import com.ftgo.resilience.health.DatabaseHealthIndicator;
import com.ftgo.resilience.health.DependentServiceHealthIndicator;
import com.ftgo.resilience.health.DiskSpaceHealthIndicator;
import com.ftgo.resilience.health.ServiceBusinessHealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * Configuration for custom health indicators.
 * <p>
 * Registers health indicators for:
 * <ul>
 *   <li>Database connectivity and query execution</li>
 *   <li>Disk space availability</li>
 *   <li>Business-specific health checks</li>
 *   <li>Dependent service availability</li>
 * </ul>
 */
@Configuration
public class HealthIndicatorConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public DiskSpaceHealthIndicator ftgoDiskSpaceHealthIndicator() {
        return new DiskSpaceHealthIndicator();
    }

    @Bean
    @ConditionalOnMissingBean
    public ServiceBusinessHealthIndicator ftgoServiceBusinessHealthIndicator() {
        return new ServiceBusinessHealthIndicator();
    }

    @Bean
    @ConditionalOnMissingBean
    public DependentServiceHealthIndicator ftgoDependentServiceHealthIndicator() {
        return new DependentServiceHealthIndicator();
    }
}
