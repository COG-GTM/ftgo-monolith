CREATE TABLE consumers (
  id BIGINT NOT NULL,
  first_name VARCHAR(255),
  last_name VARCHAR(255),
  PRIMARY KEY (id)
) ENGINE = InnoDB;

CREATE TABLE hibernate_sequence (
  next_val BIGINT
) ENGINE = InnoDB;

INSERT INTO hibernate_sequence VALUES (1);
