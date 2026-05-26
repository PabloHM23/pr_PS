package VSCode.Avance_5;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.*;
import java.util.List;

public class Scanner5 extends JFrame {

    // === DICCIONARIOS LÉXICOS ===
    static final Map<String, Integer> PALABRAS_RESERVADAS = new HashMap<>();
    static final Map<Character, Integer> DELIMITADORES = new HashMap<>();
    static final Map<Character, Integer> OPERADORES = new HashMap<>();
    static final Map<String, Integer> RELACIONALES = new HashMap<>();

    static {
        String[] pr = { "SELECT", "FROM", "WHERE", "IN", "AND", "OR", "CREATE", "TABLE",
                "CHAR", "NUMERIC", "NOT", "NULL", "CONSTRAINT", "KEY", "PRIMARY",
                "FOREIGN", "REFERENCES", "INSERT", "INTO", "VALUES", "INT", "VARCHAR", "DECIMAL", "DATE", "DISTINCT" };
        int prCode = 10;
        for (String p : pr)
            PALABRAS_RESERVADAS.put(p, prCode++);

        DELIMITADORES.put(',', 50);
        DELIMITADORES.put('.', 51);
        DELIMITADORES.put('(', 52);
        DELIMITADORES.put(')', 53);
        DELIMITADORES.put(';', 54);

        OPERADORES.put('+', 70);
        OPERADORES.put('-', 71);
        OPERADORES.put('*', 72);
        OPERADORES.put('/', 73);

        RELACIONALES.put(">=", 84);
        RELACIONALES.put("<=", 85);
        RELACIONALES.put(">", 81);
        RELACIONALES.put("<", 82);
        RELACIONALES.put("=", 83);
    }

    // === BASE DE DATOS INSCRITOS ===
    static class BaseDatos {
        Map<String, Tabla> tablas = new HashMap<>();
        String nombre = "INSCRITOS";

        BaseDatos() {
            inicializarEstructura();
        }

        void inicializarEstructura() {
            // Adaptado para coincidir exactamente con los ejemplos del Avance 5
            
            // Tabla ALUMNOS
            Tabla alumnos = new Tabla("ALUMNOS");
            alumnos.agregarColumna("A#", "VARCHAR", 3, true, false, null);
            alumnos.agregarColumna("ANOMBRE", "VARCHAR", 40, false, false, null);
            alumnos.agregarColumna("GENERACION", "VARCHAR", 4, false, false, null);
            alumnos.agregarColumna("C#", "VARCHAR", 3, false, false, null);
            alumnos.setPrimaryKey("A#");
            tablas.put("ALUMNOS", alumnos);

            // Tabla CARRERAS
            Tabla carreras = new Tabla("CARRERAS");
            carreras.agregarColumna("C#", "VARCHAR", 3, true, false, null);
            carreras.agregarColumna("CNOMBRE", "VARCHAR", 40, false, false, null);
            carreras.agregarColumna("SEMESTRES", "INT", 2, false, false, null);
            carreras.setPrimaryKey("C#");
            tablas.put("CARRERAS", carreras);

            // Tabla INSCRITOS
            Tabla inscritos = new Tabla("INSCRITOS");
            inscritos.agregarColumna("A#", "VARCHAR", 3, true, true, null);
            inscritos.agregarColumna("C#", "VARCHAR", 3, true, true, null);
            inscritos.agregarColumna("SEMESTRE", "CHAR", 6, false, false, null); 
            inscritos.setPrimaryKey("A#,C#");
            tablas.put("INSCRITOS", inscritos);

            // Tabla MATERIAS
            Tabla materias = new Tabla("MATERIAS");
            materias.agregarColumna("M#", "VARCHAR", 3, true, false, null);
            materias.agregarColumna("MNOMBRE", "VARCHAR", 40, false, false, null);
            materias.setPrimaryKey("M#");
            tablas.put("MATERIAS", materias);
        }
    }

    // === ESTRUCTURAS DE DATOS ===
    static class Columna {
        String nombre;
        String tipo;
        int longitud;
        boolean esPrimaryKey;
        boolean esNotNull;
        String valorDefault;

        Columna(String nombre, String tipo, int longitud, boolean pk, boolean nn, String def) {
            this.nombre = nombre;
            this.tipo = tipo;
            this.longitud = longitud;
            this.esPrimaryKey = pk;
            this.esNotNull = nn;
            this.valorDefault = def;
        }
    }

    static class LlaveForeignKey {
        String nombre;
        String columnaLocal;
        String tablaReferenciada;
        String columnaReferenciada;

        LlaveForeignKey(String nombre, String colLocal, String tabRef, String colRef) {
            this.nombre = nombre;
            this.columnaLocal = colLocal;
            this.tablaReferenciada = tabRef;
            this.columnaReferenciada = colRef;
        }
    }

    static class Tabla {
        String nombre;
        List<Columna> columnas = new ArrayList<>();
        List<LlaveForeignKey> llavesForeignKey = new ArrayList<>();
        List<String[]> filas = new ArrayList<>();
        String primaryKey;

        Tabla(String nombre) {
            this.nombre = nombre;
        }

        void agregarColumna(String nom, String tipo, int len, boolean pk, boolean nn, String def) {
            columnas.add(new Columna(nom, tipo, len, pk, nn, def));
        }

        void agregarLlaveForeignKey(LlaveForeignKey fk) {
            llavesForeignKey.add(fk);
        }

        void setPrimaryKey(String pk) {
            this.primaryKey = pk;
        }

        Columna obtenerColumnaObj(String nomCol) {
            for (Columna c : columnas) {
                if (c.nombre.equalsIgnoreCase(nomCol)) return c;
            }
            return null;
        }
    }

    // === TOKEN ===
    static class Token {
        String lexema;
        int tipo;      // 1=reservada, 4=identificador, 5=delimitador, 6=constante, 7=operador, 8=relacional
        int codigo;    // 601=String, 602=Numeric
        int linea;

        Token(String lexema, int tipo, int codigo, int linea) {
            this.lexema = lexema;
            this.tipo = tipo;
            this.codigo = codigo;
            this.linea = linea;
        }

        @Override
        public String toString() {
            return String.format("Token{%s, tipo=%d, cod=%d, lin=%d}", lexema, tipo, codigo, linea);
        }
    }

    // === ERROR SQL ===
    static class ErrorSQL {
        int tipo;       // 1=Léxico, 2=Sintáctico, 3=Semántico
        int codigo;
        int linea;
        String descripcion;

        ErrorSQL(int tipo, int codigo, int linea, String descripcion) {
            this.tipo = tipo;
            this.codigo = codigo;
            this.linea = linea;
            this.descripcion = descripcion;
        }

        String getTipoError() {
            return tipo == 1 ? "LÉXICO" : tipo == 2 ? "SINTÁCTICO" : "SEMÁNTICO";
        }

        @Override
        public String toString() {
            return String.format("[%s] Línea %02d, Código %d: %s", getTipoError(), linea, codigo, descripcion);
        }
    }

    // Auxiliares para el análisis Semántico del DML
    static class ContextoSelect {
        ContextoSelect padre;
        Map<String, String> aliasATabla = new HashMap<>();
        List<String> tablas = new ArrayList<>();
        ContextoSelect(ContextoSelect padre) { this.padre = padre; }
    }

    static class RefColumna {
        String prefijo;
        String nombre;
        Token tokenPrefijo;
        Token tokenNombre;
    }

    // === ANALIZADOR SQL ===
    static class AnalizadorSQL {
        BaseDatos bd;
        List<Token> tokens = new ArrayList<>();
        List<ErrorSQL> errores = new ArrayList<>();
        int posToken = 0;
        Set<String> restriccionesScript = new HashSet<>();

        AnalizadorSQL(BaseDatos bd) {
            this.bd = bd;
        }

        // FASE 1: LÉXICO
        void tokenizar(String sql) {
            tokens.clear();
            String[] lineas = sql.split("\n");

            for (int li = 1; li <= lineas.length; li++) {
                String linea = lineas[li - 1];
                int i = 0;

                while (i < linea.length()) {
                    char c = linea.charAt(i);

                    if (Character.isWhitespace(c)) { i++; continue; }
                    if (i + 1 < linea.length() && linea.substring(i, i + 2).equals("--")) break;

                    String dos = (i + 1 < linea.length()) ? linea.substring(i, i + 2) : "";
                    if (RELACIONALES.containsKey(dos)) {
                        tokens.add(new Token(dos, 8, RELACIONALES.get(dos), li));
                        i += 2; continue;
                    }
                    if (RELACIONALES.containsKey(String.valueOf(c))) {
                        tokens.add(new Token(String.valueOf(c), 8, RELACIONALES.get(String.valueOf(c)), li));
                        i++; continue;
                    }
                    if (OPERADORES.containsKey(c)) {
                        tokens.add(new Token(String.valueOf(c), 7, OPERADORES.get(c), li));
                        i++; continue;
                    }

                    if (c == '\'') {
                        int j = i + 1;
                        while (j < linea.length() && linea.charAt(j) != '\'') j++;
                        if (j < linea.length()) {
                            String valor = linea.substring(i + 1, j);
                            tokens.add(new Token("'" + valor + "'", 6, 601, li)); // 601 = String
                            i = j + 1;
                        } else {
                            errores.add(new ErrorSQL(1, 101, li, "Comilla de cierre no encontrada"));
                            i = linea.length();
                        }
                        continue;
                    }

                    if (DELIMITADORES.containsKey(c)) {
                        tokens.add(new Token(String.valueOf(c), 5, DELIMITADORES.get(c), li));
                        i++; continue;
                    }

                    if (Character.isDigit(c)) {
                        int j = i;
                        while (j < linea.length() && Character.isDigit(linea.charAt(j))) j++;
                        tokens.add(new Token(linea.substring(i, j), 6, 602, li)); // 602 = Numerico
                        i = j; continue;
                    }

                    if (Character.isLetter(c) || c == '_') {
                        int j = i;
                        while (j < linea.length() && (Character.isLetterOrDigit(linea.charAt(j)) || linea.charAt(j) == '_' || linea.charAt(j) == '#'))
                            j++;
                        String palabra = linea.substring(i, j).toUpperCase();
                        if (PALABRAS_RESERVADAS.containsKey(palabra)) {
                            tokens.add(new Token(palabra, 1, PALABRAS_RESERVADAS.get(palabra), li));
                        } else {
                            tokens.add(new Token(palabra, 4, 400, li));
                        }
                        i = j; continue;
                    }

                    errores.add(new ErrorSQL(1, 101, li, "Símbolo desconocido: '" + c + "'"));
                    i++;
                }
            }
        }

        // FASE 2 y 3: SINTÁCTICO Y SEMÁNTICO
        void analizarSintaxis() {
            posToken = 0;
            restriccionesScript.clear(); 
            
            try {
                while (posToken < tokens.size()) {
                    Token t = verToken();
                    if (t == null) break;
                    
                    int posInicial = posToken;

                    if (t.tipo == 1 && t.lexema.equals("SELECT")) {
                        analizarSelect(null);
                    } else if (t.tipo == 1 && t.lexema.equals("CREATE")) {
                        analizarCreateTable();
                    } else if (t.tipo == 1 && t.lexema.equals("INSERT")) {
                        analizarInsert();
                    } else if (t.tipo == 5 && t.lexema.equals(";")) {
                        consumir();
                    } else {
                        errores.add(new ErrorSQL(2, 201, t.linea, "Se esperaba SELECT, CREATE o INSERT"));
                        consumir(); 
                    }
                    if (posToken == posInicial) consumir(); // Salvaguarda bucle
                }
            } catch (Exception e) {
                errores.add(new ErrorSQL(2, 999, verToken() != null ? verToken().linea : 0, "Ejecución recuperada de error abrupto: " + e.getMessage()));
            }
        }

        //  DML (AVANCE 5) 
        void analizarSelect(ContextoSelect ctxPadre) {
            consumir(); // SELECT
            if (esReservada("DISTINCT")) consumir();

            List<RefColumna> columnasSelect = new ArrayList<>();
            while (verToken() != null && !esReservada("FROM")) {
                if (esTipo(7) && verToken().lexema.equals("*")) {
                    consumir();
                } else {
                    RefColumna ref = leerRefColumna();
                    if (ref != null) columnasSelect.add(ref);
                }
                if (esDelimitador(",")) consumir();
            }

            esperarReservada("FROM", 201);
            
            ContextoSelect ctx = new ContextoSelect(ctxPadre);
            while (verToken() != null) {
                if (esReservada("WHERE") || esDelimitador(";") || esDelimitador(")")) break;
                
                Token tTabla = verToken();
                if (tTabla.tipo == 4) {
                    String nomTabla = tTabla.lexema.toUpperCase();
                    // Semántico: (Error 3:314)
                    if (!bd.tablas.containsKey(nomTabla)) {
                        errores.add(new ErrorSQL(3, 314, tTabla.linea, "El nombre de la tabla \"" + nomTabla + "\" no es válido."));
                    } else {
                        ctx.tablas.add(nomTabla);
                    }
                    consumir();

                    Token tAlias = verToken();
                    if (tAlias != null && tAlias.tipo == 4 && !esDelimitador(",") && !esReservada(tAlias.lexema)) {
                        ctx.aliasATabla.put(tAlias.lexema.toUpperCase(), nomTabla);
                        consumir();
                    }
                } else {
                    consumir(); 
                }
            }

            // Validar semántica de las columnas pedidas en el SELECT
            for (RefColumna col : columnasSelect) {
                validarRefColumna(col, ctx);
            }

            if (esReservada("WHERE")) {
                consumir();
                analizarWhere(ctx);
            }
        }

        void analizarWhere(ContextoSelect ctx) {
            while (verToken() != null && !esDelimitador(";") && !esDelimitador(")")) {
                Token t = verToken();
                
                if (esReservada("AND") || esReservada("OR")) {
                    consumir();
                    continue;
                }

                RefColumna col1 = leerRefColumna();
                Columna c1 = null;
                if (col1 != null) {
                    c1 = validarRefColumna(col1, ctx);
                } else {
                    consumir();
                    continue;
                }

                Token op = verToken();
                if (op != null) {
                    if (op.lexema.equals("IN")) {
                        consumir();
                        esperarDelimitador("(", 205);
                        if (esReservada("SELECT")) {
                            analizarSelect(ctx); // Subconsulta heredando contexto
                        } else {
                            while(verToken() != null && !esDelimitador(")")) consumir();
                        }
                        esperarDelimitador(")", 205);
                    } else if (op.tipo == 8 || op.lexema.equals("=")) { 
                        consumir();
                        Token t2 = verToken();
                        if (t2 != null) {
                            if (t2.tipo == 6) { // Comparación con constante Literal
                                if (c1 != null) validarTipos(c1, t2);
                                consumir();
                            } else if (t2.tipo == 4) { // Comparación con otra Columna
                                RefColumna col2 = leerRefColumna();
                                if (col2 != null) validarRefColumna(col2, ctx);
                            } else {
                                consumir();
                            }
                        }
                    } else {
                        consumir(); 
                    }
                } else {
                    break;
                }
            }
        }

        RefColumna leerRefColumna() {
            Token t1 = verToken();
            if (t1 == null || t1.tipo != 4) return null;
            
            consumir();
            if (esDelimitador(".")) {
                consumir();
                Token t2 = verToken();
                if (t2 != null && t2.tipo == 4) {
                    consumir();
                    RefColumna ref = new RefColumna();
                    ref.prefijo = t1.lexema.toUpperCase();
                    ref.nombre = t2.lexema.toUpperCase();
                    ref.tokenPrefijo = t1;
                    ref.tokenNombre = t2;
                    return ref;
                }
            } else {
                RefColumna ref = new RefColumna();
                ref.nombre = t1.lexema.toUpperCase();
                ref.tokenNombre = t1;
                return ref;
            }
            return null;
        }

        Columna validarRefColumna(RefColumna ref, ContextoSelect ctx) {
            if (ref.prefijo != null) {
                String realTable = ctx.aliasATabla.getOrDefault(ref.prefijo, ref.prefijo);
                boolean tablaEncontrada = ctx.tablas.contains(realTable);
                
                // Buscar en el contexto padre (subconsultas)
                ContextoSelect current = ctx.padre;
                while (!tablaEncontrada && current != null) {
                     String realParent = current.aliasATabla.getOrDefault(ref.prefijo, ref.prefijo);
                     if (current.tablas.contains(realParent)) {
                         tablaEncontrada = true;
                         realTable = realParent;
                     }
                     current = current.padre;
                }

                if (!tablaEncontrada) { // Error 3:315 Identificador NO válido
                    errores.add(new ErrorSQL(3, 315, ref.tokenPrefijo.linea, 
                        "El identificador \"" + ref.prefijo + "." + ref.nombre + "\" no es válido."));
                    return null;
                }
                
                Tabla t = bd.tablas.get(realTable);
                if (t != null) {
                    Columna c = t.obtenerColumnaObj(ref.nombre);
                    if (c == null) { // Error 3:311 Atributo NO válido
                        errores.add(new ErrorSQL(3, 311, ref.tokenNombre.linea, 
                            "El nombre del atributo \"" + ref.nombre + "\" no es válido."));
                        return null;
                    }
                    return c;
                }
            } else {
                int count = 0;
                Columna colFound = null;
                
                ContextoSelect current = ctx;
                while (current != null) {
                    for (String tName : current.tablas) {
                        Tabla t = bd.tablas.get(tName);
                        if (t != null) {
                            Columna c = t.obtenerColumnaObj(ref.nombre);
                            if (c != null) {
                                count++;
                                colFound = c;
                            }
                        }
                    }
                    if (count > 0) break; // Si se encuentra en el subscope se usa esa
                    current = current.padre;
                }
                
                if (count == 0) { // Error 3:311
                    errores.add(new ErrorSQL(3, 311, ref.tokenNombre.linea, 
                        "El nombre del atributo \"" + ref.nombre + "\" no es válido."));
                } else if (count > 1) { // Error 3:312
                    errores.add(new ErrorSQL(3, 312, ref.tokenNombre.linea, 
                        "El nombre del atributo \"" + ref.nombre + "\" es ambigüo."));
                }
                return count == 1 ? colFound : null;
            }
            return null;
        }

        void validarTipos(Columna col, Token valToken) {
            boolean colIsChar = col.tipo.equals("CHAR") || col.tipo.equals("VARCHAR");
            boolean valIsNumeric = valToken.codigo == 602;
            boolean valIsString = valToken.codigo == 601;

            if (colIsChar && valIsNumeric) {
                // Error 3:313 (char a int)
                errores.add(new ErrorSQL(3, 313, valToken.linea, 
                    "Error de conversión al convertir el valor del atributo '" + col.nombre + "' del tipo char a tipo de dato int."));
            } else if (!colIsChar && valIsString) {
                // Error 3:313 (int a char)
                errores.add(new ErrorSQL(3, 313, valToken.linea, 
                    "Error de conversión al convertir el valor del atributo '" + col.nombre + "' del tipo int a tipo de dato char."));
            }
        }

        // ===================================================

        void analizarCreateTable() {
            consumir(); 
            esperarReservada("TABLE", 201);
            
            Token tTabla = verToken();
            String nomTabla = "";
            if (tTabla != null && tTabla.tipo == 4) {
                nomTabla = tTabla.lexema.toUpperCase();
                if (bd.tablas.containsKey(nomTabla)) {
                    errores.add(new ErrorSQL(3, 306, tTabla.linea, "El nombre del atributo \"" + nomTabla + "\" está duplicado."));
                }
                consumir();
            } else {
                esperarTipo(4, 204);
            }

            esperarDelimitador("(", 205);
            Tabla nuevaTabla = new Tabla(nomTabla);
            List<String> columnasLocales = new ArrayList<>();

            while (verToken() != null && !esDelimitador(")")) {
                analizarDefColumna(nomTabla, nuevaTabla, columnasLocales);
                if (esDelimitador(",")) consumir(); else break;
            }
            esperarDelimitador(")", 205);
            if (esDelimitador(";")) consumir();
            
            if (!nomTabla.isEmpty()) bd.tablas.put(nomTabla, nuevaTabla); 
        }

        void analizarDefColumna(String nomTabla, Tabla tablaActual, List<String> columnasLocales) {
            Token t = verToken();
            if (t != null && t.tipo == 1 && t.lexema.equals("CONSTRAINT")) {
                consumir();
                Token tConst = verToken();
                String nombreConstraint = "";
                if (tConst != null && tConst.tipo == 4) {
                    nombreConstraint = tConst.lexema.toUpperCase();
                    if (restriccionesScript.contains(nombreConstraint)) {
                        errores.add(new ErrorSQL(3, 306, tConst.linea, "El nombre de la restricción \"" + nombreConstraint + "\" esta duplicado."));
                    } else {
                        restriccionesScript.add(nombreConstraint);
                    }
                    consumir();
                } else {
                    esperarTipo(4, 204);
                }

                if (esReservada("PRIMARY")) {
                    consumir();
                    esperarReservada("KEY", 201);
                    esperarDelimitador("(", 205);
                    Token tCol = verToken();
                    if (tCol != null && tCol.tipo == 4) {
                        String colName = tCol.lexema.toUpperCase();
                        if (!columnasLocales.contains(colName)) {
                            errores.add(new ErrorSQL(3, 303, tCol.linea, "El nombre del atributo \"" + colName + "\" no existe en la tabla \"" + nomTabla + "\"."));
                        }
                        consumir();
                    } else esperarTipo(4, 204);
                    esperarDelimitador(")", 205);
                    
                } else if (esReservada("FOREIGN")) {
                    consumir(); esperarReservada("KEY", 201); esperarDelimitador("(", 205);
                    Token tColLocal = verToken();
                    if (tColLocal != null && tColLocal.tipo == 4) {
                        String colName = tColLocal.lexema.toUpperCase();
                        if (!columnasLocales.contains(colName)) {
                            errores.add(new ErrorSQL(3, 305, tColLocal.linea, "Se hace referencia al atributo \"" + colName + "\" no válido en la tabla \"" + nomTabla + "\"."));
                        }
                        consumir();
                    } else esperarTipo(4, 204);
                    
                    esperarDelimitador(")", 205); esperarReservada("REFERENCES", 201);
                    Token tRef = verToken();
                    if (tRef != null && tRef.tipo == 4) consumir(); else esperarTipo(4, 204);
                    esperarDelimitador("(", 205);
                    Token tColRef = verToken();
                    if (tColRef != null && tColRef.tipo == 4) consumir(); else esperarTipo(4, 204);
                    esperarDelimitador(")", 205);
                }
            } else {
                Token tCol = verToken();
                String colName = "";
                if (tCol != null && tCol.tipo == 4) {
                    colName = tCol.lexema.toUpperCase();
                    if (columnasLocales.contains(colName)) {
                        errores.add(new ErrorSQL(3, 302, tCol.linea, "El nombre del atributo \"" + colName + "\" se especifica más de una vez."));
                    } else {
                        columnasLocales.add(colName);
                    }
                    consumir();
                } else esperarTipo(4, 204);

                Token tTipo = verToken();
                String tipoDato = "";
                if (tTipo != null && tTipo.tipo == 1) { 
                    tipoDato = tTipo.lexema.toUpperCase(); consumir();
                } else esperarTipo(1, 201);

                int longitud = 0;
                if (esDelimitador("(")) {
                    consumir();
                    Token tLen = verToken();
                    if (tLen != null && tLen.tipo == 6) {
                        try { longitud = Integer.parseInt(tLen.lexema); } catch (Exception e) {}
                        consumir();
                    }
                    esperarDelimitador(")", 205);
                }

                tablaActual.agregarColumna(colName, tipoDato, longitud, false, false, null);

                if (esReservada("NOT")) {
                    consumir(); esperarReservada("NULL", 201);
                }
            }
        }

        void analizarInsert() {
            consumir(); esperarReservada("INTO", 201);
            Token tTabla = verToken();
            String nomTabla = "";
            int lineaError = tTabla != null ? tTabla.linea : 1;
            
            if (tTabla != null && tTabla.tipo == 4) {
                nomTabla = tTabla.lexema.toUpperCase(); consumir();
            } else esperarTipo(4, 204);

            if (esDelimitador("(")) {
                consumir(); esperarTipo(4, 204);
                while (esDelimitador(",")) { consumir(); esperarTipo(4, 204); }
                esperarDelimitador(")", 205);
            }
            esperarReservada("VALUES", 201); esperarDelimitador("(", 205);
            
            List<Token> valores = new ArrayList<>();
            Token tVal = verToken();
            if (tVal != null && (tVal.tipo == 6 || tVal.tipo == 4)) {
                valores.add(tVal); consumir();
            }
            while (esDelimitador(",")) {
                consumir();
                tVal = verToken();
                if (tVal != null && (tVal.tipo == 6 || tVal.tipo == 4)) {
                     valores.add(tVal); consumir();
                }
            }
            
            Token tCierre = verToken();
            if (tCierre != null) lineaError = tCierre.linea;
            esperarDelimitador(")", 205);
            if (esDelimitador(";")) consumir();

            if (!bd.tablas.containsKey(nomTabla)) {
                errores.add(new ErrorSQL(3, 301, lineaError, "Tabla no existe: " + nomTabla));
                return;
            }

            Tabla tabla = bd.tablas.get(nomTabla);
            if (valores.size() != tabla.columnas.size()) {
                errores.add(new ErrorSQL(3, 307, lineaError, "Los valores especificados no corresponden a la definición de la tabla."));
                return;
            }

            for (int i = 0; i < valores.size(); i++) {
                Token val = valores.get(i);
                Columna col = tabla.columnas.get(i);
                if (col.tipo.equals("CHAR") || col.tipo.equals("VARCHAR")) {
                    if (val.lexema.replace("'", "").length() > col.longitud && col.longitud > 0) {
                        errores.add(new ErrorSQL(3, 308, val.linea, "Los datos de cadena o binarios se truncarían."));
                    }
                }
            }
        }

        // Métodos auxiliares
        Token verToken() { return posToken < tokens.size() ? tokens.get(posToken) : null; }
        void consumir() { if (posToken < tokens.size()) posToken++; }
        boolean esTipo(int tipo) { Token t = verToken(); return t != null && t.tipo == tipo; }
        boolean esReservada(String palabra) { Token t = verToken(); return t != null && t.tipo == 1 && t.lexema.equals(palabra); }
        boolean esDelimitador(String delim) { Token t = verToken(); return t != null && t.tipo == 5 && t.lexema.equals(delim); }

        void esperarTipo(int tipo, int cod) {
            Token t = verToken();
            if (t != null && t.tipo == tipo) consumir();
            else errores.add(new ErrorSQL(2, cod, t != null ? t.linea : 1, "Se esperaba token de tipo " + tipo));
        }

        void esperarReservada(String palabra, int cod) {
            Token t = verToken();
            if (t != null && t.tipo == 1 && t.lexema.equals(palabra)) consumir();
            else errores.add(new ErrorSQL(2, cod, t != null ? t.linea : 1, "Se esperaba: " + palabra));
        }

        void esperarDelimitador(String delim, int cod) {
            Token t = verToken();
            if (t != null && t.tipo == 5 && t.lexema.equals(delim)) consumir();
            else errores.add(new ErrorSQL(2, cod, t != null ? t.linea : 1, "Se esperaba: " + delim));
        }

        void analizar(String sql) {
            tokens.clear();
            errores.clear();
            posToken = 0;
            tokenizar(sql);
            if (errores.isEmpty()) {
                analizarSintaxis();
            }
        }

        List<ErrorSQL> obtenerErrores() { return errores; }
    }

    // VARIABLES DE INTERFAZ GRÁFICA
    private JTextArea textEntrada;
    private JTable tablaErrores;
    private JTable tablaDatos;
    private JTextArea textConsola;
    private DefaultTableModel modeloErrores;
    private DefaultTableModel modeloDatos;
    private JLabel lblEstado;
    private BaseDatos bd;
    private AnalizadorSQL analizador;

    public Scanner5() {
        setTitle("Traductor SQL");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1500, 900);
        setLocationRelativeTo(null);

        bd = new BaseDatos();
        analizador = new AnalizadorSQL(bd);

        JPanel panelPrincipal = new JPanel(new BorderLayout(10, 10));
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel panelOpciones = crearPanelOpciones();
        panelPrincipal.add(panelOpciones, BorderLayout.NORTH);

        JPanel panelEntrada = crearPanelEntrada();
        JPanel panelResultados = crearPanelResultados();

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, panelEntrada, panelResultados);
        splitPane.setDividerLocation(350);

        panelPrincipal.add(splitPane, BorderLayout.CENTER);
        add(panelPrincipal);
    }

    private JPanel crearPanelOpciones() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("OPCIONES DE TABLAS SEMÁNTICAS"));

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));

        JButton btnActualizar = new JButton("✓ Actualizar Tablas Semánticas");
        JButton btnInicializar = new JButton("✗ Inicializar Tablas Semánticas");

        btnActualizar.addActionListener(e -> actualizarTablasSematicas());
        btnInicializar.addActionListener(e -> inicializarTablasSematicas());

        panelBotones.add(btnActualizar);
        panelBotones.add(btnInicializar);

        lblEstado = new JLabel("Estado: Tablas semánticas cargadas");
        lblEstado.setForeground(new Color(0, 128, 0));

        panel.add(panelBotones, BorderLayout.WEST);
        panel.add(lblEstado, BorderLayout.CENTER);

        return panel;
    }

    private void actualizarTablasSematicas() {
        bd = new BaseDatos();
        analizador = new AnalizadorSQL(bd);
        cargarEstructuraBD();
        lblEstado.setText("✓ Tablas semánticas actualizadas: " + bd.tablas.size() + " tablas cargadas");
        lblEstado.setForeground(new Color(0, 128, 0));
        JOptionPane.showMessageDialog(this, "Tablas semánticas actualizadas correctamente", "Éxito", JOptionPane.INFORMATION_MESSAGE);
    }

    private void inicializarTablasSematicas() {
        bd.tablas.clear();
        analizador = new AnalizadorSQL(bd);
        modeloDatos.setRowCount(0);
        lblEstado.setText("✓ Tablas semánticas inicializadas (vacías)");
        lblEstado.setForeground(Color.RED);
        JOptionPane.showMessageDialog(this, "Tablas semánticas limpias", "Inicialización", JOptionPane.INFORMATION_MESSAGE);
    }

    private JPanel crearPanelEntrada() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(new TitledBorder("MÓDULO DE ENTRADA - Sentencias SQL (DDL/DML)"));

        textEntrada = new JTextArea();
        textEntrada.setFont(new Font("Courier New", Font.PLAIN, 12));
        textEntrada.setLineWrap(true);
        textEntrada.setWrapStyleWord(true);
        
        // Puesto por defecto para probar los ejemplos solicitados del Avance 5
        textEntrada.setText("");

        JScrollPane scrollEntrada = new JScrollPane(textEntrada);
        panel.add(scrollEntrada, BorderLayout.CENTER);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        JButton btnAnalizar = new JButton("▶ Analizar SQL");
        JButton btnLimpiar = new JButton("✗ Limpiar");

        btnAnalizar.setFont(new Font("Tahoma", Font.BOLD, 11));
        btnAnalizar.addActionListener(e -> analizarSQL());
        btnLimpiar.addActionListener(e -> {
            textEntrada.setText("");
            limpiarResultados();
        });

        panelBotones.add(btnAnalizar);
        panelBotones.add(btnLimpiar);

        panel.add(panelBotones, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel crearPanelResultados() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));

        JTabbedPane tabs = new JTabbedPane();

        JPanel panelErrores = new JPanel(new BorderLayout());
        modeloErrores = new DefaultTableModel(
                new String[] { "Línea", "Código", "Tipo", "Ubicación", "Descripción" }, 0);
        tablaErrores = new JTable(modeloErrores);
        tablaErrores.setRowHeight(40);
        panelErrores.add(new JScrollPane(tablaErrores), BorderLayout.CENTER);
        tabs.addTab("Módulo de Errores", panelErrores);

        JPanel panelEstructura = new JPanel(new BorderLayout());
        modeloDatos = new DefaultTableModel(
                new String[] { "Tabla", "Columna", "Tipo", "PK", "FK" }, 0);
        tablaDatos = new JTable(modeloDatos);
        panelEstructura.add(new JScrollPane(tablaDatos), BorderLayout.CENTER);
        cargarEstructuraBD();
        tabs.addTab("Tablas Semánticas", panelEstructura);

        JPanel panelConsola = new JPanel(new BorderLayout());
        textConsola = new JTextArea();
        textConsola.setFont(new Font("Courier New", Font.PLAIN, 12));
        textConsola.setEditable(false);
        panelConsola.add(new JScrollPane(textConsola), BorderLayout.CENTER);
        tabs.addTab("Análisis Detallado (3 Fases)", panelConsola);

        panel.add(tabs, BorderLayout.CENTER);
        panel.setBorder(new TitledBorder("MÓDULO DE RESULTADOS"));

        return panel;
    }

    private void cargarEstructuraBD() {
        modeloDatos.setRowCount(0);
        for (Tabla tabla : bd.tablas.values()) {
            for (Columna col : tabla.columnas) {
                String fk = "NO";
                for (LlaveForeignKey llaveFK : tabla.llavesForeignKey) {
                    if (llaveFK.columnaLocal.equalsIgnoreCase(col.nombre)) { fk = "SÍ"; break; }
                }
                modeloDatos.addRow(new Object[] {
                        tabla.nombre, col.nombre, col.tipo + "(" + col.longitud + ")",
                        col.esPrimaryKey ? "SÍ" : "NO", fk
                });
            }
        }
    }

    private void analizarSQL() {
        String codigo = textEntrada.getText();
        if (codigo.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, ingrese una sentencia SQL", "Entrada vacía", JOptionPane.WARNING_MESSAGE);
            return;
        }

        limpiarResultados();
        StringBuilder consola = new StringBuilder("=== ANÁLISIS SQL (3 FASES) ===\n==================\n\n");

        consola.append("FASE 1: ANÁLISIS LÉXICO\n------------------------\n");
        analizador.analizar(codigo);
        List<Token> tokens = analizador.tokens;
        List<ErrorSQL> errores = analizador.obtenerErrores();

        consola.append("Tokens generados: ").append(tokens.size()).append("\n");
        for (Token t : tokens) consola.append("  ").append(t).append("\n");

        List<ErrorSQL> eLexicos = errores.stream().filter(e -> e.tipo == 1).toList();
        if (!eLexicos.isEmpty()) {
            consola.append("\n⚠️  Errores Léxicos:\n");
            for (ErrorSQL err : eLexicos) consola.append("  ").append(err).append("\n");
        }

        consola.append("\nFASE 2: ANÁLISIS SINTÁCTICO\n---------------------------\n");
        List<ErrorSQL> eSintacticos = errores.stream().filter(e -> e.tipo == 2).toList();
        if (eSintacticos.isEmpty()) consola.append("✓ Sintaxis válida\n");
        else {
            consola.append("⚠️  Errores Sintácticos:\n");
            for (ErrorSQL err : eSintacticos) consola.append("  ").append(err).append("\n");
        }

        consola.append("\nFASE 3: ANÁLISIS SEMÁNTICO (Avance 5 - DML)\n---------------------------\n");
        List<ErrorSQL> eSemanticos = errores.stream().filter(e -> e.tipo == 3).toList();
        if (eSemanticos.isEmpty()) consola.append("✓ Semántica válida\n");
        else {
            consola.append("⚠️  Errores Semánticos:\n");
            for (ErrorSQL err : eSemanticos) consola.append("  ").append(err).append("\n");
        }

        consola.append("\n=== RESULTADO FINAL ===\n");
        if (errores.isEmpty()) {
            consola.append("✓ CONSULTA VÁLIDA - Libre de errores\n");
            JOptionPane.showMessageDialog(this, "✓ Análisis exitoso\n\nLa sentencia SQL es válida", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            cargarEstructuraBD();
        } else {
            consola.append("✗ CONSULTA INVÁLIDA - Se encontraron ").append(errores.size()).append(" error(es)\n");
            // No bloqueamos ejecución gracias al Point 5, dejamos correr para nueva consulta
        }

        textConsola.setText(consola.toString());

        for (ErrorSQL err : errores) {
            modeloErrores.addRow(new Object[] {
                    String.format("%02d", err.linea), "3:" + err.codigo, err.getTipoError(),
                    "Línea " + String.format("%02d", err.linea), err.descripcion
            });
        }
    }

    private void limpiarResultados() {
        modeloErrores.setRowCount(0);
        textConsola.setText("");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } 
            catch (Exception e) { e.printStackTrace(); }
            Scanner5 frame = new Scanner5();
            frame.setVisible(true);
        });
    }
}
