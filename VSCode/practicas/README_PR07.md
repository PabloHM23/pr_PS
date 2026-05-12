# Práctica 07: Traductor SQL - Validador de Llaves Foráneas

## Descripción General

Este programa implementa un **Traductor SQL** que analiza sentencias `INSERT` y detecta errores semánticos relacionados con **violaciones de restricciones de llaves foráneas (Foreign Keys)**.

## Requisitos del Proyecto

### 1. Reconocimiento de Errores Semánticos de Llave Foránea
El programa debe detectar cuando una sentencia `INSERT` intenta insertar un valor que **no existe** en la tabla referenciada por una llave foránea.

### 2. Estructura de Error Semántico
El mensaje de error debe incluir:
- **Nombre de la llave foránea**: `FK_CARRERAS`
- **Base de datos**: `INSCRITOS`
- **Tabla referenciada**: `DEPARTAMENTOS`
- **Atributo referenciado**: `D#`

### 3. Cobertura
El programa debe validar este tipo de error en **cualquier tabla** de la BD `INSCRITOS` que tenga llaves foráneas.

## Base de Datos INSCRITOS

### Estructura

#### Tabla DEPARTAMENTOS
```
D# (VARCHAR 3) PRIMARY KEY
NOMBRE (VARCHAR 40)
UBICACION (VARCHAR 20)
```

**Datos:**
- D1: CIENCIAS
- D2: INGENIERIA
- D3: HUMANIDADES

#### Tabla CARRERAS
```
C# (VARCHAR 3) PRIMARY KEY
NOMBRE (VARCHAR 40)
DURACION (VARCHAR 4)
CREDITOS (INT)
D# (VARCHAR 3) FOREIGN KEY → DEPARTAMENTOS.D#
```

**Restricción:** `FK_CARRERAS` → `DEPARTAMENTOS.D#`

**Datos:**
- C1: MATEMATICA, 2008, 120, D1
- C2: FISICA, 2008, 110, D1
- C3: ICI, 2009, 10, D2
- C4: INGENIERIA CIVIL, 2010, 130, D2

#### Tabla ESTUDIANTES
```
E# (VARCHAR 3) PRIMARY KEY
NOMBRE (VARCHAR 40)
TELEFONO (VARCHAR 10)
CARRERA (VARCHAR 3) FOREIGN KEY → CARRERAS.C#
```

**Restricción:** `FK_ESTUDIANTES_CARRERA` → `CARRERAS.C#`

#### Tabla INSCRITOS
```
E# (VARCHAR 3) PRIMARY KEY FOREIGN KEY → ESTUDIANTES.E#
C# (VARCHAR 3) PRIMARY KEY FOREIGN KEY → CARRERAS.C#
SEMESTRE (INT)
NOTA (NUMERIC)
```

**Restricciones:**
- `FK_INSCRITOS_ESTUDIANTE` → `ESTUDIANTES.E#`
- `FK_INSCRITOS_CARRERA` → `CARRERAS.C#`

## Ejemplos de Sentencias

### Ejemplo 1: Sentencia Correcta ✅
```sql
INSERT INTO CARRERAS VALUES ('C3','ICI','2009',10,'D2');
```

**Resultado:** `D2` existe en DEPARTAMENTOS → Sentencia Válida

### Ejemplo 2: Sentencia con Error Semántico ❌
```sql
INSERT INTO CARRERAS VALUES ('C3','ICI','2009',10,100);
```

**Error Detectado:**
```
ERROR [3001] Línea 2:
La Sentencia INSERT está en conflicto con la restricción de Llave Foránea 'FK_CARRERAS'. 
El conflicto ocurre en la BD 'INSCRITOS', tabla 'DEPARTAMENTOS', atributo 'D#'.
```

**Explicación:** `100` no existe en `DEPARTAMENTOS.D#` → Violación de restricción

### Ejemplo 3: Estudiante con Carrera Válida ✅
```sql
INSERT INTO ESTUDIANTES VALUES ('E4','Pedro Sánchez','9999999','C1');
```

**Resultado:** `C1` existe en CARRERAS → Sentencia Válida

### Ejemplo 4: Estudiante con Carrera Inválida ❌
```sql
INSERT INTO ESTUDIANTES VALUES ('E5','Ana López','8888888','C99');
```

**Error Detectado:**
```
ERROR [3001]:
La Sentencia INSERT está en conflicto con la restricción de Llave Foránea 'FK_ESTUDIANTES_CARRERA'. 
El conflicto ocurre en la BD 'INSCRITOS', tabla 'CARRERAS', atributo 'C#'.
```

## Estructura del Código

### Clases Principales

#### `BaseDatos`
Contiene toda la estructura y datos de la BD INSCRITOS.
- Inicializa tablas con sus columnas y restricciones
- Carga datos de ejemplo
- Proporciona acceso a tablas y llaves foráneas

#### `Tabla`
Representa una tabla en la BD.
```java
Tabla {
    String nombre;
    List<Columna> columnas;
    List<LlaveForeignKey> llavesForeignKey;
    List<String[]> filas;
    String primaryKey;
}
```

#### `LlaveForeignKey`
Define una restricción de llave foránea.
```java
LlaveForeignKey {
    String nombre;              // FK_CARRERAS
    String columnaLocal;        // D#
    String tablaReferenciada;   // DEPARTAMENTOS
    String columnaReferenciada; // D#
}
```

#### `AnalizadorSQL`
Motor principal de análisis.
- Procesa sentencias INSERT
- Valida restricciones de llaves foráneas
- Genera mensajes de error semántico

#### `ErrorSemantico`
Representa un error detectado.
```java
ErrorSemantico {
    String codigo;        // 3001, 2001, etc.
    String mensaje;       // Descripción del error
    String sentencia;     // Sentencia SQL que causó el error
    int linea;           // Línea donde ocurrió
}
```

## Algoritmo de Validación

### Proceso de Análisis de INSERT

1. **Extraer nombre de tabla** de la sentencia INSERT
2. **Verificar existencia** de la tabla en la BD
3. **Extraer columnas y valores** de la sentencia
4. **Validar consistencia** (# columnas = # valores)
5. **Recorrer llaves foráneas** de la tabla
6. Para cada llave foránea:
   - Obtener índice de columna local en la sentencia
   - Si está presente en INSERT:
     - Buscar valor a insertar
     - Buscar tabla referenciada
     - **Verificar si valor existe** en tabla referenciada
     - Si NO existe → **Generar error semántico**

### Validación de Llave Foránea

```
Para cada FK en Tabla:
  Si columna FK está en INSERT:
    valor_insertar = obtenerValor(columna FK)
    tabla_ref = obtenerTablaReferenciada(FK)
    col_ref = obtenerColumnaReferenciada(FK)
    
    Si valor_insertar NO existe en tabla_ref[col_ref]:
      ERROR: Violación de restricción FK
```

## Interfaz Gráfica

### Componentes

1. **Panel de Entrada**
   - Área de texto para ingresar sentencias SQL
   - Botón "Analizar SQL"
   - Botón "Limpiar"
   - Botón "Cargar Ejemplo"

2. **Panel de Resultados** (Pestañas)
   - **Errores Detectados**: Tabla de errores con línea, código y descripción
   - **Estructura BD INSCRITOS**: Tabla de columnas, tipos y restricciones
   - **Análisis Detallado**: Consola con detalles de procesamiento

## Códigos de Error

| Código | Tipo | Descripción |
|--------|------|-------------|
| 1001 | Sintáctico | Sentencia con sintaxis incorrecta |
| 1002 | Sintáctico | Tabla no existe |
| 1003 | Sintáctico | Número de columnas ≠ número de valores |
| 2001 | Semántico | Tabla referenciada no existe |
| 2002 | Semántico | Columna referenciada no existe |
| 3001 | Semántico | Violación de restricción de llave foránea |
| 5000 | Ejecución | Error general en procesamiento |

## Cómo Ejecutar

### Compilación
```bash
javac -encoding UTF-8 VSCode/practicas/pr07.java
```

### Ejecución
```bash
java VSCode.practicas.pr07
```

## Características Implementadas

✅ **Análisis de Sentencias INSERT**
- Extrae tabla, columnas y valores
- Valida sintaxis básica

✅ **Validación de Llaves Foráneas**
- Verifica existencia de valores en tablas referenciadas
- Genera mensajes de error específicos

✅ **Mensajes de Error Detallados**
- Incluye nombre de FK
- Incluye tabla y atributo referenciados
- Incluye nombre de BD

✅ **Base de Datos Completa**
- 4 tablas con relaciones
- Datos de ejemplo precargados
- Restricciones de FK implementadas

✅ **Interfaz Interactiva**
- Múltiples pestañas de resultado
- Análisis detallado en consola
- Visualización de estructura de BD

## Extensiones Posibles

- [ ] Validación de UPDATE y DELETE
- [ ] Validación de restricciones UNIQUE
- [ ] Validación de NOT NULL
- [ ] Validación de CHECK
- [ ] Exportación de errores a archivo
- [ ] Reparación automática de errores

## Notas Importantes

1. El programa carga automáticamente los datos de la BD INSCRITOS al iniciar
2. Las sentencias INSERT deben terminar con `;` o sin él
3. Los comentarios SQL (`--`) son ignorados
4. Se soportan múltiples sentencias en el área de entrada
5. Los valores de texto deben ir entre comillas simples (')

## Ejemplo de Uso Completo

```sql
-- Correcta: D2 existe en DEPARTAMENTOS
INSERT INTO CARRERAS VALUES ('C3','ICI','2009',10,'D2');

-- Error: 100 no existe en DEPARTAMENTOS
INSERT INTO CARRERAS VALUES ('C5','SISTEMAS','2010',120,100);

-- Correcta: C1 existe en CARRERAS
INSERT INTO ESTUDIANTES VALUES ('E4','Carlos López','8765432','C1');

-- Error: C99 no existe en CARRERAS
INSERT INTO ESTUDIANTES VALUES ('E5','María García','8754321','C99');
```

---

**Fecha:** Mayo 2026
**Cumple con:** Validación de errores semánticos de llaves foráneas en sentencias INSERT
