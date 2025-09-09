-- Drop existing tables for clean dev initialization (order matters due to FKs)
DROP TABLE IF EXISTS history;
DROP TABLE IF EXISTS auth;


CREATE TABLE IF NOT EXISTS auth (
                                    id         VARCHAR(255)                                                NOT NULL PRIMARY KEY,
                                    created_at DATETIME(6)                                                 NULL,
                                    deleted_at DATETIME(6)                                                 NULL,
                                    email        VARCHAR(255)                                                NOT NULL,
                                    password     VARCHAR(255)                                                NOT NULL,
                                    provider     ENUM ('APPLE','GOOGLE','KAKAO','SYSTEM')                    NOT NULL,
                                    phone_number CHAR(11)                                                    NULL,
                                    status       ENUM ('ACTIVE','BLOCKED','DELETED','EXPIRED','UNCONFIRMED') NOT NULL,
                                    updated_at DATETIME(6)                                                 NULL,
                                    version    INT                                                         NULL,
                                    user_role    ENUM ('ADMIN','GUEST','PLACE_OWNER','USER')                 NOT NULL
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci;

-- auth 생성 후 history 생성 (컬럼 타입/길이 일치, 엔진 InnoDB)
CREATE TABLE IF NOT EXISTS history (
                                       id                VARCHAR(255) NOT NULL PRIMARY KEY,
                                       afterColumnValue VARCHAR(255) NOT NULL,
                                       beforeColumnValue VARCHAR(255) NULL,
                                       updated_at        DATETIME(6)  NULL,
                                       updatedColumn    VARCHAR(255) NOT NULL,
                                       version           INT          NULL,
                                       user_id          VARCHAR(255) NOT NULL,
                                       CONSTRAINT fk_history_user FOREIGN KEY (user_id) REFERENCES auth (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci;



CREATE TABLE IF NOT EXISTS withdraw(
    id  varchar(255) not null  primary key ,
    withdraw_at DATETIME(6) not null ,
    version int null ,
    withdraw_reason varchar(100) null

);

CREATE TABLE IF NOT EXISTS suspend(
                                      id            varchar(255) not null primary key,
                                      user_id       varchar(255) not null,
    suspend_at DATETIME(6) not null ,
    version int null,
                                      suspend_until DATE         not null,
                                      suspender     varchar(255) not null,
                                      reason        varchar(100) NOT NULL
);


drop table if exists consent;
CREATE TABLE IF NOT EXISTS consent
(
    id             varchar(255)                        not null primary key,
    user_id        varchar(255)                        not null,
    agreement_at   DATETIME(6)                         not null,
    versions       int                                 null,
    consent_type   ENUM ('PERSONAL_INFO', 'MARKETING') not null,
    consent_url    varchar(255)                        not null,
    CONSTRAINT fk_consent_id FOREIGN KEY (user_id) REFERENCES auth (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci;

