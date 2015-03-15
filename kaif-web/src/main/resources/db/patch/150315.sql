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

-- TODO patch HonorRoll for old data