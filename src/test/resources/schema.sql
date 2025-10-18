-- Auth 테이블
CREATE TABLE IF NOT EXISTS auth
(
    id         VARCHAR(255) PRIMARY KEY,
    email      VARCHAR(255) NOT NULL,
    password   VARCHAR(255),
    provider   VARCHAR(50),
    status     VARCHAR(50),
    user_role  VARCHAR(50),
    phone_number VARCHAR(20),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    version    BIGINT DEFAULT 0
);

-- Consent 테이블
CREATE TABLE IF NOT EXISTS consent
(
    id          VARCHAR(255) PRIMARY KEY,
    user_id     VARCHAR(255),
    consent_type VARCHAR(100),
    consent_url VARCHAR(500),
    agreement_at TIMESTAMP,
    version     VARCHAR(50),
    FOREIGN KEY (user_id) REFERENCES auth (id) ON DELETE CASCADE
);

-- History 테이블
CREATE TABLE IF NOT EXISTS history
(
    id                 VARCHAR(255) PRIMARY KEY,
    user_id            VARCHAR(255),
    updated_column     VARCHAR(100),
    before_column_value TEXT,
    after_column_value TEXT,
    updated_at         TIMESTAMP,
    version            VARCHAR(50),
    FOREIGN KEY (user_id) REFERENCES auth (id) ON DELETE CASCADE
);

-- Withdraw 테이블
CREATE TABLE IF NOT EXISTS withdraw
(
    user_id     VARCHAR(255) PRIMARY KEY,
    withdraw_reason TEXT,
    withdraw_at TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES auth (id) ON DELETE CASCADE
);

-- LoginStatus 테이블
CREATE TABLE IF NOT EXISTS login_status
(
    user_id VARCHAR(255) PRIMARY KEY,
    last_login TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES auth (id) ON DELETE CASCADE
);

-- Suspend 테이블
CREATE TABLE IF NOT EXISTS suspend
(
    id         VARCHAR(255) PRIMARY KEY,
    user_id    VARCHAR(255),
    reason     TEXT,
    suspend_at TIMESTAMP,
    suspend_until DATE,
    suspender  VARCHAR(255),
    version    BIGINT DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES auth (id) ON DELETE CASCADE
);

-- ConsentsTable (consents_name) 테이블 - 다른 컴포넌트가 요구하는 테이블
CREATE TABLE IF NOT EXISTS consents_name
(
    id          VARCHAR(255) PRIMARY KEY,
    consent_name VARCHAR(255),
    version     VARCHAR(50),
    consent_url VARCHAR(500),
    required    BOOLEAN DEFAULT false
);
