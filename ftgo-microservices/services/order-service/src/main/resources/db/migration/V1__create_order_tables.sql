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

CREATE TABLE order_line_items
(
    order_id     BIGINT NOT NULL,
    menu_item_id VARCHAR(255),
    name         VARCHAR(255),
    price        DECIMAL(19, 2),
    quantity     INTEGER NOT NULL,
    CONSTRAINT fk_line_items_order FOREIGN KEY (order_id) REFERENCES orders (id)
) ENGINE = InnoDB;
