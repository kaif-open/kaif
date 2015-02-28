BEGIN;
ALTER TABLE Debate ADD COLUMN replyToAccountId UUID;

UPDATE Debate
SET replyToAccountId = (SELECT a.authorId
                        FROM Article a
                        WHERE a.articleId = Debate.articleid)
WHERE parentdebateid = 0;

UPDATE Debate
SET replyToAccountId = (SELECT m.debaterId
                        FROM Debate m
                        WHERE Debate.parentdebateid = m.debateid)
WHERE parentdebateid <> 0;

SELECT
  debateid,
  debatername,
  content
FROM Debate
WHERE replyToAccountId IS NULL;

COMMIT;

BEGIN;
ALTER TABLE Debate ALTER COLUMN replyToAccountId SET NOT NULL;
COMMIT;


CREATE INDEX DebateReplyToAccountId ON Debate (replyToAccountId);