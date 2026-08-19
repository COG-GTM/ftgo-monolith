use ftgo;

CREATE TABLE app_users
(
  id            BIGINT       NOT NULL AUTO_INCREMENT,
  username      VARCHAR(255) NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  role          VARCHAR(32)  NOT NULL,
  subject_id    BIGINT       NULL,
  enabled       BIT          NOT NULL DEFAULT 1,
  PRIMARY KEY (id),
  UNIQUE KEY uk_app_users_username (username)
) ENGINE = InnoDB;
