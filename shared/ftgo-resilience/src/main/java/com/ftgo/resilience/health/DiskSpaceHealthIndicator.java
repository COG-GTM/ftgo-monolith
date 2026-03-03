package com.ftgo.resilience.health;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

import java.io.File;

/**
 * Custom health indicator for disk space availability.
 * <p>
 * Checks that the application has sufficient disk space to operate.
 * Reports DOWN when free disk space falls below the configured threshold.
 * <p>
 * Default threshold: 100 MB.
 * <p>
 * This indicator is registered under the name "ftgoDiskSpace" in the health endpoint.
 */
public class DiskSpaceHealthIndicator implements HealthIndicator {

    private static final Logger log = LoggerFactory.getLogger(DiskSpaceHealthIndicator.class);

    /** Default minimum free disk space threshold in bytes (100 MB). */
    private static final long DEFAULT_THRESHOLD_BYTES = 100L * 1024 * 1024;

    private final long thresholdBytes;
    private final String path;

    public DiskSpaceHealthIndicator() {
        this(DEFAULT_THRESHOLD_BYTES, ".");
    }

    public DiskSpaceHealthIndicator(long thresholdBytes, String path) {
        this.thresholdBytes = thresholdBytes;
        this.path = path;
    }

    @Override
    public Health health() {
        try {
            File diskPartition = new File(path);
            long freeSpace = diskPartition.getFreeSpace();
            long totalSpace = diskPartition.getTotalSpace();
            long usableSpace = diskPartition.getUsableSpace();

            Health.Builder builder = (freeSpace >= thresholdBytes)
                    ? Health.up()
                    : Health.down();

            return builder
                    .withDetail("total", formatBytes(totalSpace))
                    .withDetail("free", formatBytes(freeSpace))
                    .withDetail("usable", formatBytes(usableSpace))
                    .withDetail("threshold", formatBytes(thresholdBytes))
                    .withDetail("path", diskPartition.getAbsolutePath())
                    .build();
        } catch (Exception e) {
            log.error("Disk space health check failed", e);
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }

    private String formatBytes(long bytes) {
        if (bytes >= 1073741824) {
            return String.format("%.2f GB", bytes / 1073741824.0);
        } else if (bytes >= 1048576) {
            return String.format("%.2f MB", bytes / 1048576.0);
        } else if (bytes >= 1024) {
            return String.format("%.2f KB", bytes / 1024.0);
        }
        return bytes + " B";
    }
}
