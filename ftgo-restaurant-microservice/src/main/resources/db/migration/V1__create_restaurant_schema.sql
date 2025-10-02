CREATE DATABASE IF NOT EXISTS ftgo_restaurant;
USE ftgo_restaurant;

CREATE TABLE IF NOT EXISTS restaurants
(
  id   BIGINT NOT NULL AUTO_INCREMENT,
  name VARCHAR(255),
  street1 VARCHAR(255),
  street2 VARCHAR(255),
  city    VARCHAR(255),
  state   VARCHAR(255),
  zip     VARCHAR(255),
  PRIMARY KEY (id)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS restaurant_menu_items
(
  restaurant_id BIGINT NOT NULL,
  id            VARCHAR(255),
  name          VARCHAR(255),
  price         DECIMAL(19, 2),
  CONSTRAINT restaurant_menu_items_restaurant_id FOREIGN KEY (restaurant_id) REFERENCES restaurants (id)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS hibernate_sequence
(
  next_val BIGINT
) ENGINE = InnoDB;

INSERT INTO hibernate_sequence VALUES (1);
