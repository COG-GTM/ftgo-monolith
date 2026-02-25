package com.ftgo.common.jpa.id;

/**
 * Defines the ID generation strategies available for FTGO microservices.
 *
 * <p>During migration from the shared monolith database, each service transitions
 * from the shared {@code hibernate_sequence} table to a per-service ID generation
 * strategy. This enum documents the supported strategies.</p>
 *
 * <h3>Migration Path</h3>
 * <ol>
 *   <li><strong>Phase 1 (Current)</strong>: Each service uses its own sequence table
 *       (e.g., {@code order_id_sequence}) with TABLE strategy</li>
 *   <li><strong>Phase 2 (Future)</strong>: Migrate to IDENTITY (AUTO_INCREMENT) for
 *       new entities, keeping TABLE for migrated entities with existing IDs</li>
 *   <li><strong>Phase 3 (Optional)</strong>: Consider UUID-based IDs for new entities
 *       to eliminate coordination requirements in distributed systems</li>
 * </ol>
 */
public enum IdGenerationStrategy {

    /**
     * Database AUTO_INCREMENT / IDENTITY column.
     * Simple and performant for single-database services.
     * Recommended for new entities created after migration.
     */
    IDENTITY,

    /**
     * Per-service sequence table (e.g., {@code order_id_sequence}).
     * Used during migration to maintain compatibility with existing IDs.
     * Each service has its own sequence table with non-overlapping ranges.
     */
    TABLE_SEQUENCE,

    /**
     * UUID-based identifiers.
     * Globally unique without coordination. Recommended for entities
     * that may need cross-service identity or eventual migration to
     * distributed databases.
     */
    UUID
}
