--liquibase formatted sql
-- changeset HuzovaAlina:2026-07-16-create-payment-cards-table
CREATE TABLE IF NOT EXISTS payment_cards (

    id                 BIGSERIAL,
    user_id            BIGINT           NOT NULL,
    number             VARCHAR(19)      NOT NULL,
    holder             VARCHAR(255)     NOT NULL,
    expiration_date    DATE             NOT NULL,
    active             BOOLEAN          NOT NULL DEFAULT true,
    created_at         TIMESTAMPTZ      NOT NULL DEFAULT now(),
    updated_at         TIMESTAMPTZ      NOT NULL DEFAULT now(),

    CONSTRAINT payment_cards_pkey           PRIMARY KEY(id),
    CONSTRAINT payment_cards_user_id_fkey   FOREIGN KEY (user_id) REFERENCES users (id)
                                            ON DELETE CASCADE
                                            ON UPDATE CASCADE,
    CONSTRAINT payment_cards_number_uniq    UNIQUE (number)

);

CREATE INDEX IF NOT EXISTS payment_cards_user_id_idx ON payment_cards(user_id);

-- rollback DROP INDEX payment_cards_user_id_idx;
-- rollback DROP TABLE payment_cards;