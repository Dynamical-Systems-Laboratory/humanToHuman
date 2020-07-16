CREATE TABLE IF NOT EXISTS experiments (
  id          SERIAL          NOT NULL,
  hash        varchar         NOT NULL UNIQUE,
  policy      varchar         NOT NULL,
  description varchar         NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS devices (
  id          BIGINT          NOT NULL,
  experiment  INTEGER         NOT NULL,
  hash        varchar         NOT NULL UNIQUE,
  PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS connections (
  id                SERIAL                            NOT NULL,
  time              TIMESTAMP                         NOT NULL,
  device_a          BIGINT                            NOT NULL,
  device_b          BIGINT                            NOT NULL,
  measured_power    INTEGER                           NOT NULL,
  rssi              FLOAT                             NOT NULL,
  PRIMARY KEY (id)
);
