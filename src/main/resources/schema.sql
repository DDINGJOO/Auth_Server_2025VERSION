-- Drop existing tables for clean dev initialization (order matters due to FKs)
DROP TABLE IF EXISTS history;
DROP TABLE IF EXISTS auth;
DROP TABLE IF EXISTS user_role;

-- Create tables in FK-safe order
CREATE TABLE IF NOT EXISTS user_role (
    role ENUM ('ADMIN', 'GUEST', 'PLACE_OWNER', 'USER') NOT NULL,
    PRIMARY KEY (role)
);

CREATE TABLE IF NOT EXISTS auth (
    id            VARCHAR(255) NOT NULL PRIMARY KEY,
    created_at    DATETIME(6) NULL,
    deleted_at    DATETIME(6) NULL,
    email         VARCHAR(255) NULL,
    password      VARCHAR(255) NULL,
    provider      ENUM ('APPLE', 'GOOGLE', 'KAKAO', 'SYSTEM') NULL,
    status        ENUM ('ACTIVE', 'BLOCKED', 'DELETED', 'EXPIRED', 'UNCONFIRMED') NULL,
    updated_at    DATETIME(6) NULL,
    version       INT NULL,
    user_role_id  ENUM ('ADMIN', 'GUEST', 'PLACE_OWNER', 'USER') NULL,
    CONSTRAINT fk_auth_user_role FOREIGN KEY (user_role_id) REFERENCES user_role (role)
);

CREATE TABLE IF NOT EXISTS history (
    id                 VARCHAR(255) NOT NULL PRIMARY KEY,
    afterColumnValue   VARCHAR(255) NULL,
    beforeColumnValue  VARCHAR(255) NULL,
    updated_at         DATETIME(6) NULL,
    updatedColumn      VARCHAR(255) NULL,
    version            INT NULL,
    user_id            VARCHAR(255) NULL,
    CONSTRAINT FKn3wg2uc13l1bpwwn81epwo48v FOREIGN KEY (user_id) REFERENCES auth (id)
);

CREATE TABLE IF NOT EXISTS withdraw(
    id  varchar(255) not null  primary key ,
    withdraw_at DATETIME(6) not null ,
    version int null ,
    withdraw_reason varchar(100) null

);

CREATE TABLE IF NOT EXISTS suspend(
    user_id varchar(255) not null  primary key ,
    suspend_at DATETIME(6) not null ,
    version int null,
                                  suspend_until DATE not null,
                                  suspender varchar(255),
    reason varchar(100) null


);

