-- =============================================================================
-- Order Service Database Schema (V1)
-- Database: ftgo_order_service
-- =============================================================================
-- This migration creates the initial schema for the Order Service database.
-- Extracted from the monolith's shared ftgo database (V1__create_ftgo_db.sql).
--
-- Changes from monolith:
--   - Dedicated database/schema: ftgo_order_service
--   - Per-service ID generation replaces shared hibernate_sequence
--   - Cross-service FKs removed: orders.assigned_courier_id no longer references
--     courier(id); orders.restaurant_id no longer references restaurants(id)
--   - courier_id and restaurant_id retained as BIGINT columns for ID-based
--     cross-service references (resolved via API calls or events)
--   - consumer_id retained as BIGINT for cross-service reference
-- =============================================================================

-- ---------------------------------------------------------------------------
-- Table: orders
-- Owner: Order Service (single source of truth)
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS orders
(
    id                       BIGINT        NOT NULL AUTO_INCREMENT,
    order_state              VARCHAR(50)   NOT NULL DEFAULT 'APPROVAL_PENDING',
    consumer_id              BIGINT        NOT NULL COMMENT 'References consumer in Consumer Service (no FK)',
    restaurant_id            BIGINT        NOT NULL COMMENT 'References restaurant in Restaurant Service (no FK)',
    assigned_courier_id      BIGINT        NULL     COMMENT 'References courier in Courier Service (no FK)',
    delivery_address_street1 VARCHAR(255),
    delivery_address_street2 VARCHAR(255),
    delivery_address_city    VARCHAR(255),
    delivery_address_state   VARCHAR(255),
    delivery_address_zip     VARCHAR(255),
    delivery_time            DATETIME      NULL,
    order_minimum            DECIMAL(19, 2),
    payment_token            VARCHAR(255),
    accept_time              DATETIME      NULL,
    preparing_time           DATETIME      NULL,
    ready_for_pickup_time    DATETIME      NULL,
    picked_up_time           DATETIME      NULL,
    delivered_time           DATETIME      NULL,
    ready_by                 DATETIME      NULL,
    previous_ticket_state    INTEGER,
    version                  BIGINT        NOT NULL DEFAULT 0,
    created_at               TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at               TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------------
-- Table: order_line_items
-- Owner: Order Service (embedded within Order aggregate)
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS order_line_items
(
    id           BIGINT        NOT NULL AUTO_INCREMENT,
    order_id     BIGINT        NOT NULL,
    menu_item_id VARCHAR(255),
    name         VARCHAR(255),
    price        DECIMAL(19, 2),
    quantity     INTEGER       NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_order_line_items_order
        FOREIGN KEY (order_id) REFERENCES orders (id)
            ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------------
-- Per-service sequence table for ID generation
-- Replaces the shared hibernate_sequence from the monolith
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS order_sequence
(
    sequence_name VARCHAR(255) NOT NULL,
    next_val      BIGINT       NOT NULL DEFAULT 1,
    PRIMARY KEY (sequence_name)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

INSERT INTO order_sequence (sequence_name, next_val) VALUES ('order_id_seq', 1);

-- ---------------------------------------------------------------------------
-- Indexes for cross-service reference lookups
-- ---------------------------------------------------------------------------
CREATE INDEX idx_orders_consumer_id    ON orders (consumer_id);
CREATE INDEX idx_orders_restaurant_id  ON orders (restaurant_id);
CREATE INDEX idx_orders_courier_id     ON orders (assigned_courier_id);
CREATE INDEX idx_orders_state          ON orders (order_state);
CREATE INDEX idx_order_line_items_order ON order_line_items (order_id);
