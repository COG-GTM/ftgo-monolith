-- =============================================================================
-- Courier Service Database Schema - V1 Initial Migration
-- =============================================================================
-- Service: ftgo-courier-service
-- Database: ftgo_courier_service
-- Bounded Context: Courier
--
-- This migration creates the initial schema for the Courier Service's
-- dedicated database, extracted from the shared ftgo monolith database.
--
-- Original monolith tables: courier, courier_actions (from V1__create_ftgo_db.sql)
--
-- Changes from monolith schema:
--   - Dedicated database (ftgo_courier_service) instead of shared ftgo
--   - Uses service-local AUTO_INCREMENT instead of shared hibernate_sequence
--   - courier_actions.order_id FK to orders table REMOVED (cross-service)
--     Replaced with a plain BIGINT column; consistency via Courier Service API
--   - courier_actions.courier_id FK RETAINED (same-service relationship)
--
-- ID Generation Strategy:
--   AUTO_INCREMENT is used for new records created by this service.
--   During migration, existing IDs from the monolith are preserved to
--   maintain referential integrity across services via ID-based references.
-- =============================================================================

-- ---------------------------------------------------------------------------
-- Table: courier
-- ---------------------------------------------------------------------------
-- Core entity for the Courier bounded context.
-- Stores courier profile, availability status, and address information.
--
-- Owner: ftgo-courier-service (sole writer)
-- Readers: Order Service references courier_id; accesses data via API
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS courier
(
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    available  BIT,
    first_name VARCHAR(255),
    last_name  VARCHAR(255),
    street1    VARCHAR(255),
    street2    VARCHAR(255),
    city       VARCHAR(255),
    state      VARCHAR(255),
    zip        VARCHAR(255),
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------------
-- Table: courier_actions
-- ---------------------------------------------------------------------------
-- Records pickup and dropoff actions in a courier's delivery plan.
-- Each action references a courier (local FK) and an order (cross-service ID).
--
-- Cross-service reference: order_id is a plain BIGINT (no FK constraint).
-- The Order Service owns the orders table; this service stores only the ID
-- and resolves order details via the Order Service API when needed.
--
-- Owner: ftgo-courier-service (sole writer)
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS courier_actions
(
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    courier_id BIGINT       NOT NULL,
    order_id   BIGINT,
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
-- Indexes
-- ---------------------------------------------------------------------------
CREATE INDEX idx_courier_actions_courier_id ON courier_actions (courier_id);
CREATE INDEX idx_courier_actions_order_id ON courier_actions (order_id);
CREATE INDEX idx_courier_available ON courier (available);
