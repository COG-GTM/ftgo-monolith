-- =============================================================================
-- Order Service Schema Migration V1
-- =============================================================================
-- Extracted from the monolith's shared ftgo database.
--
-- Key changes from the monolith schema:
--   1. Removed cross-service foreign keys (consumer_id, restaurant_id,
--      assigned_courier_id) -- these are now enforced via eventual consistency.
--   2. Uses a per-service ID sequence instead of the shared hibernate_sequence.
--   3. Indexes added on columns that reference entities in other services
--      to support efficient lookups without FK constraints.
-- =============================================================================

-- Per-service ID sequence replaces the shared hibernate_sequence.
-- Each service owns its own sequence to avoid cross-service coordination.
CREATE TABLE order_service_sequence
(
    next_val BIGINT NOT NULL
) ENGINE = InnoDB;

INSERT INTO order_service_sequence VALUES (1);

-- -------------------------------------------------------------------
-- orders table
-- -------------------------------------------------------------------
CREATE TABLE orders
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
) ENGINE = InnoDB;

-- -------------------------------------------------------------------
-- order_line_items table (collection table owned by orders)
-- -------------------------------------------------------------------
CREATE TABLE order_line_items
(
    order_id     BIGINT       NOT NULL,
    menu_item_id VARCHAR(255),
    name         VARCHAR(255),
    price        DECIMAL(19, 2),
    quantity     INTEGER      NOT NULL,
    CONSTRAINT fk_order_line_items_order_id
        FOREIGN KEY (order_id) REFERENCES orders (id)
) ENGINE = InnoDB;

-- -------------------------------------------------------------------
-- Indexes for cross-service reference columns (no FK, soft references)
-- -------------------------------------------------------------------
-- consumer_id, restaurant_id, and assigned_courier_id reference entities
-- in other services. We keep indexes for query performance but do NOT
-- add foreign key constraints -- consistency is enforced at the
-- application level via eventual consistency patterns.
CREATE INDEX idx_orders_consumer_id        ON orders (consumer_id);
CREATE INDEX idx_orders_restaurant_id      ON orders (restaurant_id);
CREATE INDEX idx_orders_assigned_courier_id ON orders (assigned_courier_id);
CREATE INDEX idx_orders_order_state        ON orders (order_state);
