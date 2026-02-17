-- FTGO Order Service - Initial Schema
-- Database: ftgo_order_db

CREATE TABLE IF NOT EXISTS orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    consumer_id BIGINT NOT NULL,
    restaurant_id BIGINT NOT NULL,
    state VARCHAR(30) NOT NULL DEFAULT 'APPROVAL_PENDING',
    order_minimum DECIMAL(19,2),
    delivery_address_street1 VARCHAR(255),
    delivery_address_street2 VARCHAR(255),
    delivery_address_city VARCHAR(100),
    delivery_address_state VARCHAR(50),
    delivery_address_zip VARCHAR(20),
    delivery_time DATETIME,
    payment_token VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_orders_consumer (consumer_id),
    INDEX idx_orders_restaurant (restaurant_id),
    INDEX idx_orders_state (state)
);

CREATE TABLE IF NOT EXISTS order_line_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    menu_item_id VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    price DECIMAL(19,2) NOT NULL,
    quantity INT NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    INDEX idx_line_items_order (order_id)
);
