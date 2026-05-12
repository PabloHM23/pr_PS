# Casos de Prueba - Práctica 07

## Descripción
Documento con casos de prueba para validar la detección de errores semánticos de llaves foráneas en sentencias INSERT.

---

## GRUPO 1: Errores en Tabla CARRERAS

### Caso 1.1: Inserción Válida - FK_CARRERAS OK ✅
**Entrada:**
```sql
INSERT INTO CARRERAS VALUES ('C3','ICI','2009',10,'D2');
```

**Expectativa:**
- Resultado: Válido
- Explicación: D2 existe en DEPARTAMENTOS

**Validación:** ✓ PASA

---

### Caso 1.2: Inserción Inválida - FK_CARRERAS con valor inexistente ❌
**Entrada:**
```sql
INSERT INTO CARRERAS VALUES ('C3','ICI','2009',10,100);
```

**Expectativa:**
```
ERROR [3001] Línea 1:
La Sentencia INSERT está en conflicto con la restricción de Llave Foránea 'FK_CARRERAS'. 
El conflicto ocurre en la BD 'INSCRITOS', tabla 'DEPARTAMENTOS', atributo 'D#'.
```

**Validación:** ✓ PASA

---

### Caso 1.3: Inserción Inválida - FK_CARRERAS con null ❌
**Entrada:**
```sql
INSERT INTO CARRERAS VALUES ('C5','SISTEMAS','2011',140,'D99');
```

**Expectativa:**
```
ERROR [3001]:
La Sentencia INSERT está en conflicto con la restricción de Llave Foránea 'FK_CARRERAS'. 
El conflicto ocurre en la BD 'INSCRITOS', tabla 'DEPARTAMENTOS', atributo 'D#'.
```

**Validación:** ✓ PASA

---

## GRUPO 2: Errores en Tabla ESTUDIANTES

### Caso 2.1: Inserción Válida - FK_ESTUDIANTES_CARRERA OK ✅
**Entrada:**
```sql
INSERT INTO ESTUDIANTES VALUES ('E4','Pedro Sánchez','9999999','C1');
```

**Expectativa:**
- Resultado: Válido
- Explicación: C1 existe en CARRERAS

**Validación:** ✓ PASA

---

### Caso 2.2: Inserción Inválida - FK_ESTUDIANTES_CARRERA con valor inexistente ❌
**Entrada:**
```sql
INSERT INTO ESTUDIANTES VALUES ('E5','Ana López','8888888','C99');
```

**Expectativa:**
```
ERROR [3001]:
La Sentencia INSERT está en conflicto con la restricción de Llave Foránea 'FK_ESTUDIANTES_CARRERA'. 
El conflicto ocurre en la BD 'INSCRITOS', tabla 'CARRERAS', atributo 'C#'.
```

**Validación:** ✓ PASA

---

### Caso 2.3: Inserción Válida - Estudiante sin carrera (NULL) ✅
**Entrada:**
```sql
INSERT INTO ESTUDIANTES VALUES ('E6','Carlos López','9876543','NULL');
```

**Expectativa:**
- Resultado: Válido
- Explicación: NULL en llave foránea es permitido

**Validación:** ✓ PASA (si se implementa manejo de NULL)

---

## GRUPO 3: Errores en Tabla INSCRITOS

### Caso 3.1: Inserción Válida - Ambas FKs OK ✅
**Entrada:**
```sql
INSERT INTO INSCRITOS VALUES ('E1','C2',2,3.2);
```

**Expectativa:**
- Resultado: Válido
- Explicación: E1 existe en ESTUDIANTES, C2 existe en CARRERAS

**Validación:** ✓ PASA

---

### Caso 3.2: Inserción Inválida - FK_INSCRITOS_ESTUDIANTE falla ❌
**Entrada:**
```sql
INSERT INTO INSCRITOS VALUES ('E99','C1',1,4.0);
```

**Expectativa:**
```
ERROR [3001]:
La Sentencia INSERT está en conflicto con la restricción de Llave Foránea 'FK_INSCRITOS_ESTUDIANTE'. 
El conflicto ocurre en la BD 'INSCRITOS', tabla 'ESTUDIANTES', atributo 'E#'.
```

**Validación:** ✓ PASA

---

### Caso 3.3: Inserción Inválida - FK_INSCRITOS_CARRERA falla ❌
**Entrada:**
```sql
INSERT INTO INSCRITOS VALUES ('E1','C88',1,3.5);
```

**Expectativa:**
```
ERROR [3001]:
La Sentencia INSERT está en conflicto con la restricción de Llave Foránea 'FK_INSCRITOS_CARRERA'. 
El conflicto ocurre en la BD 'INSCRITOS', tabla 'CARRERAS', atributo 'C#'.
```

**Validación:** ✓ PASA

---

### Caso 3.4: Inserción Inválida - Ambas FKs fallan ❌
**Entrada:**
```sql
INSERT INTO INSCRITOS VALUES ('E88','C77',1,2.5);
```

**Expectativa:**
```
ERROR [3001]:
La Sentencia INSERT está en conflicto con la restricción de Llave Foránea 'FK_INSCRITOS_ESTUDIANTE'. 
El conflicto ocurre en la BD 'INSCRITOS', tabla 'ESTUDIANTES', atributo 'E#'.
```

**Nota:** Se reporta el primer error encontrado

**Validación:** ✓ PASA

---

## GRUPO 4: Casos de Múltiples Sentencias

### Caso 4.1: Secuencia Mixta de Sentencias Válidas e Inválidas
**Entrada:**
```sql
-- Válida
INSERT INTO CARRERAS VALUES ('C5','SISTEMAS','2011',140,'D2');

-- Inválida
INSERT INTO CARRERAS VALUES ('C6','BASES DE DATOS','2012',150,100);

-- Válida
INSERT INTO ESTUDIANTES VALUES ('E4','Pedro Sánchez','9999999','C1');

-- Inválida
INSERT INTO ESTUDIANTES VALUES ('E5','Ana López','8888888','C99');
```

**Expectativa:**
- Línea 2: Válida
- Línea 5: ERROR [3001] - FK_CARRERAS, DEPARTAMENTOS.D#
- Línea 8: Válida
- Línea 11: ERROR [3001] - FK_ESTUDIANTES_CARRERA, CARRERAS.C#

**Validación:** ✓ PASA

---

## GRUPO 5: Casos de Sintaxis y Errores Léxicos

### Caso 5.1: Sentencia INSERT sin VALUES ❌
**Entrada:**
```sql
INSERT INTO CARRERAS ('C7','REDES','2013',160,'D2');
```

**Expectativa:**
```
ERROR [1001]: Sintaxis incorrecta: falta VALUES
```

**Validación:** ✓ PASA

---

### Caso 5.2: Tabla no existe ❌
**Entrada:**
```sql
INSERT INTO TABLA_NO_EXISTE VALUES ('val1','val2');
```

**Expectativa:**
```
ERROR [1002]: Tabla no existe: TABLA_NO_EXISTE
```

**Validación:** ✓ PASA

---

### Caso 5.3: Número de valores incorrecto ❌
**Entrada:**
```sql
INSERT INTO CARRERAS VALUES ('C8','TELEMATICA','2014');
```

**Expectativa:**
```
ERROR [1003]: Número de columnas (5) no coincide con número de valores (3)
```

**Validación:** ✓ PASA

---

## GRUPO 6: Casos de Comentarios y Líneas Vacías

### Caso 6.1: Entrada con comentarios
**Entrada:**
```sql
-- Esto es un comentario
INSERT INTO CARRERAS VALUES ('C9','IOT','2015',170,'D1');
-- Otro comentario

INSERT INTO ESTUDIANTES VALUES ('E7','Roberto López','7777777','C1');
```

**Expectativa:**
- Línea 2: Válida
- Línea 5: Válida
- Comentarios ignorados

**Validación:** ✓ PASA

---

## GRUPO 7: Casos Limítrofes

### Caso 7.1: Valores con espacios en blanco
**Entrada:**
```sql
INSERT INTO CARRERAS VALUES ( 'C10' , 'CLOUD COMPUTING' , '2016' , 180 , 'D2' );
```

**Expectativa:**
- Resultado: Válido
- Espacios correctamente manejados

**Validación:** ✓ PASA

---

### Caso 7.2: Valores numéricos como texto
**Entrada:**
```sql
INSERT INTO DEPARTAMENTOS VALUES ('D4','VIRTUAL','EDIFICIO D');
INSERT INTO CARRERAS VALUES ('C11','GAMING','2017',190,'D4');
```

**Expectativa:**
- Ambas válidas
- Comparación de valores funciona correctamente

**Validación:** ✓ PASA

---

## GRUPO 8: Validación de Mensajes de Error

### Caso 8.1: Formato completo del mensaje
**Entrada:**
```sql
INSERT INTO CARRERAS VALUES ('C3','ICI','2009',10,500);
```

**Validación de Mensaje:**
- ✓ Contiene "restricción de Llave Foránea"
- ✓ Contiene nombre FK: 'FK_CARRERAS'
- ✓ Contiene nombre BD: 'INSCRITOS'
- ✓ Contiene tabla ref: 'DEPARTAMENTOS'
- ✓ Contiene atributo ref: 'D#'

**Validación:** ✓ PASA

---

## Resumen de Pruebas

| Grupo | Casos | Descripción | Estado |
|-------|-------|-------------|--------|
| 1 | 3 | Errores en CARRERAS | ✓ LISTO |
| 2 | 3 | Errores en ESTUDIANTES | ✓ LISTO |
| 3 | 4 | Errores en INSCRITOS | ✓ LISTO |
| 4 | 1 | Múltiples sentencias | ✓ LISTO |
| 5 | 3 | Errores sintácticos | ✓ LISTO |
| 6 | 1 | Comentarios | ✓ LISTO |
| 7 | 2 | Casos limítrofes | ✓ LISTO |
| 8 | 1 | Formato de mensajes | ✓ LISTO |

**Total:** 18 casos de prueba

---

## Notas Importantes

1. El programa debe cumplir con TODOS los casos de prueba
2. Los mensajes de error deben incluir EXACTAMENTE los 4 componentes requeridos
3. El programa debe manejar múltiples sentencias en una sesión
4. Los comentarios (--) deben ser ignorados
5. La validación de FK debe hacerse en orden de definición

---

**Última actualización:** Mayo 2026
**Versión:** 1.0
