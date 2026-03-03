-- =============================================================================
-- Order Service Database Schema - V1 Initial Migration
-- =============================================================================
-- Service: ftgo-order-service
-- Database: ftgo_order_service
-- Bounded Context: Order
--
-- This migration creates the initial schema for the Order Service's
-- dedicated database, extracted from the shared ftgo monolith database.
--
-- Original monolith tables: orders, order_line_items (from V1__create_ftgo_db.sql)
--
-- Changes from monolith schema:
--   - Dedicated database (ftgo_order_service) instead of shared ftgo
--   - Uses service-local AUTO_INCREMENT instead of shared hibernate_sequence
--   - orders.assigned_courier_id FK to courier table REMOVED (cross-service)
--     Replaced with plain BIGINT column; consistency via Courier Service API
--   - orders.restaurant_id FK to restaurants table REMOVED (cross-service)
--     Replaced with plain BIGINT column; consistency via Restaurant Service API
--   - orders.consumer_id was already a plain BIGINT (no FK in monolith)
--   - order_line_items.order_id FK RETAINED (same-service relationship)
--
-- ID Generation Strategy:
--   AUTO_INCREMENT is used for new records created by this service.
--   During migration, existing IDs from the monolith are preserved to
--   maintain referential integrity across services via ID-based references.
-- =============================================================================

-- ---------------------------------------------------------------------------
-- Table: orders
-- ---------------------------------------------------------------------------
-- Core entity for the Order bounded context.
-- Manages the full order lifecycle from creation through delivery.
--
-- Cross-service references (plain BIGINT, no FK constraints):
--   - consumer_id    -> Consumer Service owns the consumer record
--   - restaurant_id  -> Restaurant Service owns the restaurant record
--   - assigned_courier_id -> Courier Service owns the courier record
--
-- Owner: ftgo-order-service (sole writer)
-- Readers: Other services access order data via Order Service API
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS orders
(
    id                       BIGINT       NOT NULL AUTO_INCREMENT,
    accept_time              DATETIME,
    consumer_id              BIGINT,
    delivery_address_city    VARCHAR(255),
    delivery_address_state   VARCHAR(255),
    delivery_address_street1 VARCHAR(255),
    delivery_address_street2 VARCHAR(255),
    delivery_address_zip     VARCHAR(255),
    delivery_time            DATETIME,
    order_state              VARCHAR(255),
    order_minimum            DECIMAL(19, 2),
    payment_token            VARCHAR(255),
    picked_up_time           DATETIME,
    delivered_time           DATETIME,
    preparing_time           DATETIME,
    previous_ticket_state    INTEGER,
    ready_by                 DATETIME,
    ready_for_pickup_time    DATETIME,
    version                  BIGINT,
    assigned_courier_id      BIGINT,
    restaurant_id            BIGINT,
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------------
-- Table: order_line_items
-- ---------------------------------------------------------------------------
-- Embedded collection of line items within an order.
-- Each line item references a menu item (by ID/name) from the restaurant's
-- menu at the time the order was placed.
--
-- The order_id FK is RETAINED because both tables are in the same service.
--
-- Owner: ftgo-order-service (sole writer, via Order aggregate)
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS order_line_items
(
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    order_id     BIGINT       NOT NULL,
    menu_item_id VARCHAR(255),
    name         VARCHAR(255),
    price        DECIMAL(19, 2),
    quantity     INTEGER      NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_order_line_items_order
        FOREIGN KEY (order_id) REFERENCES orders (id)
            ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------------
-- Indexes
-- ---------------------------------------------------------------------------
CREATE INDEX idx_orders_consumer_id ON orders (consumer_id);
CREATE INDEX idx_orders_restaurant_id ON orders (restaurant_id);
CREATE INDEX idx_orders_assigned_courier_id ON orders (assigned_courier_id);
CREATE INDEX idx_orders_order_state ON orders (order_state);
CREATE INDEX idx_order_line_items_order_id ON order_line_items (order_id);
