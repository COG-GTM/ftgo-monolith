-- =============================================================================
-- Restaurant Service Database Schema - V1 Initial Migration
-- =============================================================================
-- Service: ftgo-restaurant-service
-- Database: ftgo_restaurant_service
-- Bounded Context: Restaurant
--
-- This migration creates the initial schema for the Restaurant Service's
-- dedicated database, extracted from the shared ftgo monolith database.
--
-- Original monolith tables: restaurants, restaurant_menu_items
--   (from V1__create_ftgo_db.sql)
--
-- Changes from monolith schema:
--   - Dedicated database (ftgo_restaurant_service) instead of shared ftgo
--   - Uses service-local AUTO_INCREMENT instead of shared hibernate_sequence
--   - restaurant_menu_items.restaurant_id FK RETAINED (same-service)
--   - No cross-service foreign keys existed for these tables
--
-- ID Generation Strategy:
--   AUTO_INCREMENT is used for new records created by this service.
--   During migration, existing IDs from the monolith are preserved to
--   maintain referential integrity across services via ID-based references.
-- =============================================================================

-- ---------------------------------------------------------------------------
-- Table: restaurants
-- ---------------------------------------------------------------------------
-- Core entity for the Restaurant bounded context.
-- Stores restaurant profile information including name and address.
--
-- Owner: ftgo-restaurant-service (sole writer)
-- Readers: Order Service references restaurant_id; accesses data via API
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS restaurants
(
    id      BIGINT       NOT NULL AUTO_INCREMENT,
    name    VARCHAR(255),
    street1 VARCHAR(255),
    street2 VARCHAR(255),
    city    VARCHAR(255),
    state   VARCHAR(255),
    zip     VARCHAR(255),
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------------
-- Table: restaurant_menu_items
-- ---------------------------------------------------------------------------
-- Menu items belonging to a restaurant.
-- The restaurant_id FK is RETAINED because both tables are in the same service.
--
-- Owner: ftgo-restaurant-service (sole writer, via Restaurant aggregate)
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS restaurant_menu_items
(
    restaurant_id BIGINT       NOT NULL,
    id            VARCHAR(255),
    name          VARCHAR(255),
    price         DECIMAL(19, 2),
    CONSTRAINT fk_restaurant_menu_items_restaurant
        FOREIGN KEY (restaurant_id) REFERENCES restaurants (id)
            ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------------
-- Indexes
-- ---------------------------------------------------------------------------
CREATE INDEX idx_restaurant_menu_items_restaurant_id ON restaurant_menu_items (restaurant_id);
