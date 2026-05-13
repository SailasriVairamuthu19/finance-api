CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE users (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email           VARCHAR(255) NOT NULL UNIQUE,
    password        VARCHAR(255) NOT NULL,
    full_name       VARCHAR(100) NOT NULL,
    phone_number    VARCHAR(15),
    role            VARCHAR(20)  NOT NULL DEFAULT 'USER',
    is_active       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE TABLE accounts (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    account_number  VARCHAR(20)  NOT NULL UNIQUE,
    account_type    VARCHAR(20)  NOT NULL,
    balance         DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    currency        VARCHAR(3)   NOT NULL DEFAULT 'INR',
    is_active       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE TABLE transactions (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id       UUID          NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,
    type             VARCHAR(20)   NOT NULL,
    amount           DECIMAL(15,2) NOT NULL,
    description      VARCHAR(255),
    reference_number VARCHAR(50)   UNIQUE,
    status           VARCHAR(20)   NOT NULL DEFAULT 'SUCCESS',
    created_at       TIMESTAMP     NOT NULL DEFAULT now()
);

CREATE INDEX idx_user_email          ON users(email);
CREATE INDEX idx_account_user_id     ON accounts(user_id);
CREATE INDEX idx_account_number      ON accounts(account_number);
CREATE INDEX idx_transaction_account ON transactions(account_id);
CREATE INDEX idx_transaction_created ON transactions(created_at DESC);