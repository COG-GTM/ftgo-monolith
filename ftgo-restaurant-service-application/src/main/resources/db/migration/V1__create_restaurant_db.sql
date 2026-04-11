CREATE TABLE restaurants
(
  id      BIGINT AUTO_INCREMENT PRIMARY KEY,
  name    VARCHAR(255),
  street1 VARCHAR(255),
  street2 VARCHAR(255),
  city    VARCHAR(255),
  state   VARCHAR(255),
  zip     VARCHAR(255)
);

CREATE TABLE restaurant_menu_items
(
  restaurant_id BIGINT NOT NULL,
  id            VARCHAR(255),
  name          VARCHAR(255),
  price         DECIMAL(19, 2),
  CONSTRAINT restaurant_menu_items_restaurant_id FOREIGN KEY (restaurant_id) REFERENCES restaurants (id)
);

CREATE TABLE hibernate_sequence
(
  next_val BIGINT
);

INSERT INTO hibernate_sequence VALUES (1);
