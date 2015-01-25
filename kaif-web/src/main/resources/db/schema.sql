CREATE TABLE Account (
    accountId uuid PRIMARY KEY NOT NULL,
    name varchar(4095) NOT NULL,
    email varchar(4095) NOT NULL,
    passwordHash varchar(4095) NOT NULL,
    authorities TEXT[] NOT NULL,
    createTime timestamp NOT NULL
);

CREATE UNIQUE INDEX AccountNameIndex ON Account (LOWER(name));
CREATE UNIQUE INDEX AccountEmailIndex ON Account (LOWER(email));

CREATE TABLE AccountOnceToken (
    token varchar(4095) PRIMARY KEY NOT NULL,
    accountId uuid NOT NULL REFERENCES Account(accountId),
    complete boolean NOT NULL,
    type varchar(4095) NOT NULL,
    createTime timestamp NOT NULL
);