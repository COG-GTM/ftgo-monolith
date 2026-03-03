package com.ftgo.logging;

import java.util.UUID;

/**
 * Generates unique correlation IDs for request tracing across services.
 * Uses UUID v4 for globally unique, collision-resistant identifiers.
 */
public final class CorrelationIdGenerator {

    private CorrelationIdGenerator() {
        // Utility class - prevent instantiation
    }

    /**
     * Generates a new correlation ID.
     *
     * @return a new UUID-based correlation ID string
     */
    public static String generate() {
        return UUID.randomUUID().toString();
    }
}
