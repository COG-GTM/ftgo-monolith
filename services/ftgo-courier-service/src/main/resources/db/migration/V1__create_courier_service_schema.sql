-- ============================================================
-- Courier Service - Initial Database Schema (V1)
-- ============================================================
-- Migrated from monolith ftgo.courier and ftgo.courier_actions tables.
-- This service owns the Courier bounded context.
--
-- Changes from monolith:
--   - Dedicated database: ftgo_courier_service
--   - Per-service ID sequence (replaces shared hibernate_sequence)
--   - courier_actions.order_id is stored as a reference ID (no FK to orders table)
--   - courier_actions.courier_id FK retained (within same service boundary)
-- ============================================================

-- Per-service ID generation sequence (replaces shared hibernate_sequence)
CREATE TABLE courier_id_sequence (
    next_val BIGINT NOT NULL
) ENGINE = InnoDB;

INSERT INTO courier_id_sequence VALUES (1000);

-- Courier entity
CREATE TABLE courier (
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
) ENGINE = InnoDB;

-- Courier actions (delivery lifecycle events)
-- Note: order_id is a cross-service reference stored as a plain BIGINT.
--       No FK constraint to the Order Service database.
--       Data consistency is maintained via domain events.
CREATE TABLE courier_actions (
    courier_id BIGINT       NOT NULL,
    order_id   BIGINT       COMMENT 'Reference to Order Service order ID (no FK - cross-service)',
    time       DATETIME,
    type       VARCHAR(255)
) ENGINE = InnoDB;

-- Intra-service FK: courier_actions -> courier (same bounded context)
ALTER TABLE courier_actions
    ADD CONSTRAINT fk_courier_actions_courier_id
        FOREIGN KEY (courier_id) REFERENCES courier (id);

-- Indexes for common query patterns
CREATE INDEX idx_courier_available ON courier (available);
CREATE INDEX idx_courier_actions_courier_id ON courier_actions (courier_id);
CREATE INDEX idx_courier_actions_order_id ON courier_actions (order_id);
