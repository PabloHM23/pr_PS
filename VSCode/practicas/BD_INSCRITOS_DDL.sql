-- ========================================
-- ESQUEMA DDL - BASE DE DATOS INSCRITOS
-- Práctica 07: Validación de Llaves Foráneas
-- ========================================

-- Tabla DEPARTAMENTOS
CREATE TABLE DEPARTAMENTOS (
    D# VARCHAR(3) PRIMARY KEY,
    NOMBRE VARCHAR(40) NOT NULL,
    UBICACION VARCHAR(20)
);

-- Tabla CARRERAS
CREATE TABLE CARRERAS (
    C# VARCHAR(3) PRIMARY KEY,
    NOMBRE VARCHAR(40) NOT NULL,
    DURACION VARCHAR(4),
    CREDITOS INT,
    D# VARCHAR(3) NOT NULL,
    CONSTRAINT FK_CARRERAS FOREIGN KEY (D#) REFERENCES DEPARTAMENTOS(D#)
);

-- Tabla ESTUDIANTES
CREATE TABLE ESTUDIANTES (
    E# VARCHAR(3) PRIMARY KEY,
    NOMBRE VARCHAR(40) NOT NULL,
    TELEFONO VARCHAR(10),
    CARRERA VARCHAR(3),
    CONSTRAINT FK_ESTUDIANTES_CARRERA FOREIGN KEY (CARRERA) REFERENCES CARRERAS(C#)
);

-- Tabla INSCRITOS
CREATE TABLE INSCRITOS (
    E# VARCHAR(3) NOT NULL,
    C# VARCHAR(3) NOT NULL,
    SEMESTRE INT,
    NOTA NUMERIC(3,1),
    PRIMARY KEY (E#, C#),
    CONSTRAINT FK_INSCRITOS_ESTUDIANTE FOREIGN KEY (E#) REFERENCES ESTUDIANTES(E#),
    CONSTRAINT FK_INSCRITOS_CARRERA FOREIGN KEY (C#) REFERENCES CARRERAS(C#)
);

-- ========================================
-- DATOS DE EJEMPLO
-- ========================================

-- Insertar Departamentos
INSERT INTO DEPARTAMENTOS VALUES ('D1', 'CIENCIAS', 'EDIFICIO A');
INSERT INTO DEPARTAMENTOS VALUES ('D2', 'INGENIERIA', 'EDIFICIO B');
INSERT INTO DEPARTAMENTOS VALUES ('D3', 'HUMANIDADES', 'EDIFICIO C');

-- Insertar Carreras
INSERT INTO CARRERAS VALUES ('C1', 'MATEMATICA', '2008', 120, 'D1');
INSERT INTO CARRERAS VALUES ('C2', 'FISICA', '2008', 110, 'D1');
INSERT INTO CARRERAS VALUES ('C3', 'ICI', '2009', 10, 'D2');
INSERT INTO CARRERAS VALUES ('C4', 'INGENIERIA CIVIL', '2010', 130, 'D2');

-- Insertar Estudiantes
INSERT INTO ESTUDIANTES VALUES ('E1', 'Juan Pérez', '2121212', 'C1');
INSERT INTO ESTUDIANTES VALUES ('E2', 'María López', '2222222', 'C3');
INSERT INTO ESTUDIANTES VALUES ('E3', 'Carlos García', '2323232', 'C2');

-- Insertar Inscritos
INSERT INTO INSCRITOS VALUES ('E1', 'C1', 1, 3.5);
INSERT INTO INSCRITOS VALUES ('E2', 'C3', 1, 4.0);
INSERT INTO INSCRITOS VALUES ('E3', 'C2', 1, 3.8);

-- ========================================
-- PRUEBAS DE ERRORES SEMÁNTICOS
-- ========================================

-- SENTENCIA 1: CORRECTA
-- D2 existe en DEPARTAMENTOS
-- INSERT INTO CARRERAS VALUES ('C5', 'SISTEMAS', '2011', 140, 'D2');

-- SENTENCIA 2: ERROR SEMÁNTICO
-- 100 NO existe en DEPARTAMENTOS.D#
-- INSERT INTO CARRERAS VALUES ('C6', 'CIVIL', '2012', 150, 100);

-- SENTENCIA 3: CORRECTA
-- C1 existe en CARRERAS
-- INSERT INTO ESTUDIANTES VALUES ('E4', 'Pedro Sánchez', '9999999', 'C1');

-- SENTENCIA 4: ERROR SEMÁNTICO
-- C99 NO existe en CARRERAS.C#
-- INSERT INTO ESTUDIANTES VALUES ('E5', 'Ana López', '8888888', 'C99');

-- SENTENCIA 5: CORRECTA
-- E1 y C1 existen
-- INSERT INTO INSCRITOS VALUES ('E1', 'C2', 2, 3.2);

-- SENTENCIA 6: ERROR SEMÁNTICO
-- E99 NO existe en ESTUDIANTES.E#
-- INSERT INTO INSCRITOS VALUES ('E99', 'C1', 1, 4.0);

-- SENTENCIA 7: ERROR SEMÁNTICO
-- C88 NO existe en CARRERAS.C#
-- INSERT INTO INSCRITOS VALUES ('E1', 'C88', 1, 3.5);

-- ========================================
-- RELACIONES DE LLAVES FORÁNEAS
-- ========================================
/*
DEPARTAMENTOS
    ↑
    | FK_CARRERAS
    |
CARRERAS (D#)
    ↑
    | FK_ESTUDIANTES_CARRERA
    |
ESTUDIANTES (CARRERA)

INSCRITOS
    ├─ FK_INSCRITOS_ESTUDIANTE → ESTUDIANTES.E#
    └─ FK_INSCRITOS_CARRERA → CARRERAS.C#
*/

-- ========================================
-- DESCRIPCIÓN DE ERRORES ESPERADOS
-- ========================================
/*
ERROR TIPO 1: Violación de FK_CARRERAS
- Sentencia: INSERT INTO CARRERAS VALUES ('C6', 'CIVIL', '2012', 150, 100)
- Error: La Sentencia INSERT está en conflicto con la restricción de Llave Foránea 'FK_CARRERAS'. 
         El conflicto ocurre en la BD 'INSCRITOS', tabla 'DEPARTAMENTOS', atributo 'D#'.
- Causa: El valor 100 no existe en DEPARTAMENTOS.D#

ERROR TIPO 2: Violación de FK_ESTUDIANTES_CARRERA
- Sentencia: INSERT INTO ESTUDIANTES VALUES ('E5', 'Ana López', '8888888', 'C99')
- Error: La Sentencia INSERT está en conflicto con la restricción de Llave Foránea 'FK_ESTUDIANTES_CARRERA'. 
         El conflicto ocurre en la BD 'INSCRITOS', tabla 'CARRERAS', atributo 'C#'.
- Causa: El valor C99 no existe en CARRERAS.C#

ERROR TIPO 3: Violación de FK_INSCRITOS_ESTUDIANTE
- Sentencia: INSERT INTO INSCRITOS VALUES ('E99', 'C1', 1, 4.0)
- Error: La Sentencia INSERT está en conflicto con la restricción de Llave Foránea 'FK_INSCRITOS_ESTUDIANTE'. 
         El conflicto ocurre en la BD 'INSCRITOS', tabla 'ESTUDIANTES', atributo 'E#'.
- Causa: El valor E99 no existe en ESTUDIANTES.E#

ERROR TIPO 4: Violación de FK_INSCRITOS_CARRERA
- Sentencia: INSERT INTO INSCRITOS VALUES ('E1', 'C88', 1, 3.5)
- Error: La Sentencia INSERT está en conflicto con la restricción de Llave Foránea 'FK_INSCRITOS_CARRERA'. 
         El conflicto ocurre en la BD 'INSCRITOS', tabla 'CARRERAS', atributo 'C#'.
- Causa: El valor C88 no existe en CARRERAS.C#
*/
