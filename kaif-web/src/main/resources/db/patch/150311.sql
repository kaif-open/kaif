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

BEGIN;

INSERT INTO FeedAsset (accountId, assetId, assetType, createTime, acked)
     SELECT replytoaccountid, debateid, 0, createTime, false FROM Debate
      WHERE debaterid <> debate.replytoaccountid;

COMMIT;