CREATE TABLE Account (
    accountId uuid PRIMARY KEY NOT NULL,
    name varchar(4095) UNIQUE NOT NULL,
    email varchar(4095) UNIQUE NOT NULL,
    passwordHash varchar(4095) NOT NULL,
    activated boolean NOT NULL,
    authorities TEXT[] NOT NULL,
    createTime timestamp NOT NULL
);