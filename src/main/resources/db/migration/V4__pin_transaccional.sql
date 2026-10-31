-- ===================================================================
-- V4__pin_transaccional.sql
-- PIN TRANSACCIONAL PARA AUTORIZACIÓN DE OPERACIONES FINANCIERAS
-- ===================================================================

ALTER TABLE usuarios
    ADD COLUMN IF NOT EXISTS pin_hash CHAR(60),
    ADD COLUMN IF NOT EXISTS pin_intentos_fallidos SMALLINT NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS pin_bloqueado_hasta TIMESTAMPTZ;

