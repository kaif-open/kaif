CREATE TABLE ZoneAdmin (
  accountId       UUID          NOT NULL,
  zone            VARCHAR(4096) NOT NULL,
  createTime      TIMESTAMPTZ NOT NULL,
  PRIMARY KEY (accountId, zone)
);