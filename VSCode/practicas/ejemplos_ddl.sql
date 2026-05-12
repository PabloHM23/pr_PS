-- Ejemplo 1: Tabla Simple
CREATE TABLE usuarios (
    id INT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    edad INT
);

-- Ejemplo 2: Tabla con más restricciones
CREATE TABLE empleados (
    id INT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE,
    telefono VARCHAR(15),
    fecha_contratacion DATE NOT NULL,
    salario NUMERIC(10,2),
    id_depto INT
);

-- Ejemplo 3: Tabla con DEFAULT
CREATE TABLE productos (
    id INT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(500),
    precio NUMERIC(10,2) NOT NULL,
    stock INT DEFAULT 0,
    activo INT DEFAULT 1
);

-- Ejemplo 4: Tabla con fecha
CREATE TABLE ordenes (
    id INT PRIMARY KEY,
    numero_orden VARCHAR(20) NOT NULL UNIQUE,
    fecha_orden DATE NOT NULL,
    fecha_entrega DATE,
    id_cliente INT,
    total NUMERIC(12,2)
);

-- Ejemplo 5: Tabla de auditoría
CREATE TABLE auditoria (
    id INT PRIMARY KEY AUTO_INCREMENT,
    tabla VARCHAR(50) NOT NULL,
    operacion VARCHAR(20),
    usuario VARCHAR(50),
    fecha_cambio DATETIME,
    descripcion VARCHAR(500)
);
