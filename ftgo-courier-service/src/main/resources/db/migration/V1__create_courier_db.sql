CREATE TABLE courier (
  id BIGINT NOT NULL,
  available BIT(1),
  first_name VARCHAR(255),
  last_name VARCHAR(255),
  PRIMARY KEY (id)
) ENGINE = InnoDB;

CREATE TABLE courier_actions (
  id BIGINT NOT NULL,
  action_type INTEGER,
  address_city VARCHAR(255),
  address_state VARCHAR(255),
  address_street1 VARCHAR(255),
  address_street2 VARCHAR(255),
  address_zip VARCHAR(255),
  time DATETIME,
  courier_id BIGINT,
  order_id BIGINT,
  PRIMARY KEY (id)
) ENGINE = InnoDB;

CREATE TABLE hibernate_sequence (
  next_val BIGINT
) ENGINE = InnoDB;

INSERT INTO hibernate_sequence VALUES (1);

ALTER TABLE courier_actions
  ADD CONSTRAINT FK_courier_actions_courier_id
  FOREIGN KEY (courier_id)
  REFERENCES courier (id);
