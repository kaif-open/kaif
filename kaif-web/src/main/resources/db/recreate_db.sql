DROP DATABASE if exists kaif;
DROP ROLE if exists kaif;
CREATE ROLE kaif LOGIN PASSWORD 'changeme' NOINHERIT VALID UNTIL 'infinity';
CREATE DATABASE kaif WITH ENCODING='UTF8' OWNER=kaif TEMPLATE=template1;
\c kaif
ALTER SCHEMA public OWNER TO kaif;
