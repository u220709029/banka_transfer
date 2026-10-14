-- =============================================
-- V1__init_schema.sql
-- Veritabanı şema kurulumu: users, accounts, transaction_histories
-- PostgreSQL uyumlu sözdizimi
-- =============================================

-- 1. users tablosu
CREATE TABLE IF NOT EXISTS users (
    id              BIGSERIAL       NOT NULL,
    first_name      VARCHAR(255),
    last_name       VARCHAR(255),
    username        VARCHAR(255),
    email           VARCHAR(255)    NOT NULL,
    password        VARCHAR(255)    NOT NULL,
    phone_number    VARCHAR(255),
    role            VARCHAR(255)    DEFAULT 'USER',
    created_at      TIMESTAMP,
    updated_at      TIMESTAMP,

    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uk_users_email UNIQUE (email)
);

-- 2. accounts tablosu
CREATE TABLE IF NOT EXISTS accounts (
    id              BIGSERIAL       NOT NULL,
    iban            VARCHAR(255)    NOT NULL,
    account_number  VARCHAR(255),
    balance         NUMERIC(19, 4)  NOT NULL DEFAULT 0.0000,
    currency        VARCHAR(255)    DEFAULT 'TRY',
    active          BOOLEAN         DEFAULT TRUE,
    user_id         BIGINT          NOT NULL,
    created_at      TIMESTAMP,
    updated_at      TIMESTAMP,

    CONSTRAINT pk_accounts PRIMARY KEY (id),
    CONSTRAINT uk_accounts_iban UNIQUE (iban),
    CONSTRAINT uk_accounts_account_number UNIQUE (account_number),
    CONSTRAINT fk_accounts_user FOREIGN KEY (user_id) REFERENCES users (id)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

-- 3. transaction_histories tablosu
CREATE TABLE IF NOT EXISTS transaction_histories (
    id                  BIGSERIAL       NOT NULL,
    account_id          BIGINT          NOT NULL,
    target_account_id   BIGINT,
    amount              NUMERIC(19, 4)  NOT NULL,
    source_iban         VARCHAR(255),
    target_iban         VARCHAR(255),
    transaction_type    VARCHAR(255),
    description         VARCHAR(255),
    status              VARCHAR(255),
    transaction_date    TIMESTAMP,

    CONSTRAINT pk_transaction_histories PRIMARY KEY (id),
    CONSTRAINT fk_txn_account FOREIGN KEY (account_id) REFERENCES accounts (id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT fk_txn_target_account FOREIGN KEY (target_account_id) REFERENCES accounts (id)
        ON UPDATE CASCADE
        ON DELETE SET NULL
);
