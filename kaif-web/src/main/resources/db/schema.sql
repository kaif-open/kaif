CREATE TABLE Account (
  accountId    UUID          NOT NULL PRIMARY KEY,
  username     VARCHAR(4096) NOT NULL,
  email        VARCHAR(4096) NOT NULL,
  passwordHash VARCHAR(4096) NOT NULL,
  authorities  TEXT []       NOT NULL,
  createTime   TIMESTAMP     NOT NULL
);

CREATE UNIQUE INDEX AccountUsernameIndex ON Account (LOWER(username));
CREATE UNIQUE INDEX AccountEmailIndex ON Account (LOWER(email));

CREATE TABLE AccountOnceToken (
  token      VARCHAR(4096) NOT NULL PRIMARY KEY,
  accountId  UUID          NOT NULL REFERENCES Account (accountId),
  complete   BOOLEAN       NOT NULL,
  tokenType  VARCHAR(4096) NOT NULL,
  createTime TIMESTAMP     NOT NULL
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
  createTime      TIMESTAMP     NOT NULL
);

CREATE TABLE Article (
  zone        VARCHAR(4096)  NOT NULL,
  articleId   BIGINT         NOT NULL,
  title       VARCHAR(4096)  NOT NULL,
  urlName     VARCHAR(4096)  NULL,
  linkType    VARCHAR(4096)  NOT NULL,
  createTime  TIMESTAMP      NOT NULL,
  content     VARCHAR(16384) NOT NULL,
  contentType VARCHAR(4096)  NOT NULL,
  authorId    UUID           NOT NULL REFERENCES Account (accountId),
  authorName  VARCHAR(4096)  NOT NULL,
  deleted     BOOLEAN        NOT NULL DEFAULT FALSE,
  upVote      BIGINT         NOT NULL DEFAULT 0,
  downVote    BIGINT         NOT NULL DEFAULT 0,
  debateCount BIGINT         NOT NULL DEFAULT 0,
  PRIMARY KEY (zone, articleId)
);

CREATE INDEX ArticleAuthorIndex ON Article (authorId);

CREATE TABLE Debate (
  articleId      BIGINT         NOT NULL,
  debateId       BIGINT         NOT NULL,
  parentDebateId BIGINT         NOT NULL,
  level          INT            NOT NULL,
  content        VARCHAR(16384) NOT NULL,
  renderContent  VARCHAR(16384) NOT NULL,
  contentType    VARCHAR(4096)  NOT NULL,
  debaterId      UUID           NOT NULL REFERENCES Account (accountId),
  debaterName    VARCHAR(4096)  NOT NULL,
  upVote         BIGINT         NOT NULL DEFAULT 0,
  downVote       BIGINT         NOT NULL DEFAULT 0,
  createTime     TIMESTAMP      NOT NULL,
  lastUpdateTime TIMESTAMP      NOT NULL,
  PRIMARY KEY (articleId, debateId)
);

CREATE INDEX DebaterIndex ON Debate (debaterId);

CREATE TABLE DebateHistory (
  debateId         BIGINT         NOT NULL,
  revision         INT            NOT NULL,
  content          VARCHAR(16384) NOT NULL,
  renderContent    VARCHAR(16384) NOT NULL,
  createTime       TIMESTAMP      NOT NULL,
  PRIMARY KEY (debateId, revision)
);

-- foreign key is intended exclude
CREATE TABLE ArticleVoter (
  voterId       UUID          NOT NULL,
  articleId     BIGINT        NOT NULL,
  voteState     VARCHAR(4096) NOT NULL,
  previousCount BIGINT        NOT NULL,
  updateTime    TIMESTAMP     NOT NULL,
  PRIMARY KEY (voterId, articleId)
);

-- foreign key is intended exclude
CREATE TABLE DebateVoter (
  voterId       UUID          NOT NULL,
  articleId     BIGINT        NOT NULL,
  debateId      BIGINT        NOT NULL,
  voteState     VARCHAR(4096) NOT NULL,
  previousCount BIGINT        NOT NULL,
  updateTime    TIMESTAMP     NOT NULL,
  PRIMARY KEY (voterId, articleId, debateId)
);

CREATE TABLE AccountStats (
  accountId       UUID   NOT NULL REFERENCES Account (accountId),
  debateCount     BIGINT NOT NULL DEFAULT 0,
  articleCount    BIGINT NOT NULL DEFAULT 0,
  debateUpVoted   BIGINT NOT NULL DEFAULT 0,
  debateDownVoted BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (accountId)
);