package net.chrisrichardson.ftgo.resilience;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import net.chrisrichardson.ftgo.resilience.bulkhead.FtgoBulkheadConfiguration;
import net.chrisrichardson.ftgo.resilience.config.FtgoResilienceProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link FtgoBulkheadConfiguration}.
 */
class FtgoBulkheadConfigurationTest {

    private FtgoResilienceProperties properties;
    private FtgoBulkheadConfiguration configuration;

    @BeforeEach
    void setUp() {
        properties = new FtgoResilienceProperties();
        configuration = new FtgoBulkheadConfiguration();
    }

    @Test
    @DisplayName("Bulkhead config uses default values")
    void bulkheadConfigUsesDefaults() {
        BulkheadConfig config = configuration.ftgoBulkheadConfig(properties);

        assertThat(config.getMaxConcurrentCalls()).isEqualTo(25);
        assertThat(config.getMaxWaitDuration()).isEqualTo(Duration.ZERO);
    }

    @Test
    @DisplayName("Bulkhead config uses custom values")
    void bulkheadConfigUsesCustomValues() {
        FtgoResilienceProperties.BulkheadProperties bhProps = properties.getBulkhead();
        bhProps.setMaxConcurrentCalls(10);
        bhProps.setMaxWaitDuration(Duration.ofSeconds(1));

        BulkheadConfig config = configuration.ftgoBulkheadConfig(properties);

        assertThat(config.getMaxConcurrentCalls()).isEqualTo(10);
        assertThat(config.getMaxWaitDuration()).isEqualTo(Duration.ofSeconds(1));
    }

    @Test
    @DisplayName("Bulkhead registry pre-registers service bulkheads")
    void registryPreRegistersServiceBulkheads() {
        BulkheadConfig config = configuration.ftgoBulkheadConfig(properties);
        BulkheadRegistry registry = configuration.ftgoBulkheadRegistry(config);

        assertThat(registry.bulkhead("order-service")).isNotNull();
        assertThat(registry.bulkhead("consumer-service")).isNotNull();
        assertThat(registry.bulkhead("restaurant-service")).isNotNull();
        assertThat(registry.bulkhead("courier-service")).isNotNull();
    }

    @Test
    @DisplayName("Bulkhead rejects calls when limit is reached")
    void bulkheadRejectsWhenLimitReached() {
        properties.getBulkhead().setMaxConcurrentCalls(2);
        properties.getBulkhead().setMaxWaitDuration(Duration.ZERO);

        BulkheadConfig config = configuration.ftgoBulkheadConfig(properties);
        BulkheadRegistry registry = configuration.ftgoBulkheadRegistry(config);
        Bulkhead bulkhead = registry.bulkhead("test-service");

        // Acquire all permits
        bulkhead.acquirePermission();
        bulkhead.acquirePermission();

        // Next acquisition should fail
        assertThatThrownBy(bulkhead::acquirePermission)
                .isInstanceOf(BulkheadFullException.class);
    }
}
