-- Restaurant service owned schema. IF NOT EXISTS keeps this safe to run against a database where the
-- monolith's ftgo-flyway V1/V2 already created these tables.
CREATE TABLE IF NOT EXISTS restaurants
(
  id        BIGINT NOT NULL AUTO_INCREMENT,
  name      VARCHAR(255),
  street1   VARCHAR(255),
  street2   VARCHAR(255),
  city      VARCHAR(255),
  state     VARCHAR(255),
  zip       VARCHAR(255),
  latitude  DOUBLE NULL,
  longitude DOUBLE NULL,
  PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS restaurant_menu_items
(
  restaurant_id BIGINT NOT NULL,
  id            VARCHAR(255),
  name          VARCHAR(255),
  price         DECIMAL(19, 2),
  CONSTRAINT restaurant_menu_items_restaurant_id FOREIGN KEY (restaurant_id) REFERENCES restaurants (id)
);
