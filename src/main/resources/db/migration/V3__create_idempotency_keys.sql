
CREATE TABLE IF NOT EXISTS idempotency_keys (
    id                   BIGSERIAL      NOT NULL,
    idempotency_key      VARCHAR(255)   NOT NULL,
    user_id              BIGINT         NOT NULL,
    request_fingerprint  VARCHAR(64)    NOT NULL,
    transaction_id       BIGINT         NOT NULL,
    created_at           TIMESTAMP      NOT NULL,

    CONSTRAINT pk_idempotency_keys PRIMARY KEY (id),
    CONSTRAINT uk_idempotency_keys_key UNIQUE (idempotency_key),
    CONSTRAINT fk_idempotency_txn FOREIGN KEY (transaction_id)
        REFERENCES transaction_histories (id)
);