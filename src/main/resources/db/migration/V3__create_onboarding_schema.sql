-- ===================================================================
-- V3__create_onboarding_schema.sql
-- MIGRACIÓN DE TABLAS DE ONBOARDING, CLIENTES, BIOMETRÍA Y CUENTAS
-- ===================================================================

-- 1. TABLA: CLIENTES
CREATE TABLE IF NOT EXISTS clientes (
    fecha_creacion       TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    fecha_actualizacion  TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    fecha_baja           TIMESTAMPTZ,
    id                   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    fecha_nacimiento     DATE          NOT NULL,
    estado_civil         VARCHAR(20)   NOT NULL,  -- SOLTERO, CASADO, DIVORCIADO, VIUDO, UNION_LIBRE
    activo               BOOLEAN       NOT NULL DEFAULT TRUE,
    sexo                 CHAR(1)       NOT NULL,  -- M, F, X
    nacionalidad         CHAR(3)       NOT NULL DEFAULT 'MEX',
    curp                 CHAR(18)      NOT NULL,
    telefono_movil       CHAR(10)      NOT NULL,
    telefono_alternativo CHAR(10),
    ingreso_mensual      NUMERIC(15,2) NOT NULL,
    rfc                  VARCHAR(13)   NOT NULL,
    nombre               VARCHAR(50)   NOT NULL,
    segundo_nombre       VARCHAR(50),
    apellido_paterno     VARCHAR(50)   NOT NULL,
    apellido_materno     VARCHAR(50)   NOT NULL,
    correo               VARCHAR(100)  NOT NULL,
    ocupacion            VARCHAR(100)  NOT NULL,
    empresa              VARCHAR(100)  NOT NULL,

    CONSTRAINT uq_clientes_curp   UNIQUE (curp),
    CONSTRAINT uq_clientes_rfc    UNIQUE (rfc),
    CONSTRAINT uq_clientes_correo UNIQUE (correo),
    CONSTRAINT chk_ingreso_positivo CHECK (ingreso_mensual > 0),
    CONSTRAINT chk_clientes_sexo    CHECK (sexo IN ('M', 'F', 'X')),
    CONSTRAINT chk_clientes_ecivil  CHECK (estado_civil IN ('SOLTERO', 'CASADO', 'DIVORCIADO', 'VIUDO', 'UNION_LIBRE'))
);

CREATE INDEX IF NOT EXISTS idx_clientes_apellidos
    ON clientes(apellido_paterno, apellido_materno);
CREATE INDEX IF NOT EXISTS idx_clientes_fecha_creacion
    ON clientes USING BRIN (fecha_creacion);

-- 2. TABLA: DOMICILIOS
CREATE TABLE IF NOT EXISTS domicilios (
    fecha_creacion      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    fecha_actualizacion TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    cliente_id          BIGINT       NOT NULL,
    codigo_postal       CHAR(5)      NOT NULL,
    pais                CHAR(3)      NOT NULL DEFAULT 'MEX',
    numero_exterior     VARCHAR(20)  NOT NULL,
    numero_interior     VARCHAR(20),
    estado              VARCHAR(50)  NOT NULL,
    calle               VARCHAR(100) NOT NULL,
    colonia             VARCHAR(100) NOT NULL,
    municipio           VARCHAR(100) NOT NULL,

    CONSTRAINT uq_domicilio_cliente UNIQUE (cliente_id),
    CONSTRAINT fk_domicilio_cliente FOREIGN KEY (cliente_id)
        REFERENCES clientes(id) ON DELETE RESTRICT
);

-- 3. TABLA: CUENTAS BANCARIAS
CREATE TABLE IF NOT EXISTS cuentas (
    fecha_creacion      TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    fecha_actualizacion TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    fecha_bloqueo       TIMESTAMPTZ,
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    cliente_id          BIGINT        NOT NULL,
    version_lock        BIGINT        NOT NULL DEFAULT 0,
    estatus             VARCHAR(20)   NOT NULL DEFAULT 'ACTIVA',  -- ACTIVA, BLOQUEADA, CANCELADA
    activa              BOOLEAN       NOT NULL DEFAULT TRUE,
    saldo               NUMERIC(15,2) NOT NULL DEFAULT 0.00,
    numero_cuenta       VARCHAR(20)   NOT NULL,
    motivo_bloqueo      VARCHAR(255),

    CONSTRAINT uq_cuentas_numero_cuenta UNIQUE (numero_cuenta),
    CONSTRAINT fk_cuentas_cliente FOREIGN KEY (cliente_id)
        REFERENCES clientes(id) ON DELETE RESTRICT,
    CONSTRAINT chk_saldo_no_negativo CHECK (saldo >= 0.00),
    CONSTRAINT chk_estatus_cuenta CHECK (estatus IN ('ACTIVA', 'BLOQUEADA', 'CANCELADA'))
) WITH (fillfactor = 85);

CREATE INDEX IF NOT EXISTS idx_cuentas_cliente_id ON cuentas(cliente_id);

-- 4. TABLA: USUARIOS DE ACCESO
CREATE TABLE IF NOT EXISTS usuarios (
    fecha_creacion      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    fecha_actualizacion TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    cliente_id          BIGINT       NOT NULL,
    activo              BOOLEAN      NOT NULL DEFAULT TRUE,
    password_hash       CHAR(60)     NOT NULL,
    rol                 VARCHAR(30)  NOT NULL DEFAULT 'ROLE_CLIENTE',
    correo              VARCHAR(100) NOT NULL,

    CONSTRAINT uq_usuarios_cliente UNIQUE (cliente_id),
    CONSTRAINT uq_usuarios_correo  UNIQUE (correo),
    CONSTRAINT fk_usuarios_cliente FOREIGN KEY (cliente_id)
        REFERENCES clientes(id) ON DELETE RESTRICT
);

-- 5. TABLA: BIOMETRÍA Y RECONOCIMIENTO FACIAL
CREATE TABLE IF NOT EXISTS biometria_cliente (
    fecha_captura       TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    fecha_actualizacion TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    cliente_id          BIGINT        NOT NULL,
    liveness_score      NUMERIC(5,4)  DEFAULT 0.0000,
    estatus_facial      VARCHAR(20)   NOT NULL DEFAULT 'APROBADO',  -- APROBADO, RECHAZADO, PENDIENTE
    sha256_foto         CHAR(64),
    vector_facial       TEXT,
    foto_rostro         BYTEA,
    template_dactilar   BYTEA,

    CONSTRAINT uq_biometria_cliente UNIQUE (cliente_id),
    CONSTRAINT fk_biometria_cliente FOREIGN KEY (cliente_id)
        REFERENCES clientes(id) ON DELETE RESTRICT,
    CONSTRAINT chk_biometria_liveness CHECK (liveness_score BETWEEN 0.0000 AND 1.0000),
    CONSTRAINT chk_biometria_estatus  CHECK (estatus_facial IN ('APROBADO', 'RECHAZADO', 'PENDIENTE'))
);

ALTER TABLE biometria_cliente ALTER COLUMN foto_rostro       SET STORAGE EXTERNAL;
ALTER TABLE biometria_cliente ALTER COLUMN template_dactilar SET STORAGE EXTERNAL;

-- 6. TABLA: AUDITORÍA DE EVENTOS DE CUENTAS
CREATE TABLE IF NOT EXISTS historial_bloqueo_cuenta (
    fecha_evento        TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    cuenta_id           BIGINT       NOT NULL,
    tipo_evento         VARCHAR(20)  NOT NULL,  -- BLOQUEO, DESBLOQUEO, APERTURA, CANCELACION
    usuario_operador    VARCHAR(100) NOT NULL,
    motivo              VARCHAR(255) NOT NULL,

    CONSTRAINT fk_historial_cuenta FOREIGN KEY (cuenta_id)
        REFERENCES cuentas(id) ON DELETE CASCADE,
    CONSTRAINT chk_historial_tipo CHECK (tipo_evento IN ('BLOQUEO', 'DESBLOQUEO', 'APERTURA', 'CANCELACION'))
);

CREATE INDEX IF NOT EXISTS idx_historial_cuenta_fecha
    ON historial_bloqueo_cuenta(cuenta_id, fecha_evento);
