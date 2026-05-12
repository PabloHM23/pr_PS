# Práctica 06: Analizador DDL - Generador de Tabla Semántica

## Descripción General

Este programa implementa un analizador de sentencias DDL (Data Definition Language) de SQL que genera una **tabla semántica** con información detallada sobre las tablas, atributos y restricciones definidas en el código.

## Requisitos del Proyecto

La aplicación debe constar de **3 módulos principales**:

### 1. **Módulo de Entrada**
- Interfaz gráfica (GUI) para recibir sentencias DDL
- Soporte para múltiples instrucciones `CREATE TABLE`
- Permite editar el código DDL en tiempo real
- Botones para analizar y limpiar la entrada

### 2. **Módulo de Análisis**
- **Escáner Léxico**: Tokenización del código DDL
  - Identifica palabras reservadas
  - Detecta delimitadores y operadores
  - Genera identificadores y constantes
  
- **Análisis Sintáctico**: Validación de estructura
  - Verifica estructura CREATE TABLE
  - Analiza definición de columnas
  - Procesa restricciones (PRIMARY KEY, FOREIGN KEY, etc.)
  
- **Generación de Tabla Semántica**: Recopilación de metadatos
  - Nombre de tabla
  - Atributos y sus propiedades
  - Restricciones y validaciones

### 3. **Módulo de Resultados**
Muestra la información extraída en múltiples vistas:
- **Tablas Definidas**: Listado de tablas con cantidad de atributos y restricciones
- **Atributos**: Detalles de cada columna (nombre, tipo, longitud, restricciones)
- **Restricciones**: Restricciones de integridad definidas
- **Errores y Análisis**: Reporte de errores léxicos y sintácticos detectados

## Estructura del Código

### Clases Principales

#### `Token`
Representa un token generado por el escáner léxico.
```java
Token {
    String lexema;      // Contenido del token
    int tipo;          // Tipo (1=reservada, 4=identificador, 5=delimitador, etc.)
    int codigo;        // Código del token
    int linea;         // Línea donde se encontró
}
```

#### `Atributo`
Define una columna/atributo de una tabla.
```java
Atributo {
    String nombre;
    String tipo;
    String longitud;
    boolean notNull;
    boolean primaryKey;
    String valorDefault;
}
```

#### `Tabla`
Contiene información de una tabla DDL.
```java
Tabla {
    String nombre;
    List<Atributo> atributos;
    List<String> restricciones;
}
```

#### `AnalizadorDDL`
Motor principal de análisis que:
- Tokeniza el código DDL
- Analiza la estructura sintáctica
- Genera objetos de tabla y atributos

## Ejemplos de Uso

### Ejemplo 1: Tabla Simple
```sql
CREATE TABLE usuarios (
    id INT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    edad INT
);
```

**Resultado esperado:**
- Tabla: `usuarios`
- Atributos: 4 (id, nombre, email, edad)
- Restricciones: PRIMARY KEY (id), NOT NULL (nombre)

### Ejemplo 2: Múltiples Tablas
```sql
CREATE TABLE departamentos (
    id INT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL
);

CREATE TABLE empleados (
    id INT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    id_depto INT,
    salario NUMERIC(10,2)
);
```

**Resultado esperado:**
- 2 tablas definidas
- 4 atributos en departamentos
- 4 atributos en empleados

### Ejemplo 3: Tabla con Restricciones
```sql
CREATE TABLE productos (
    id INT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    precio NUMERIC(10,2) NOT NULL,
    stock INT DEFAULT 0
);
```

**Resultado esperado:**
- Tabla: `productos`
- Atributos con restricciones detectadas
- Valores por defecto registrados

## Tipos de Datos Soportados

- `INT`, `INTEGER`: Números enteros
- `VARCHAR`, `CHAR`: Texto variable y fijo
- `NUMERIC`, `DECIMAL`: Números decimales
- `DATE`, `DATETIME`: Fechas y timestamps

## Palabras Reservadas Reconocidas

- CREATE, TABLE
- PRIMARY, KEY, FOREIGN, REFERENCES
- NOT, NULL, UNIQUE, CHECK
- DEFAULT, AUTO_INCREMENT
- Tipos de datos (VARCHAR, INT, NUMERIC, etc.)

## Características Principales

✅ **Análisis Léxico Completo**
- Tokenización de código DDL
- Identificación de palabras reservadas
- Detección de identificadores y constantes

✅ **Análisis Sintáctico**
- Validación de estructura CREATE TABLE
- Verificación de atributos y tipos
- Procesamiento de restricciones

✅ **Tabla Semántica Generada**
- Metadatos completos de tablas
- Información detallada de columnas
- Listado de restricciones

✅ **Interfaz Interactiva**
- GUI amigable con Swing
- Edición en tiempo real
- Múltiples vistas de resultados

✅ **Manejo de Errores**
- Detección de errores léxicos
- Detección de errores sintácticos
- Reporte detallado en consola

## Cómo Ejecutar

### Compilación
```bash
javac -encoding UTF-8 VSCode/practicas/pr06.java
```

### Ejecución
```bash
java VSCode.practicas.pr06
```

## Interfaz Gráfica

La ventana principal contiene:

**Lado Izquierdo (Módulo de Entrada):**
- Área de texto para ingresar DDL
- Botón "Analizar DDL" para procesar
- Botón "Limpiar" para resetear

**Lado Derecho (Módulo de Resultados):**
- Tab "Tablas Definidas": Resumen de tablas
- Tab "Atributos": Detalles de columnas
- Tab "Restricciones": Restricciones de integridad
- Tab "Errores y Análisis": Reporte de análisis

## Mejoras Futuras

- [ ] Soporte para más restricciones (CHECK, UNIQUE)
- [ ] Análisis de FOREIGN KEY completo
- [ ] Exportación de tabla semántica (XML, JSON)
- [ ] Validación de integridad referencial
- [ ] Soporte para vistas (CREATE VIEW)
- [ ] Soporte para índices (CREATE INDEX)

## Errores Comunes

| Error | Causa | Solución |
|-------|-------|----------|
| "Se esperaba TABLE" | CREATE sin TABLE | Escribir CREATE TABLE |
| "Se esperaba '('" | Falta paréntesis | Añadir ( después del nombre |
| "Se esperaba nombre de columna" | Tipo sin nombre | Especificar nombre antes del tipo |
| "Símbolo desconocido" | Carácter no reconocido | Usar delimitadores válidos |

## Requisitos Técnicos

- Java 8 o superior
- Compilador `javac`
- Entorno de ejecución Java (JRE)
- No requiere dependencias externas

## Autor

Estudiante de Programación Web
Fecha: 2026

---

**Nota**: Este programa cumple con los requisitos establecidos para la Práctica 06 de análisis léxico y sintáctico de DDL SQL.
