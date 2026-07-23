--liquibase formatted sql
-- changeset HuzovaAlina:2026-07-16-create-users-table
CREATE TABLE IF NOT EXISTS users (

    id              BIGSERIAL,
    name            VARCHAR(255)    NOT NULL,
    surname         VARCHAR(255)    NOT NULL,
    birth_date      DATE            NOT NULL,
    email           VARCHAR(255)    NOT NULL,
    active          BOOLEAN         NOT NULL DEFAULT true,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT now(),

    CONSTRAINT users_pkey           PRIMARY KEY (id),
    CONSTRAINT users_email_uniq     UNIQUE (email)

);

-- rollback DROP TABLE users;