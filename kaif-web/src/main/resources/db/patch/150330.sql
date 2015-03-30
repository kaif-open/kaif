CREATE TABLE ClientApp (
  clientId       UUID          NOT NULL,
  clientSecret   VARCHAR(4096) NOT NULL,
  appName        VARCHAR(4096) NOT NULL,
  description    VARCHAR(4096) NULL,
  createTime     TIMESTAMPTZ   NOT NULL,
  ownerAccountId UUID          NOT NULL,
  revoked        BOOLEAN       NOT NULL,
  callbackUrl    VARCHAR(4096) NULL,
  PRIMARY KEY (clientId)
);

CREATE TABLE ClientAppUser (
  clientAppUserId UUID          NOT NULL,
  clientId        UUID          NOT NULL REFERENCES ClientApp (clientId),
  accountId       UUID          NOT NULL REFERENCES Account (accountId),
  scopes          TEXT []       NOT NULL,
  sessionKey      VARCHAR(4096) NOT NULL,
  PRIMARY KEY (clientAppUserId)
);

CREATE UNIQUE INDEX ClientAppUserClientAccountIndex ON ClientAppUser (clientId, accountId);