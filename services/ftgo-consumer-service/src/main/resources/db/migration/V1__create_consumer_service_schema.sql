-- =============================================================================
-- Consumer Service Database Schema (V1)
-- Database: ftgo_consumer_service
-- =============================================================================
-- This migration creates the initial schema for the Consumer Service database.
-- Extracted from the monolith's shared ftgo database (V1__create_ftgo_db.sql).
--
-- Changes from monolith:
--   - Dedicated database/schema: ftgo_consumer_service
--   - Per-service ID generation replaces shared hibernate_sequence
--   - No cross-service foreign keys
-- =============================================================================

-- ---------------------------------------------------------------------------
-- Table: consumers
-- Owner: Consumer Service (single source of truth)
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS consumers
(
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    first_name VARCHAR(255) NOT NULL,
    last_name  VARCHAR(255) NOT NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------------
-- Per-service sequence table for ID generation
-- Replaces the shared hibernate_sequence from the monolith
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS consumer_sequence
(
    sequence_name VARCHAR(255) NOT NULL,
    next_val      BIGINT       NOT NULL DEFAULT 1,
    PRIMARY KEY (sequence_name)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

INSERT INTO consumer_sequence (sequence_name, next_val) VALUES ('consumer_id_seq', 1);

-- ---------------------------------------------------------------------------
-- Indexes
-- ---------------------------------------------------------------------------
CREATE INDEX idx_consumers_last_name ON consumers (last_name);
