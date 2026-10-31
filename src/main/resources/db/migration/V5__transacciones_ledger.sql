-- ===================================================================
-- V5__transacciones_ledger.sql
-- MIGRACIÓN DE LEDGER INMUTABLE Y TRANSACCIONES DE PAGO DE SERVICIOS
-- ===================================================================

CREATE SEQUENCE IF NOT EXISTS seq_upc START WITH 100000;

CREATE TABLE IF NOT EXISTS transacciones_servicio (
    id                    UUID          PRIMARY KEY,
    cuenta_id             BIGINT        NOT NULL REFERENCES cuentas(id),
    id_documento          VARCHAR(40)   NOT NULL,
    upc                   VARCHAR(15)   NOT NULL,
    hora_local            VARCHAR(14)   NOT NULL,
    request_hash          CHAR(64)      NOT NULL,
    id_producto           INTEGER       NOT NULL,
    id_servicio           INTEGER       NOT NULL,
    referencia_enmascarada VARCHAR(30)  NOT NULL,
    payload_cifrado       TEXT,
    monto                 NUMERIC(15,2) NOT NULL CHECK (monto > 0),
    estado                VARCHAR(25)   NOT NULL,
    codigo_proveedor      VARCHAR(20),
    autorizacion_proveedor VARCHAR(60),
    id_ticket_proveedor   VARCHAR(60),
    intentos_conciliacion SMALLINT      NOT NULL DEFAULT 0,
    proximo_intento_en    TIMESTAMPTZ,
    fecha_creacion        TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    fecha_envio           TIMESTAMPTZ,
    fecha_resolucion      TIMESTAMPTZ,
    version_lock          BIGINT        NOT NULL DEFAULT 0,
    CONSTRAINT uq_tx_cuenta_documento UNIQUE (cuenta_id, id_documento),
    CONSTRAINT uq_tx_upc              UNIQUE (upc),
    CONSTRAINT chk_tx_estado CHECK (estado IN
      ('PENDIENTE','ENVIADA','EXITOSA','RECHAZADA','REVERSADA','INCIERTA','REVISION_MANUAL'))
);

CREATE INDEX IF NOT EXISTS idx_tx_conciliacion ON transacciones_servicio (estado, proximo_intento_en)
    WHERE estado IN ('ENVIADA','INCIERTA');

CREATE TABLE IF NOT EXISTS movimientos_cuenta (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    cuenta_id       BIGINT        NOT NULL REFERENCES cuentas(id),
    transaccion_id  UUID          NOT NULL REFERENCES transacciones_servicio(id),
    tipo            VARCHAR(10)   NOT NULL CHECK (tipo IN ('DEBITO','REVERSO')),
    monto           NUMERIC(15,2) NOT NULL CHECK (monto > 0),
    saldo_anterior  NUMERIC(15,2) NOT NULL,
    saldo_posterior NUMERIC(15,2) NOT NULL CHECK (saldo_posterior >= 0),
    fecha           TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_mov_tx_tipo UNIQUE (transaccion_id, tipo)
);

CREATE TABLE IF NOT EXISTS shedlock (
    name       VARCHAR(64)  NOT NULL PRIMARY KEY,
    lock_until TIMESTAMPTZ  NOT NULL,
    locked_at  TIMESTAMPTZ  NOT NULL,
    locked_by  VARCHAR(255) NOT NULL
);
