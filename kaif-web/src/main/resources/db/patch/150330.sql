CREATE TABLE ClientApp (
  clientId       VARCHAR(4096) NOT NULL,
  clientSecret   VARCHAR(4096) NOT NULL,
  appName        VARCHAR(4096) NOT NULL,
  description    VARCHAR(4096) NOT NULL,
  createTime     TIMESTAMPTZ   NOT NULL,
  ownerAccountId UUID          NOT NULL,
  revoked        BOOLEAN       NOT NULL,
  callbackUri    VARCHAR(4096) NOT NULL,
  PRIMARY KEY (clientId)
);

-- delete ClientApp (revoked?) will cascade delete all ClientAppUser
CREATE TABLE ClientAppUser (
  clientAppUserId   UUID          NOT NULL,
  clientId          VARCHAR(4096) NOT NULL REFERENCES ClientApp (clientId) ON DELETE CASCADE,
  accountId         UUID          NOT NULL REFERENCES Account (accountId),
  lastGrantedScopes TEXT []       NOT NULL,
  lastUpdateTime    TIMESTAMPTZ   NOT NULL,
  PRIMARY KEY (clientAppUserId)
);

CREATE UNIQUE INDEX ClientAppUserClientAccountIndex ON ClientAppUser (clientId, accountId);