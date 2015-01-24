DROP DATABASE if exists ci_kaif;
DROP ROLE if exists ci_kaif;
CREATE ROLE ci_kaif LOGIN PASSWORD 'changeme' NOINHERIT VALID UNTIL 'infinity';
CREATE DATABASE ci_kaif  WITH ENCODING='UTF8' OWNER=ci_kaif TEMPLATE=template1;
\c ci_kaif
ALTER SCHEMA public OWNER TO ci_kaif;
