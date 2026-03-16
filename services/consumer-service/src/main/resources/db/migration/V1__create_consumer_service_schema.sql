-- =============================================================================
-- Consumer Service Schema Migration V1
-- =============================================================================
-- Extracted from the monolith's shared ftgo database.
--
-- Key changes from the monolith schema:
--   1. Uses a per-service ID sequence instead of the shared hibernate_sequence.
--   2. No cross-service foreign keys -- this service owns the consumers table
--      exclusively.
-- =============================================================================

-- Per-service ID sequence replaces the shared hibernate_sequence.
-- Each service owns its own sequence to avoid cross-service coordination.
CREATE TABLE consumer_service_sequence
(
    next_val BIGINT NOT NULL
) ENGINE = InnoDB;

INSERT INTO consumer_service_sequence VALUES (1);

-- -------------------------------------------------------------------
-- consumers table
-- -------------------------------------------------------------------
CREATE TABLE consumers
(
    id         BIGINT       NOT NULL,
    first_name VARCHAR(255),
    last_name  VARCHAR(255),
    PRIMARY KEY (id)
) ENGINE = InnoDB;
