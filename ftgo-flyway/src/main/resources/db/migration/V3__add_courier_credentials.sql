CREATE TABLE courier_credentials (
  courier_id    BIGINT       NOT NULL,
  username      VARCHAR(100) NOT NULL,
  password_hash VARCHAR(100) NOT NULL,
  PRIMARY KEY (courier_id),
  UNIQUE KEY uk_courier_credentials_username (username),
  CONSTRAINT fk_courier_credentials_courier FOREIGN KEY (courier_id) REFERENCES courier (id)
) ENGINE = InnoDB;
