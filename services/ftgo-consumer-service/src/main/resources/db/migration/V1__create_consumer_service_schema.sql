-- ============================================================
-- Consumer Service - Initial Database Schema (V1)
-- ============================================================
-- Migrated from monolith ftgo.consumers table.
-- This service owns the Consumer bounded context.
--
-- Changes from monolith:
--   - Dedicated database: ftgo_consumer_service
--   - Per-service ID sequence (replaces shared hibernate_sequence)
--   - No cross-service foreign keys
-- ============================================================

-- Per-service ID generation sequence (replaces shared hibernate_sequence)
CREATE TABLE consumer_id_sequence (
    next_val BIGINT NOT NULL
) ENGINE = InnoDB;

INSERT INTO consumer_id_sequence VALUES (1000);

-- Consumer entity
CREATE TABLE consumers (
    id         BIGINT       NOT NULL,
    first_name VARCHAR(255),
    last_name  VARCHAR(255),
    PRIMARY KEY (id)
) ENGINE = InnoDB;

-- Indexes for common query patterns
CREATE INDEX idx_consumers_last_name ON consumers (last_name);
