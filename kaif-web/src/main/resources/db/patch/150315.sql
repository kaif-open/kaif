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

ALTER TABLE AccountStats ADD COLUMN articleUpVoted BIGINT NOT NULL DEFAULT 0;


BEGIN;

UPDATE AccountStats
SET articleUpVoted = Score.total
FROM (SELECT
        sum(upvote) AS total,
        authorId
      FROM Article
      GROUP BY authorId) AS Score
WHERE Score.authorId = accountId;

COMMIT;

BEGIN;

INSERT INTO HonorRoll (accountId, zone, bucket, username, articleUpVoted, debateUpVoted, debateDownVoted)
  SELECT
    aId,
    zone,
    '2015-03-01',
    aName,
    0,
    0,
    0
  FROM (
         SELECT
           authorid   AS aId,
           zone,
           authorname AS aName
         FROM Article
         GROUP BY authorid, zone, authorname
         UNION
         SELECT
           debaterId   AS aId,
           zone,
           debaterName AS aName
         FROM debate
         GROUP BY debaterId, zone, debatername
       ) AS AllRoll;

UPDATE HonorRoll
SET articleUpVoted = Score.total
FROM (SELECT
        sum(upvote) AS total,
        authorId,
        zone        AS sZone
      FROM Article
      GROUP BY authorId, zone) AS Score
WHERE Score.authorId = accountId
      AND Score.sZone = zone;

UPDATE HonorRoll
SET debateUpVoted   = Score.totalUp
  , debateDownVoted = Score.totalDown
FROM (SELECT
        sum(upvote)   AS totalUp,
        sum(downvote) AS totalDown,
        debaterId,
        zone          AS sZone
      FROM debate
      GROUP BY debaterId, zone) AS Score
WHERE Score.debaterid = accountId
      AND Score.sZone = zone;

COMMIT;


