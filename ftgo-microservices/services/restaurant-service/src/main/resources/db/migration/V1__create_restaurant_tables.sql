CREATE TABLE restaurants
(
    id      BIGINT       NOT NULL AUTO_INCREMENT,
    name    VARCHAR(255),
    street1 VARCHAR(255),
    street2 VARCHAR(255),
    city    VARCHAR(255),
    state   VARCHAR(255),
    zip     VARCHAR(255),
    PRIMARY KEY (id)
) ENGINE = InnoDB;

CREATE TABLE restaurant_menu_items
(
    restaurant_id BIGINT NOT NULL,
    id            VARCHAR(255),
    name          VARCHAR(255),
    price         DECIMAL(19, 2),
    CONSTRAINT fk_menu_items_restaurant FOREIGN KEY (restaurant_id) REFERENCES restaurants (id)
) ENGINE = InnoDB;
