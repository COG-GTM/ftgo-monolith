CREATE TABLE IF NOT EXISTS courier (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    street1 VARCHAR(255),
    street2 VARCHAR(255),
    city VARCHAR(255),
    state VARCHAR(255),
    zip VARCHAR(20),
    available BOOLEAN DEFAULT FALSE,
    INDEX idx_courier_available (available)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS courier_actions (
    courier_id BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL,
    time DATETIME(6),
    order_id BIGINT,
    CONSTRAINT fk_courier_actions_courier FOREIGN KEY (courier_id) REFERENCES courier(id) ON DELETE CASCADE,
    INDEX idx_courier_actions_courier (courier_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
