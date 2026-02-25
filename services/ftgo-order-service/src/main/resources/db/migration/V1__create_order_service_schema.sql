-- ============================================================
-- Order Service - Initial Database Schema (V1)
-- ============================================================
-- Migrated from monolith ftgo.orders and ftgo.order_line_items tables.
-- This service owns the Order bounded context.
--
-- Changes from monolith:
--   - Dedicated database: ftgo_order_service
--   - Per-service ID sequence (replaces shared hibernate_sequence)
--   - orders.restaurant_id stored as reference ID (no FK to restaurants table)
--   - orders.assigned_courier_id stored as reference ID (no FK to courier table)
--   - orders.consumer_id stored as reference ID (no FK to consumers table)
--   - order_line_items FK retained (within same service boundary)
-- ============================================================

-- Per-service ID generation sequence (replaces shared hibernate_sequence)
CREATE TABLE order_id_sequence (
    next_val BIGINT NOT NULL
) ENGINE = InnoDB;

INSERT INTO order_id_sequence VALUES (1000);

-- Order entity
-- Cross-service IDs (consumer_id, restaurant_id, assigned_courier_id) are stored
-- as plain BIGINT columns without FK constraints. Referential integrity is
-- maintained through domain events and eventual consistency.
CREATE TABLE orders (
    id                       BIGINT       NOT NULL AUTO_INCREMENT,
    accept_time              DATETIME,
    consumer_id              BIGINT       COMMENT 'Reference to Consumer Service (no FK - cross-service)',
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
    assigned_courier_id      BIGINT       COMMENT 'Reference to Courier Service (no FK - cross-service)',
    restaurant_id            BIGINT       COMMENT 'Reference to Restaurant Service (no FK - cross-service)',
    PRIMARY KEY (id)
) ENGINE = InnoDB;

-- Order line items (part of Order aggregate)
CREATE TABLE order_line_items (
    order_id     BIGINT         NOT NULL,
    menu_item_id VARCHAR(255),
    name         VARCHAR(255),
    price        DECIMAL(19, 2),
    quantity     INTEGER        NOT NULL
) ENGINE = InnoDB;

-- Intra-service FK: order_line_items -> orders (same bounded context)
ALTER TABLE order_line_items
    ADD CONSTRAINT fk_order_line_items_order_id
        FOREIGN KEY (order_id) REFERENCES orders (id);

-- Indexes for common query patterns
CREATE INDEX idx_orders_consumer_id ON orders (consumer_id);
CREATE INDEX idx_orders_restaurant_id ON orders (restaurant_id);
CREATE INDEX idx_orders_assigned_courier_id ON orders (assigned_courier_id);
CREATE INDEX idx_orders_order_state ON orders (order_state);
CREATE INDEX idx_order_line_items_order_id ON order_line_items (order_id);
