CREATE TABLE Account (
  accountId    UUID          NOT NULL PRIMARY KEY,
  username     VARCHAR(4095) NOT NULL,
  email        VARCHAR(4095) NOT NULL,
  passwordHash VARCHAR(4095) NOT NULL,
  authorities  TEXT []       NOT NULL,
  createTime   TIMESTAMP     NOT NULL
);

CREATE UNIQUE INDEX AccountUsernameIndex ON Account (LOWER(username));
CREATE UNIQUE INDEX AccountEmailIndex ON Account (LOWER(email));

CREATE TABLE AccountOnceToken (
  token      VARCHAR(4095) NOT NULL PRIMARY KEY,
  accountId  UUID          NOT NULL REFERENCES Account (accountId),
  complete   BOOLEAN       NOT NULL,
  tokenType  VARCHAR(4095) NOT NULL,
  createTime TIMESTAMP     NOT NULL
);

CREATE TABLE ZoneInfo (
  zone            VARCHAR(4095) NOT NULL PRIMARY KEY,
  aliasName       VARCHAR(4095) NULL,
  theme           VARCHAR(4095) NULL,
  readAuthority   VARCHAR(4095) NOT NULL,
  writeAuthority  VARCHAR(4095) NOT NULL,
  adminAccountIds UUID []       NOT NULL,
  createTime      TIMESTAMP     NOT NULL
);

CREATE TABLE ZoneArticle (
  zone        VARCHAR(4095)  NOT NULL,
  articleId   BIGINT         NOT NULL,
  title       VARCHAR(4095)  NOT NULL,
  articleType VARCHAR(4095)  NOT NULL,
  createTime  TIMESTAMP      NOT NULL,
  content     VARCHAR(16384) NULL,
  authorId    UUID           NOT NULL REFERENCES Account (accountId),
  authorName  VARCHAR(4095)  NOT NULL,
  deleted     BOOL           NOT NULL DEFAULT FALSE,
  upVote      BIGINT         NOT NULL DEFAULT 0,
  downVote    BIGINT         NOT NULL DEFAULT 0,
  PRIMARY KEY (zone, articleId)
);

CREATE INDEX ArticleAuthorIndex ON ZoneArticle (authorId);

CREATE TABLE ArticleComment (
  articleId       BIGINT         NOT NULL,
  commentId       BIGINT         NOT NULL,
  parentCommentId BIGINT         NULL,
  level           INT            NOT NULL,
  content         VARCHAR(16384) NOT NULL,
  commentType     VARCHAR(4095)  NOT NULL,
  commenterId     UUID           NOT NULL REFERENCES Account (accountId),
  commenterName   VARCHAR(4095)  NOT NULL,
  upVote          BIGINT         NOT NULL DEFAULT 0,
  downVote        BIGINT         NOT NULL DEFAULT 0,
  createTime      TIMESTAMP      NOT NULL,
  lastUpdateTime  TIMESTAMP      NOT NULL,
  PRIMARY KEY (articleId, commenterId)
);

CREATE INDEX CommenterIndex ON ArticleComment (commenterId);

CREATE TABLE CommentHistory (
  commentId  BIGINT         NOT NULL,
  revision   INT            NOT NULL,
  content    VARCHAR(16384) NOT NULL,
  createTime TIMESTAMP      NOT NULL,
  PRIMARY KEY (commentId, revision)
);

CREATE TABLE ArticleVoter (
  voterId   UUID   NOT NULL PRIMARY KEY,
  articleId BIGINT NOT NULL,
  upVote    BOOL   NOT NULL
);

CREATE TABLE CommentVoter (
  voterId   UUID   NOT NULL PRIMARY KEY,
  commentId BIGINT NOT NULL,
  upVote    BOOL   NOT NULL
);

