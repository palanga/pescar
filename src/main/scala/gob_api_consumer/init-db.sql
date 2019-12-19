DROP DATABASE IF EXISTS postgres;

CREATE DATABASE postgres;

DROP TABLE IF EXISTS records;

CREATE TABLE records
(
    captura          INT     NOT NULL,
    categoria        VARCHAR NOT NULL,
    departamento     VARCHAR NOT NULL,
    departamento_id  INT     NOT NULL,
    especie          VARCHAR NOT NULL,
    especie_agrupada VARCHAR NOT NULL,
    fecha            VARCHAR NOT NULL,
    flota            VARCHAR NOT NULL,
    latitud          FLOAT,
    longitud         FLOAT,
    provincia        VARCHAR NOT NULL,
    provincia_id     INT     NOT NULL,
    puerto           VARCHAR NOT NULL
);
