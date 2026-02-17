package net.chrisrichardson.ftgo.observability.metrics;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.config.NamingConvention;

public class FtgoMetricsNamingConvention implements NamingConvention {

    private static final String PREFIX = "ftgo";

    @Override
    public String name(String name, Meter.Type type, String baseUnit) {
        if (name.startsWith(PREFIX)) {
            return NamingConvention.snakeCase.name(name, type, baseUnit);
        }
        return NamingConvention.snakeCase.name(name, type, baseUnit);
    }

    @Override
    public String tagKey(String key) {
        return NamingConvention.snakeCase.tagKey(key);
    }

    @Override
    public String tagValue(String value) {
        return NamingConvention.snakeCase.tagValue(value);
    }
}
