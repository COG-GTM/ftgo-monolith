package com.ftgo.common.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.config.MeterFilter;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;

/**
 * Applies common tags to all metrics for consistent labeling across FTGO services.
 *
 * <p>Tags applied:</p>
 * <ul>
 *   <li>{@code application} - The Spring application name</li>
 *   <li>{@code platform} - Always "ftgo"</li>
 * </ul>
 */
public class FtgoCommonTagsCustomizer implements MeterRegistryCustomizer<MeterRegistry> {

    private final String applicationName;

    public FtgoCommonTagsCustomizer(String applicationName) {
        this.applicationName = applicationName;
    }

    @Override
    public void customize(MeterRegistry registry) {
        registry.config()
                .commonTags("application", applicationName)
                .commonTags("platform", "ftgo")
                .meterFilter(MeterFilter.deny(id ->
                        id.getName().startsWith("jvm.") && id.getTag("application") == null));
    }
}
