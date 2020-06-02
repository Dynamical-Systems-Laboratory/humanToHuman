create table if not exists devices (
  id          BIGINT         NOT NULL,
  PRIMARY KEY (id)
);

create table if not exists connections (
  id                SERIAL                            NOT NULL,
  time              TIMESTAMP                         NOT NULL,
  device_a          BIGINT REFERENCES devices (id)    NOT NULL,
  device_b          BIGINT REFERENCES devices (id)    NOT NULL,
  measured_power   INTEGER                            NOT NULL,
  rssi              FLOAT                             NOT NULL,
  PRIMARY KEY (id)
);
