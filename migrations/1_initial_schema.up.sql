create table if not exists devices (
  id          INTEGER         NOT NULL,
  PRIMARY KEY (id)
);

create table if not exists connections (
  id                INTEGER                           NOT NULL,
  time              TIMESTAMP                         NOT NULL,
  device_a          INTEGER REFERENCES devices (id)   NOT NULL,
  device_b          INTEGER REFERENCES devices (id)   NOT NULL,
  meausured_power   INTEGER                           NOT NULL,
  rssi              FLOAT                             NOT NULL,
  PRIMARY KEY (id)
);
