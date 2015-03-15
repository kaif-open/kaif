CREATE TABLE Account (
  accountId    UUID           NOT NULL PRIMARY KEY,
  username     VARCHAR(4096)  NOT NULL,
  email        VARCHAR(4096)  NOT NULL,
  passwordHash VARCHAR(4096)  NOT NULL,
  description  VARCHAR(16384) NOT NULL,
  authorities  TEXT []        NOT NULL,
  createTime   TIMESTAMPTZ    NOT NULL
);

CREATE UNIQUE INDEX AccountUsernameIndex ON Account (LOWER(username));
CREATE UNIQUE INDEX AccountEmailIndex ON Account (email);

CREATE TABLE AccountOnceToken (
  token      VARCHAR(4096) NOT NULL PRIMARY KEY,
  accountId  UUID          NOT NULL REFERENCES Account (accountId),
  complete   BOOLEAN       NOT NULL,
  tokenType  VARCHAR(4096) NOT NULL,
  createTime TIMESTAMPTZ   NOT NULL
);

CREATE TABLE ZoneInfo (
  zone            VARCHAR(4096) NOT NULL PRIMARY KEY,
  aliasName       VARCHAR(4096) NOT NULL,
  theme           VARCHAR(4096) NOT NULL,
  voteAuthority   VARCHAR(4096) NOT NULL,
  debateAuthority VARCHAR(4096) NOT NULL,
  writeAuthority  VARCHAR(4096) NOT NULL,
  adminAccountIds UUID []       NOT NULL,
  hideFromTop     BOOLEAN       NOT NULL,
  createTime      TIMESTAMPTZ   NOT NULL
);

CREATE TABLE Article (
  articleId   BIGINT         NOT NULL,
  zone        VARCHAR(4096)  NOT NULL,
  aliasName   VARCHAR(4096)  NOT NULL,
  title       VARCHAR(4096)  NOT NULL,
  link        VARCHAR(4096)  NULL,
  content     VARCHAR(16384) NULL,
  contentType VARCHAR(4096)  NOT NULL,
  createTime  TIMESTAMPTZ    NOT NULL,
  authorId    UUID           NOT NULL REFERENCES Account (accountId),
  authorName  VARCHAR(4096)  NOT NULL,
  deleted     BOOLEAN        NOT NULL DEFAULT FALSE,
  upVote      BIGINT         NOT NULL DEFAULT 0,
  downVote    BIGINT         NOT NULL DEFAULT 0,
  debateCount BIGINT         NOT NULL DEFAULT 0,
  PRIMARY KEY (articleId)
);

CREATE INDEX ArticleAuthorIndex ON Article (authorId);
CREATE INDEX ArticleZoneIndex ON Article (zone);

CREATE TABLE Debate (
  debateId         BIGINT         NOT NULL,
  articleId        BIGINT         NOT NULL,
  zone             VARCHAR(4096)  NOT NULL,
  parentDebateId   BIGINT         NOT NULL,
  replyToAccountId UUID           NOT NULL,
  level            INT            NOT NULL,
  content          VARCHAR(16384) NOT NULL,
  contentType      VARCHAR(4096)  NOT NULL,
  debaterId        UUID           NOT NULL REFERENCES Account (accountId),
  debaterName      VARCHAR(4096)  NOT NULL,
  upVote           BIGINT         NOT NULL DEFAULT 0,
  downVote         BIGINT         NOT NULL DEFAULT 0,
  createTime       TIMESTAMPTZ    NOT NULL,
  lastUpdateTime   TIMESTAMPTZ    NOT NULL,
  PRIMARY KEY (debateId)
);

CREATE INDEX DebaterIndex ON Debate (debaterId);
CREATE INDEX DebateArticleIndex ON Debate (articleId);
CREATE INDEX DebateReplyToAccountId ON Debate (replyToAccountId);
CREATE INDEX DebateZoneIndex ON Debate (zone);

-- foreign key is intended exclude
CREATE TABLE ArticleVoter (
  voterId       UUID          NOT NULL,
  articleId     BIGINT        NOT NULL,
  voteState     VARCHAR(4096) NOT NULL,
  previousCount BIGINT        NOT NULL,
  updateTime    TIMESTAMPTZ   NOT NULL,
  PRIMARY KEY (voterId, articleId)
);

-- foreign key is intended exclude
CREATE TABLE DebateVoter (
  voterId       UUID          NOT NULL,
  articleId     BIGINT        NOT NULL,
  debateId      BIGINT        NOT NULL,
  voteState     VARCHAR(4096) NOT NULL,
  previousCount BIGINT        NOT NULL,
  updateTime    TIMESTAMPTZ   NOT NULL,
  PRIMARY KEY (voterId, articleId, debateId)
);

CREATE INDEX DebateVoterDebateIdIndex ON DebateVoter (voterId, debateId);

CREATE TABLE AccountStats (
  accountId       UUID   NOT NULL REFERENCES Account (accountId),
  debateCount     BIGINT NOT NULL DEFAULT 0,
  articleCount    BIGINT NOT NULL DEFAULT 0,
  articleUpVoted  BIGINT NOT NULL DEFAULT 0,
  debateUpVoted   BIGINT NOT NULL DEFAULT 0,
  debateDownVoted BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (accountId)
);

CREATE TABLE FeedAsset (
  accountId  UUID        NOT NULL REFERENCES Account (accountId),
  assetId    BIGINT      NOT NULL,
  assetType  INT         NOT NULL,
  createTime TIMESTAMPTZ NOT NULL,
  acked      BOOLEAN     NOT NULL,
  PRIMARY KEY (accountId, assetId)
);

CREATE TABLE ArticleWatch (
  watchId    BIGINT      NOT NULL,
  accountId  UUID        NOT NULL REFERENCES Account (accountId),
  articleId  BIGINT      NOT NULL REFERENCES Article (articleId),
  createTime TIMESTAMPTZ NOT NULL,
  PRIMARY KEY (watchId)
);

CREATE UNIQUE INDEX ArticleWatchAccountArticleIndex ON ArticleWatch (accountId, articleId);
CREATE INDEX ArticleWatchArticleAccountIndex ON ArticleWatch (articleId, accountId);

-- see HotRanking.java
CREATE FUNCTION hotRanking(upVoted BIGINT, downVoted BIGINT, createTime TIMESTAMPTZ)
  RETURNS NUMERIC AS $$
SELECT round(cast(log(greatest(abs($1 - $2), 1)) * sign($1 - $2) +
                  (EXTRACT(EPOCH FROM $3) - 1420070400) / 45000.0 AS NUMERIC), 7)
$$ LANGUAGE SQL IMMUTABLE;

CREATE TABLE HonorRoll (
  accountId       UUID          NOT NULL REFERENCES Account (accountId),
  zone            VARCHAR(4096) NOT NULL,
  bucket          VARCHAR(10)   NOT NULL,
  username        VARCHAR(4096) NOT NULL,
  articleUpVoted  BIGINT        NOT NULL DEFAULT 0,
  debateUpVoted   BIGINT        NOT NULL DEFAULT 0,
  debateDownVoted BIGINT        NOT NULL DEFAULT 0,
  PRIMARY KEY (accountId, zone, bucket)
);

CREATE INDEX HonorRollZoneBucketIndex ON HonorRoll (zone, bucket);
