CREATE TABLE IF NOT EXISTS orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    order_state VARCHAR(50) NOT NULL,
    consumer_id BIGINT NOT NULL,
    restaurant_id BIGINT,
    order_minimum DECIMAL(19, 2),
    delivery_time DATETIME(6),
    delivery_address_street1 VARCHAR(255),
    delivery_address_street2 VARCHAR(255),
    delivery_address_city VARCHAR(255),
    delivery_address_state VARCHAR(255),
    delivery_address_zip VARCHAR(20),
    payment_token VARCHAR(255),
    ready_by DATETIME(6),
    accept_time DATETIME(6),
    preparing_time DATETIME(6),
    ready_for_pickup_time DATETIME(6),
    picked_up_time DATETIME(6),
    delivered_time DATETIME(6),
    assigned_courier_id BIGINT,
    INDEX idx_orders_consumer_id (consumer_id),
    INDEX idx_orders_restaurant_id (restaurant_id),
    INDEX idx_orders_state (order_state),
    INDEX idx_orders_assigned_courier (assigned_courier_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS order_line_items (
    order_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    menu_item_id VARCHAR(255),
    name VARCHAR(255),
    price DECIMAL(19, 2),
    CONSTRAINT fk_order_line_items_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    INDEX idx_order_line_items_order (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
