-- H2 Database Schema for Testing
-- Drop existing tables for clean dev initialization (order matters due to FKs)

-- Consents name table (must be created before consent table due to FK)
CREATE TABLE IF NOT EXISTS consents_name
(
    id           VARCHAR(255) NOT NULL PRIMARY KEY,
    consent_name VARCHAR(255) NULL,
    version      VARCHAR(50)  NULL,
    consent_url  TEXT         NULL,
    required     BOOLEAN      NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS auth
(
    id           VARCHAR(255) NOT NULL PRIMARY KEY,
    created_at   TIMESTAMP    NULL,
    deleted_at   TIMESTAMP    NULL,
    email        VARCHAR(255) NOT NULL,
    password     VARCHAR(255) NULL,
    provider     VARCHAR(20)  NOT NULL,
    phone_number VARCHAR(255) NULL,
    status       VARCHAR(20)  NOT NULL,
    updated_at   TIMESTAMP    NULL,
    version      INT          NULL,
    user_role    VARCHAR(20)  NOT NULL
);

-- auth 생성 후 history 생성 (컬럼 타입/길이 일치)
CREATE TABLE IF NOT EXISTS history
(
    id                  VARCHAR(255) NOT NULL PRIMARY KEY,
    after_column_value  VARCHAR(255) NOT NULL,
    before_column_value VARCHAR(255) NULL,
    updated_at          TIMESTAMP    NULL,
    updated_column      VARCHAR(255) NOT NULL,
    version             INT          NULL,
    user_id             VARCHAR(255) NOT NULL,
    CONSTRAINT fk_history_user FOREIGN KEY (user_id) REFERENCES auth (id)
);

-- Withdraw table aligned with entity (PK is user_id, no version column)
CREATE TABLE IF NOT EXISTS withdraw
(
    user_id         VARCHAR(255) NOT NULL PRIMARY KEY,
    withdraw_at     TIMESTAMP    NOT NULL,
    withdraw_reason VARCHAR(100) NULL,
    CONSTRAINT fk_withdraw_user FOREIGN KEY (user_id) REFERENCES auth (id)
);

-- Suspend table (optional FK to auth for integrity)
CREATE TABLE IF NOT EXISTS suspend
(
    id            VARCHAR(255) NOT NULL PRIMARY KEY,
    user_id       VARCHAR(255) NOT NULL,
    suspend_at    TIMESTAMP    NOT NULL,
    version       INT          NULL,
    suspend_until DATE         NOT NULL,
    suspender     VARCHAR(255) NOT NULL,
    reason        VARCHAR(100) NOT NULL,
    CONSTRAINT fk_suspend_user FOREIGN KEY (user_id) REFERENCES auth (id)
);

-- Consent table aligned with entity
CREATE TABLE IF NOT EXISTS consent
(
    id           VARCHAR(255) NOT NULL PRIMARY KEY,
    user_id      VARCHAR(255) NOT NULL,
    consent_id   VARCHAR(255) NOT NULL,
    consented_at TIMESTAMP    NOT NULL,
    CONSTRAINT fk_consent_user FOREIGN KEY (user_id) REFERENCES auth (id),
    CONSTRAINT fk_consent_table FOREIGN KEY (consent_id) REFERENCES consents_name (id)
);

-- Login status table to match LoginStatus entity
CREATE TABLE IF NOT EXISTS login_status
(
    user_id    VARCHAR(255) NOT NULL PRIMARY KEY,
    last_login TIMESTAMP    NULL,
    CONSTRAINT fk_login_status_user FOREIGN KEY (user_id) REFERENCES auth (id)
);

-- auth.shedlock definition (must be preserved)
CREATE TABLE IF NOT EXISTS shedlock
(
    name       VARCHAR(64)  NOT NULL,
    lock_until TIMESTAMP    NOT NULL,
    locked_at  TIMESTAMP    NOT NULL,
    locked_by  VARCHAR(255) NOT NULL,
    PRIMARY KEY (name)
);
