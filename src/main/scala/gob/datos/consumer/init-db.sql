DROP TABLE IF EXISTS records;

-- TODO make primary key
CREATE TABLE records
(
    fecha            VARCHAR NOT NULL,
    flota            VARCHAR NOT NULL,
    puerto           VARCHAR NOT NULL,
    provincia        VARCHAR NOT NULL,
    provincia_id     INT     NOT NULL,
    departamento     VARCHAR NOT NULL,
    departamento_id  INT     NOT NULL,
    latitud          FLOAT,
    longitud         FLOAT,
    categoria        VARCHAR NOT NULL,
    especie          VARCHAR NOT NULL,
    especie_agrupada VARCHAR NOT NULL,
    captura          INT     NOT NULL
);
