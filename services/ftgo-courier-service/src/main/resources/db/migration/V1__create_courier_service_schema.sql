-- =============================================================================
-- Courier Service Database Schema (V1)
-- Database: ftgo_courier_service
-- =============================================================================
-- This migration creates the initial schema for the Courier Service database.
-- Extracted from the monolith's shared ftgo database (V1__create_ftgo_db.sql).
--
-- Changes from monolith:
--   - Dedicated database/schema: ftgo_courier_service
--   - Per-service ID generation replaces shared hibernate_sequence
--   - Cross-service FKs removed: courier_actions.order_id no longer references
--     orders(id) from the Order Service
--   - order_id retained as BIGINT for cross-service reference (resolved via
--     API calls or events)
-- =============================================================================

-- ---------------------------------------------------------------------------
-- Table: courier
-- Owner: Courier Service (single source of truth)
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS courier
(
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    available  BIT          NOT NULL DEFAULT 0,
    first_name VARCHAR(255) NOT NULL,
    last_name  VARCHAR(255) NOT NULL,
    street1    VARCHAR(255),
    street2    VARCHAR(255),
    city       VARCHAR(255),
    state      VARCHAR(255),
    zip        VARCHAR(255),
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------------
-- Table: courier_actions
-- Owner: Courier Service (part of Courier aggregate / delivery plan)
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS courier_actions
(
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    courier_id BIGINT       NOT NULL,
    order_id   BIGINT       NULL     COMMENT 'References order in Order Service (no FK)',
    time       DATETIME,
    type       VARCHAR(255),
    PRIMARY KEY (id),
    CONSTRAINT fk_courier_actions_courier
        FOREIGN KEY (courier_id) REFERENCES courier (id)
            ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------------
-- Per-service sequence table for ID generation
-- Replaces the shared hibernate_sequence from the monolith
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS courier_sequence
(
    sequence_name VARCHAR(255) NOT NULL,
    next_val      BIGINT       NOT NULL DEFAULT 1,
    PRIMARY KEY (sequence_name)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

INSERT INTO courier_sequence (sequence_name, next_val) VALUES ('courier_id_seq', 1);

-- ---------------------------------------------------------------------------
-- Indexes
-- ---------------------------------------------------------------------------
CREATE INDEX idx_courier_available      ON courier (available);
CREATE INDEX idx_courier_actions_courier ON courier_actions (courier_id);
CREATE INDEX idx_courier_actions_order   ON courier_actions (order_id);
