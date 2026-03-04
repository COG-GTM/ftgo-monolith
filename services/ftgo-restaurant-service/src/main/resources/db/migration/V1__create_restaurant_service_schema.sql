-- =============================================================================
-- Restaurant Service Database Schema (V1)
-- Database: ftgo_restaurant_service
-- =============================================================================
-- This migration creates the initial schema for the Restaurant Service database.
-- Extracted from the monolith's shared ftgo database (V1__create_ftgo_db.sql).
--
-- Changes from monolith:
--   - Dedicated database/schema: ftgo_restaurant_service
--   - Per-service ID generation replaces shared hibernate_sequence
--   - No cross-service foreign keys (restaurant_menu_items references only
--     the local restaurants table)
-- =============================================================================

-- ---------------------------------------------------------------------------
-- Table: restaurants
-- Owner: Restaurant Service (single source of truth)
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS restaurants
(
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    name       VARCHAR(255) NOT NULL,
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
-- Table: restaurant_menu_items
-- Owner: Restaurant Service (part of Restaurant aggregate)
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS restaurant_menu_items
(
    id            BIGINT         NOT NULL AUTO_INCREMENT,
    restaurant_id BIGINT         NOT NULL,
    menu_item_id  VARCHAR(255),
    name          VARCHAR(255),
    price         DECIMAL(19, 2),
    PRIMARY KEY (id),
    CONSTRAINT fk_menu_items_restaurant
        FOREIGN KEY (restaurant_id) REFERENCES restaurants (id)
            ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------------
-- Per-service sequence table for ID generation
-- Replaces the shared hibernate_sequence from the monolith
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS restaurant_sequence
(
    sequence_name VARCHAR(255) NOT NULL,
    next_val      BIGINT       NOT NULL DEFAULT 1,
    PRIMARY KEY (sequence_name)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

INSERT INTO restaurant_sequence (sequence_name, next_val) VALUES ('restaurant_id_seq', 1);

-- ---------------------------------------------------------------------------
-- Indexes
-- ---------------------------------------------------------------------------
CREATE INDEX idx_restaurants_name              ON restaurants (name);
CREATE INDEX idx_menu_items_restaurant         ON restaurant_menu_items (restaurant_id);
