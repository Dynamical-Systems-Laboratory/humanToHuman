-- CREATE TABLE IF NOT EXISTS metadata (
-- );

CREATE TABLE IF NOT EXISTS experiments (
  id          SERIAL          NOT NULL,
  token       char(127)       NOT NULL UNIQUE,
  open        TIMESTAMP       NOT NULL,
  began       TIMESTAMP       ,
  ended       TIMESTAMP       ,
  PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS devices (
  id          BIGINT                                NOT NULL,
  experiment  INTEGER REFERENCES experiments (id)   NOT NULL,
  token       char(127)                             NOT NULL UNIQUE,
  PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS connections (
  id                SERIAL                            NOT NULL,
  time              TIMESTAMP                         NOT NULL,
  device_a          BIGINT REFERENCES devices (id)    NOT NULL,
  device_b          BIGINT REFERENCES devices (id)    NOT NULL,
  measured_power   INTEGER                            NOT NULL,
  rssi              FLOAT                             NOT NULL,
  PRIMARY KEY (id)
);
