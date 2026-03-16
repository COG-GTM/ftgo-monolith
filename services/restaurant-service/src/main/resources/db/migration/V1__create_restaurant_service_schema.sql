-- =============================================================================
-- Restaurant Service Schema Migration V1
-- =============================================================================
-- Extracted from the monolith's shared ftgo database.
--
-- Key changes from the monolith schema:
--   1. Uses a per-service ID sequence instead of the shared hibernate_sequence.
--   2. Retains the intra-service FK between restaurant_menu_items and
--      restaurants since both tables are owned by this service.
-- =============================================================================

-- Per-service ID sequence replaces the shared hibernate_sequence.
-- Each service owns its own sequence to avoid cross-service coordination.
CREATE TABLE restaurant_service_sequence
(
    next_val BIGINT NOT NULL
) ENGINE = InnoDB;

INSERT INTO restaurant_service_sequence VALUES (1);

-- -------------------------------------------------------------------
-- restaurants table
-- -------------------------------------------------------------------
CREATE TABLE restaurants
(
    id      BIGINT       NOT NULL AUTO_INCREMENT,
    name    VARCHAR(255),
    street1 VARCHAR(255),
    street2 VARCHAR(255),
    city    VARCHAR(255),
    state   VARCHAR(255),
    zip     VARCHAR(255),
    PRIMARY KEY (id)
) ENGINE = InnoDB;

-- -------------------------------------------------------------------
-- restaurant_menu_items table (collection table owned by restaurants)
-- -------------------------------------------------------------------
CREATE TABLE restaurant_menu_items
(
    restaurant_id BIGINT       NOT NULL,
    id            VARCHAR(255),
    name          VARCHAR(255),
    price         DECIMAL(19, 2),
    CONSTRAINT fk_restaurant_menu_items_restaurant_id
        FOREIGN KEY (restaurant_id) REFERENCES restaurants (id)
) ENGINE = InnoDB;
