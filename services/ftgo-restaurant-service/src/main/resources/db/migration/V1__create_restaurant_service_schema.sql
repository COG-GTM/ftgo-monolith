-- ============================================================
-- Restaurant Service - Initial Database Schema (V1)
-- ============================================================
-- Migrated from monolith ftgo.restaurants and ftgo.restaurant_menu_items tables.
-- This service owns the Restaurant bounded context.
--
-- Changes from monolith:
--   - Dedicated database: ftgo_restaurant_service
--   - Per-service ID sequence (replaces shared hibernate_sequence)
--   - restaurant_menu_items FK retained (within same service boundary)
-- ============================================================

-- Per-service ID generation sequence (replaces shared hibernate_sequence)
CREATE TABLE restaurant_id_sequence (
    next_val BIGINT NOT NULL
) ENGINE = InnoDB;

INSERT INTO restaurant_id_sequence VALUES (1000);

-- Restaurant entity
CREATE TABLE restaurants (
    id      BIGINT       NOT NULL AUTO_INCREMENT,
    name    VARCHAR(255),
    street1 VARCHAR(255),
    street2 VARCHAR(255),
    city    VARCHAR(255),
    state   VARCHAR(255),
    zip     VARCHAR(255),
    PRIMARY KEY (id)
) ENGINE = InnoDB;

-- Restaurant menu items (part of Restaurant aggregate)
CREATE TABLE restaurant_menu_items (
    restaurant_id BIGINT         NOT NULL,
    id            VARCHAR(255),
    name          VARCHAR(255),
    price         DECIMAL(19, 2)
) ENGINE = InnoDB;

-- Intra-service FK: restaurant_menu_items -> restaurants (same bounded context)
ALTER TABLE restaurant_menu_items
    ADD CONSTRAINT fk_restaurant_menu_items_restaurant_id
        FOREIGN KEY (restaurant_id) REFERENCES restaurants (id);

-- Indexes for common query patterns
CREATE INDEX idx_restaurants_name ON restaurants (name);
CREATE INDEX idx_restaurant_menu_items_restaurant_id ON restaurant_menu_items (restaurant_id);
