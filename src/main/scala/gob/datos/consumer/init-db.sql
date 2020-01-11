DROP TABLE IF EXISTS landings;

CREATE TABLE landings
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
    captura          INT     NOT NULL,
    created_at       TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (fecha, flota, puerto, provincia_id, departamento_id, categoria, especie, especie_agrupada)
);
