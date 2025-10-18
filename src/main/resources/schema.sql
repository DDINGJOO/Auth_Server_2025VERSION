-- Drop existing tables for clean dev initialization (order matters due to FKs)


CREATE TABLE IF NOT EXISTS auth
(
    id           VARCHAR(255)                                                NOT NULL PRIMARY KEY,
    created_at   DATETIME(6)                                                 NULL,
    deleted_at   DATETIME(6)                                                 NULL,
    email        VARCHAR(255)                                                NOT NULL,
    password     VARCHAR(255)                                                NOT NULL,
    provider     ENUM ('APPLE','GOOGLE','KAKAO','SYSTEM')                    NOT NULL,
    phone_number CHAR(11)                                                    NULL,
    status       ENUM ('ACTIVE','BLOCKED','DELETED','EXPIRED','UNCONFIRMED') NOT NULL,
    updated_at   DATETIME(6)                                                 NULL,
    version      INT                                                         NULL,
    user_role    ENUM ('ADMIN','GUEST','PLACE_OWNER','USER')                 NOT NULL
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci;

-- auth 생성 후 history 생성 (컬럼 타입/길이 일치, 엔진 InnoDB)
CREATE TABLE IF NOT EXISTS history
(
    id                  VARCHAR(255) NOT NULL PRIMARY KEY,
    after_column_value  VARCHAR(255) NOT NULL,
    before_column_value VARCHAR(255) NULL,
    updated_at          DATETIME(6)  NULL,
    updated_column      VARCHAR(255) NOT NULL,
    version             INT          NULL,
    user_id             VARCHAR(255) NOT NULL,
    CONSTRAINT fk_history_user FOREIGN KEY (user_id) REFERENCES auth (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci;


-- Withdraw table aligned with entity (PK is user_id, no version column)
CREATE TABLE IF NOT EXISTS withdraw
(
    user_id         VARCHAR(255) NOT NULL PRIMARY KEY,
    withdraw_at     DATETIME(6)  NOT NULL,
    withdraw_reason VARCHAR(100) NULL,
    CONSTRAINT fk_withdraw_user FOREIGN KEY (user_id) REFERENCES auth (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci;

-- Suspend table (optional FK to auth for integrity)
CREATE TABLE IF NOT EXISTS suspend
(
    id            VARCHAR(255) NOT NULL PRIMARY KEY,
    user_id       VARCHAR(255) NOT NULL,
    suspend_at    DATETIME(6)  NOT NULL,
    version       INT          NULL,
    suspend_until DATE         NOT NULL,
    suspender     VARCHAR(255) NOT NULL,
    reason        VARCHAR(100) NOT NULL,
    CONSTRAINT fk_suspend_user FOREIGN KEY (user_id) REFERENCES auth (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci;

-- Consent table aligned with entity (remove versions column)
CREATE TABLE IF NOT EXISTS consent
(
    id           VARCHAR(255) NOT NULL PRIMARY KEY,
    user_id      VARCHAR(255) NOT NULL,
    agreement_at DATETIME(6)  NOT NULL,
    consent_type VARCHAR(255) NOT NULL,
    consent_url  VARCHAR(255) NOT NULL,
    CONSTRAINT fk_consent_id FOREIGN KEY (user_id) REFERENCES auth (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci;

-- Login status table to match LoginStatus entity
CREATE TABLE IF NOT EXISTS login_status
(
    id         VARCHAR(255) NOT NULL PRIMARY KEY,
    user_id    VARCHAR(255) NOT NULL,
    last_login DATETIME(6)  NULL

) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci;

-- auth.shedlock definition (must be preserved)
CREATE TABLE IF NOT EXISTS shedlock
(
    `name`       varchar(64)  NOT NULL,
    `lock_until` timestamp(3) NOT NULL,
    `locked_at`  timestamp(3) NOT NULL,
    `locked_by`  varchar(255) NOT NULL,
    PRIMARY KEY (`name`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;



create table if not exists consents_name
(
    id              VARCHAR(250) NOT NULL PRIMARY KEY,
    consent_name    varchar(255),
    consent_url     text,
    consent_version varchar(50)
)
