-- FTGO Restaurant Service - Initial Schema
-- Database: ftgo_restaurant_db

CREATE TABLE IF NOT EXISTS restaurants (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    address_street1 VARCHAR(255),
    address_street2 VARCHAR(255),
    address_city VARCHAR(100),
    address_state VARCHAR(50),
    address_zip VARCHAR(20),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_restaurants_name (name)
);

CREATE TABLE IF NOT EXISTS menu_items (
    id VARCHAR(50) NOT NULL,
    restaurant_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    price DECIMAL(19,2) NOT NULL,
    PRIMARY KEY (id, restaurant_id),
    FOREIGN KEY (restaurant_id) REFERENCES restaurants(id) ON DELETE CASCADE,
    INDEX idx_menu_items_restaurant (restaurant_id)
);
