-- =============================================================================
-- Courier Service Schema Migration V1
-- =============================================================================
-- Extracted from the monolith's shared ftgo database.
--
-- Key changes from the monolith schema:
--   1. Uses a per-service ID sequence instead of the shared hibernate_sequence.
--   2. Removed cross-service foreign key from courier_actions.order_id to
--      orders -- order references are now soft (eventual consistency).
--   3. Retains the intra-service FK between courier_actions and courier
--      since both tables are owned by this service.
--   4. Index on courier_actions.order_id for query performance without FK.
-- =============================================================================

-- Per-service ID sequence replaces the shared hibernate_sequence.
-- Each service owns its own sequence to avoid cross-service coordination.
CREATE TABLE courier_service_sequence
(
    next_val BIGINT NOT NULL
) ENGINE = InnoDB;

INSERT INTO courier_service_sequence VALUES (1);

-- -------------------------------------------------------------------
-- courier table
-- -------------------------------------------------------------------
CREATE TABLE courier
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
) ENGINE = InnoDB;

-- -------------------------------------------------------------------
-- courier_actions table
-- -------------------------------------------------------------------
CREATE TABLE courier_actions
(
    courier_id BIGINT       NOT NULL,
    order_id   BIGINT,
    time       DATETIME,
    type       VARCHAR(255),
    CONSTRAINT fk_courier_actions_courier_id
        FOREIGN KEY (courier_id) REFERENCES courier (id)
) ENGINE = InnoDB;

-- -------------------------------------------------------------------
-- Index for cross-service reference column (no FK, soft reference)
-- -------------------------------------------------------------------
-- order_id references an entity in the Order Service. We keep an index
-- for query performance but do NOT add a foreign key constraint --
-- consistency is enforced at the application level.
CREATE INDEX idx_courier_actions_order_id ON courier_actions (order_id);
