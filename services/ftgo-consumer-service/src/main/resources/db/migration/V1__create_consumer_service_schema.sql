-- =============================================================================
-- Consumer Service Database Schema - V1 Initial Migration
-- =============================================================================
-- Service: ftgo-consumer-service
-- Database: ftgo_consumer_service
-- Bounded Context: Consumer
--
-- This migration creates the initial schema for the Consumer Service's
-- dedicated database, extracted from the shared ftgo monolith database.
--
-- Original monolith table: consumers (from V1__create_ftgo_db.sql)
--
-- Changes from monolith schema:
--   - Dedicated database (ftgo_consumer_service) instead of shared ftgo
--   - Uses service-local AUTO_INCREMENT instead of shared hibernate_sequence
--   - No cross-service foreign keys
--
-- ID Generation Strategy:
--   AUTO_INCREMENT is used for new records created by this service.
--   During migration, existing IDs from the monolith are preserved to
--   maintain referential integrity across services via ID-based references.
-- =============================================================================

-- ---------------------------------------------------------------------------
-- Table: consumers
-- ---------------------------------------------------------------------------
-- Core entity for the Consumer bounded context.
-- Stores customer profile information including name.
--
-- Owner: ftgo-consumer-service (sole writer)
-- Readers: Other services access consumer data via Consumer Service API
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS consumers
(
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    first_name VARCHAR(255),
    last_name  VARCHAR(255),
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------------
-- Flyway Schema History
-- ---------------------------------------------------------------------------
-- Flyway automatically creates the flyway_schema_history table in this
-- database to track migration versions independently from other services.
-- Each service maintains its own migration version history.
-- ---------------------------------------------------------------------------
