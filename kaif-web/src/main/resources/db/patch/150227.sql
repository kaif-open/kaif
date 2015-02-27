ALTER TABLE article ADD COLUMN aliasName VARCHAR(4096);

UPDATE article
SET aliasName = (SELECT aliasname
                 FROM zoneInfo z
                 WHERE article.zone = z.zone);

ALTER TABLE article ALTER COLUMN aliasName SET NOT NULL;