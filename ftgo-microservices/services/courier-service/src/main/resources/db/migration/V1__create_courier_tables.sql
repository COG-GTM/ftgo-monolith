CREATE TABLE courier
(
    id         BIGINT NOT NULL AUTO_INCREMENT,
    available  BIT,
    first_name VARCHAR(255),
    last_name  VARCHAR(255),
    street1    VARCHAR(255),
    street2    VARCHAR(255),
    city       VARCHAR(255),
    state      VARCHAR(255),
    zip        VARCHAR(255),
    PRIMARY KEY (id)
) ENGINE = InnoDB;

CREATE TABLE courier_actions
(
    courier_id BIGINT NOT NULL,
    order_id   BIGINT,
    time       DATETIME,
    type       VARCHAR(255),
    CONSTRAINT fk_courier_actions_courier FOREIGN KEY (courier_id) REFERENCES courier (id)
) ENGINE = InnoDB;
