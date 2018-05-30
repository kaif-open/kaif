
BEGIN;

ALTER TABLE debate
ADD CONSTRAINT debate_articleid_fkey
FOREIGN KEY (articleId) REFERENCES article(articleId);

COMMIT;