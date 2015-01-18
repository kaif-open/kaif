CREATE TABLE Account (
    accountId uuid PRIMARY KEY NOT NULL,

    email varchar(4095) UNIQUE NOT NULL,
    passwordHash varchar(4095) NOT NULL,

    name varchar(4095) UNIQUE NOT NULL,
    createTime timestamp NOT NULL
);