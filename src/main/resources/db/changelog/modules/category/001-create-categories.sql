--liquibase formatted sql
--changeset smartshop:category-001-create-categories

CREATE SCHEMA IF NOT EXISTS smartshop;

CREATE TABLE smartshop.categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),

    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_categories_name_not_blank
        CHECK (LENGTH(BTRIM(name)) > 0)
);

CREATE UNIQUE INDEX uq_categories_name_ci
    ON smartshop.categories (LOWER(BTRIM(name)));