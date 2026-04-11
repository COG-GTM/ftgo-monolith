CREATE TABLE courier
(
  id         BIGINT AUTO_INCREMENT NOT NULL,
  available  BOOLEAN,
  first_name VARCHAR(255),
  last_name  VARCHAR(255),
  street1    VARCHAR(255),
  street2    VARCHAR(255),
  city       VARCHAR(255),
  state      VARCHAR(255),
  zip        VARCHAR(255),
  PRIMARY KEY (id)
);

CREATE TABLE courier_actions
(
  courier_id BIGINT NOT NULL,
  order_id   BIGINT,
  time       TIMESTAMP,
  type       VARCHAR(255)
);

CREATE TABLE hibernate_sequence
(
  next_val BIGINT
);

INSERT INTO hibernate_sequence VALUES (1);

ALTER TABLE courier_actions
  ADD CONSTRAINT courier_actions_courier_id FOREIGN KEY (courier_id) REFERENCES courier (id);
