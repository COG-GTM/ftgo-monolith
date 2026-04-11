CREATE TABLE orders (
  id BIGINT NOT NULL,
  consumer_id BIGINT,
  delivery_information_delivery_address_city VARCHAR(255),
  delivery_information_delivery_address_state VARCHAR(255),
  delivery_information_delivery_address_street1 VARCHAR(255),
  delivery_information_delivery_address_street2 VARCHAR(255),
  delivery_information_delivery_address_zip VARCHAR(255),
  delivery_information_delivery_time DATETIME,
  order_minimum DECIMAL(19, 2),
  payment_information_payment_token VARCHAR(255),
  restaurant_name VARCHAR(255),
  state VARCHAR(255),
  assigned_courier_id BIGINT,
  restaurant_id BIGINT,
  PRIMARY KEY (id)
) ENGINE = InnoDB;

CREATE TABLE order_line_items (
  id BIGINT NOT NULL AUTO_INCREMENT,
  menu_item_id VARCHAR(255),
  name VARCHAR(255),
  price DECIMAL(19, 2),
  quantity INTEGER,
  order_id BIGINT,
  PRIMARY KEY (id)
) ENGINE = InnoDB;

CREATE TABLE hibernate_sequence (
  next_val BIGINT
) ENGINE = InnoDB;

INSERT INTO hibernate_sequence VALUES (1);

ALTER TABLE order_line_items
  ADD CONSTRAINT FK_order_line_items_order_id
  FOREIGN KEY (order_id)
  REFERENCES orders (id);
