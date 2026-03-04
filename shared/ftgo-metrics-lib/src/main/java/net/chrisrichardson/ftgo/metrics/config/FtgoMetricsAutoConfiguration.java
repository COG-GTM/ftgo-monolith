package net.chrisrichardson.ftgo.metrics.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.core.instrument.binder.system.UptimeMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * Auto-configuration for FTGO metrics infrastructure.
 *
 * <p>Configures:
 * <ul>
 *   <li>Common meter registry tags (application name, environment)</li>
 *   <li>JVM metrics (memory, GC, threads, classloaders)</li>
 *   <li>System metrics (CPU, uptime)</li>
 * </ul>
 *
 * <p>Services that include this library automatically get Prometheus-compatible
 * metrics exposed at {@code /actuator/prometheus}.
 */
@Configuration
@ConditionalOnClass(MeterRegistry.class)
public class FtgoMetricsAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(FtgoMetricsAutoConfiguration.class);

    /**
     * Customizes the MeterRegistry with common tags for all metrics.
     * Tags include the application name and active Spring profiles.
     */
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> ftgoCommonTags(Environment environment) {
        return registry -> {
            String appName = environment.getProperty("spring.application.name", "unknown");
            String[] profiles = environment.getActiveProfiles();
            String env = profiles.length > 0 ? profiles[0] : "default";

            registry.config()
                    .commonTags("application", appName)
                    .commonTags("environment", env);

            log.info("FTGO Metrics configured for application='{}', environment='{}'", appName, env);
        };
    }

    /**
     * Registers JVM memory metrics (heap and non-heap usage).
     */
    @Bean
    @ConditionalOnMissingBean(JvmMemoryMetrics.class)
    public JvmMemoryMetrics jvmMemoryMetrics() {
        return new JvmMemoryMetrics();
    }

    /**
     * Registers JVM garbage collection metrics.
     */
    @Bean
    @ConditionalOnMissingBean(JvmGcMetrics.class)
    public JvmGcMetrics jvmGcMetrics() {
        return new JvmGcMetrics();
    }

    /**
     * Registers JVM thread metrics.
     */
    @Bean
    @ConditionalOnMissingBean(JvmThreadMetrics.class)
    public JvmThreadMetrics jvmThreadMetrics() {
        return new JvmThreadMetrics();
    }

    /**
     * Registers class loader metrics.
     */
    @Bean
    @ConditionalOnMissingBean(ClassLoaderMetrics.class)
    public ClassLoaderMetrics classLoaderMetrics() {
        return new ClassLoaderMetrics();
    }

    /**
     * Registers CPU and processor metrics.
     */
    @Bean
    @ConditionalOnMissingBean(ProcessorMetrics.class)
    public ProcessorMetrics processorMetrics() {
        return new ProcessorMetrics();
    }

    /**
     * Registers uptime metrics.
     */
    @Bean
    @ConditionalOnMissingBean(UptimeMetrics.class)
    public UptimeMetrics uptimeMetrics() {
        return new UptimeMetrics();
    }
}
