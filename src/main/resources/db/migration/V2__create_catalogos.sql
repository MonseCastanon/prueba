-- ===================================================================
-- V2__create_catalogos.sql
-- TABLAS DE CATÁLOGOS: PAÍSES, ESTADOS Y CÓDIGOS POSTALES
-- ===================================================================

-- 1. CATÁLOGO DE PAÍSES
CREATE TABLE IF NOT EXISTS cat_paises (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    codigo_iso_alfa3    CHAR(3)      NOT NULL,
    nombre              VARCHAR(100) NOT NULL,
    activo              BOOLEAN      NOT NULL DEFAULT TRUE,

    CONSTRAINT uq_cat_paises_iso UNIQUE (codigo_iso_alfa3)
);

-- 2. CATÁLOGO DE ESTADOS / ENTIDADES FEDERATIVAS
CREATE TABLE IF NOT EXISTS cat_estados (
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    pais_id      BIGINT       NOT NULL,
    clave_estado VARCHAR(10)  NOT NULL,
    nombre       VARCHAR(100) NOT NULL,
    activo       BOOLEAN      NOT NULL DEFAULT TRUE,

    CONSTRAINT fk_cat_estados_pais FOREIGN KEY (pais_id)
        REFERENCES cat_paises(id) ON DELETE RESTRICT,
    CONSTRAINT uq_cat_estados_clave UNIQUE (pais_id, clave_estado)
);

CREATE INDEX IF NOT EXISTS idx_cat_estados_pais ON cat_estados(pais_id);

-- 3. CATÁLOGO DE CÓDIGOS POSTALES Y MUNICIPIOS
CREATE TABLE IF NOT EXISTS cat_codigos_postales (
    id                 BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    estado_id          BIGINT       NOT NULL,
    codigo_postal      CHAR(5)      NOT NULL,
    municipio_alcaldia VARCHAR(100) NOT NULL,
    colonia            VARCHAR(100) NOT NULL,
    activo             BOOLEAN      NOT NULL DEFAULT TRUE,

    CONSTRAINT fk_cat_cp_estado FOREIGN KEY (estado_id)
        REFERENCES cat_estados(id) ON DELETE RESTRICT
);

CREATE INDEX IF NOT EXISTS idx_cat_cp_codigo ON cat_codigos_postales(codigo_postal);
CREATE INDEX IF NOT EXISTS idx_cat_cp_estado ON cat_codigos_postales(estado_id);

-- INSERTS SEMILLA BÁSICOS PARA OPERACIÓN INICIAL
INSERT INTO cat_paises (codigo_iso_alfa3, nombre, activo) VALUES
    ('MEX', 'MEXICO', TRUE),
    ('USA', 'ESTADOS UNIDOS DE AMERICA', TRUE),
    ('ESP', 'ESPAÑA', TRUE)
ON CONFLICT (codigo_iso_alfa3) DO NOTHING;

INSERT INTO cat_estados (pais_id, clave_estado, nombre, activo) VALUES
    (1, 'CDMX', 'CIUDAD DE MEXICO', TRUE),
    (1, 'MEX', 'ESTADO DE MEXICO', TRUE),
    (1, 'JAL', 'JALISCO', TRUE),
    (1, 'NLE', 'NUEVO LEON', TRUE)
ON CONFLICT (pais_id, clave_estado) DO NOTHING;

INSERT INTO cat_codigos_postales (estado_id, codigo_postal, municipio_alcaldia, colonia, activo) VALUES
    (1, '03940', 'BENITO JUAREZ', 'CREDITO CONSTRUCTOR', TRUE),
    (1, '06000', 'CUAUHTEMOC', 'CENTRO', TRUE),
    (1, '01000', 'ALVARO OBREGON', 'SAN ANGEL', TRUE),
    (3, '44100', 'GUADALAJARA', 'CENTRO', TRUE),
    (4, '64000', 'MONTERREY', 'CENTRO', TRUE);
