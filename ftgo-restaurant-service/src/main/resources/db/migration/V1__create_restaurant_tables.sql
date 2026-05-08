CREATE TABLE restaurants (
  id        BIGINT NOT NULL AUTO_INCREMENT,
  name      VARCHAR(100),
  street1   VARCHAR(100),
  street2   VARCHAR(100),
  city      VARCHAR(100),
  state     VARCHAR(50),
  zip       VARCHAR(50),
  latitude  DOUBLE NULL,
  longitude DOUBLE NULL,
  PRIMARY KEY (id)
) ENGINE = InnoDB;

CREATE TABLE restaurant_menu_items (
  restaurant_id BIGINT       NOT NULL,
  id            VARCHAR(100) NOT NULL,
  name          VARCHAR(100),
  price         VARCHAR(50),
  CONSTRAINT fk_restaurant_menu_items_restaurant
    FOREIGN KEY (restaurant_id) REFERENCES restaurants (id)
) ENGINE = InnoDB;
