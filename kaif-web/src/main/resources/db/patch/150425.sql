CREATE TABLE ArticleExternalLink (
  articleId    BIGINT        NOT NULL,
  zone         VARCHAR(4096) NOT NULL,
  canonicalUrl VARCHAR(4096) NOT NULL,
  rawUrl       VARCHAR(4096) NOT NULL,
  createTime   TIMESTAMPTZ   NOT NULL,
  PRIMARY KEY (articleId)
);

CREATE INDEX ArticleExternalLinkCanonicalUrlZoneIndex ON ArticleExternalLink (canonicalUrl, zone);

BEGIN;

INSERT INTO ArticleExternalLink (articleId, zone, canonicalUrl, rawUrl, createTime)
  SELECT
    articleId,
    zone,
    link,
    link,
    createTime
  FROM Article
  WHERE link IS NOT NULL;

COMMIT;