CREATE TABLE restaurants (
  id BIGINT NOT NULL,
  name VARCHAR(255),
  PRIMARY KEY (id)
) ENGINE = InnoDB;

CREATE TABLE restaurant_menu_items (
  menu_id VARCHAR(255) NOT NULL,
  name VARCHAR(255),
  price DECIMAL(19, 2),
  restaurant_id BIGINT,
  PRIMARY KEY (menu_id)
) ENGINE = InnoDB;

CREATE TABLE hibernate_sequence (
  next_val BIGINT
) ENGINE = InnoDB;

INSERT INTO hibernate_sequence VALUES (1);

ALTER TABLE restaurant_menu_items
  ADD CONSTRAINT FK_restaurant_menu_items_restaurant_id
  FOREIGN KEY (restaurant_id)
  REFERENCES restaurants (id);
