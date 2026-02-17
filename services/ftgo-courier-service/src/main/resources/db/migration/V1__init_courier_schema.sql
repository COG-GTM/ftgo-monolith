-- FTGO Courier Service - Initial Schema
-- Database: ftgo_courier_db

CREATE TABLE IF NOT EXISTS couriers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    available BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS courier_actions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    courier_id BIGINT NOT NULL,
    action_type VARCHAR(30) NOT NULL,
    address_street1 VARCHAR(255),
    address_street2 VARCHAR(255),
    address_city VARCHAR(100),
    address_state VARCHAR(50),
    address_zip VARCHAR(20),
    delivery_time DATETIME,
    FOREIGN KEY (courier_id) REFERENCES couriers(id) ON DELETE CASCADE,
    INDEX idx_actions_courier (courier_id)
);
