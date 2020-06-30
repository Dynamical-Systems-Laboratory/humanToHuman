CREATE TABLE IF NOT EXISTS metadata (
  id          INTEGER         NOT NULL,
  tdata       VARCHAR         NOT NULL DEFAULT '',
  ndata       BIGINT          NOT NULL DEFAULT 0,
  PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS experiments (
  id          SERIAL          NOT NULL,
  hash        varchar         NOT NULL UNIQUE,
  open        TIMESTAMP       NOT NULL,
  began       TIMESTAMP       ,
  ended       TIMESTAMP       ,
  PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS devices (
  id          BIGINT                                NOT NULL,
  experiment  INTEGER REFERENCES experiments (id)   NOT NULL,
  hash        varchar                               NOT NULL UNIQUE,
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
