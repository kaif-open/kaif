CREATE TABLE ZoneAdmin (
  accountId  UUID          NOT NULL,
  zone       VARCHAR(4096) NOT NULL,
  createTime TIMESTAMPTZ   NOT NULL,
  PRIMARY KEY (accountId, zone)
);

BEGIN;

INSERT INTO ZoneAdmin (accountId, zone, createTime)
  SELECT
    unnest(adminaccountids),
    zone,
    createTime,
    FALSE
  FROM ZoneInfo;

COMMIT;