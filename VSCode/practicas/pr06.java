package VSCode.practicas;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.*;
import java.util.List;

public class pr06 extends JFrame {

    // --- DICCIONARIOS LEXICOS ---
    static final Map<String, Integer> PALABRAS_RESERVADAS = new HashMap<>();
    static final Map<Character, Integer> DELIMITADORES = new HashMap<>();
    static final Map<Character, Integer> OPERADORES = new HashMap<>();
    static final Map<String, Integer> RELACIONALES = new HashMap<>();

    static {
        String[] pr = { "SELECT", "FROM", "WHERE", "IN", "AND", "OR", "CREATE", "TABLE",
                "CHAR", "NUMERIC", "NOT", "NULL", "CONSTRAINT", "KEY", "PRIMARY",
                "FOREIGN", "REFERENCES", "INSERT", "INTO", "VALUES", "DATE", "CHECK" }; // AÑADIDO: DATE y CHECK
        int prCode = 10;
        for (String p : pr)
            PALABRAS_RESERVADAS.put(p, prCode++);

        DELIMITADORES.put(',', 50);
        DELIMITADORES.put('.', 51);
        DELIMITADORES.put('(', 52);
        DELIMITADORES.put(')', 53);
        DELIMITADORES.put(';', 55);
        DELIMITADORES.put('\'', 54);

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

    // --- CLASES DE DATOS ---
    static class Token {
        String lexema;
        int tipo;
        int codigo;
        int linea;
        int sub;
        boolean es_ident = false, es_const = false, desconocido = false;

        Token(String lexema, int tipo, int codigo, int linea) {
            this.lexema = lexema;
            this.tipo = tipo;
            this.codigo = codigo;
            this.linea = linea;
        }
    }

    static class ErrorSQL {
        int tipo, codigo, linea;
        String descripcion;

        ErrorSQL(int tipo, int codigo, int linea, String descripcion) {
            this.tipo = tipo;
            this.codigo = codigo;
            this.linea = linea;
            this.descripcion = descripcion;
        }
    }

    static class Columna {
        String nombre;
        String tipo;
        boolean notNull;
        String restriccion;

        Columna(String nombre, String tipo, boolean notNull, String restriccion) {
            this.nombre = nombre;
            this.tipo = tipo;
            this.notNull = notNull;
            this.restriccion = restriccion;
        }
    }

    static class TablaSemantica {
        String nombre;
        List<Columna> columnas = new ArrayList<>();
        List<String> restricciones = new ArrayList<>();

        TablaSemantica(String nombre) {
            this.nombre = nombre;
        }
    }

    static class ResultadoAnalisis {
        List<Object[]> filasTokens = new ArrayList<>();
        List<Object[]> filasSemantica = new ArrayList<>();
        List<ErrorSQL> errores = new ArrayList<>();
    }

    // --- MOTOR DE ANALISIS ---
    static class Analizador {
        static List<Token> tokens;
        static List<ErrorSQL> erroresLexicos;

        static void tokenizar(String sql) {
            tokens = new ArrayList<>();
            erroresLexicos = new ArrayList<>();
            String[] lineas = sql.split("\n");

            for (int li = 1; li <= lineas.length; li++) {
                String linea = lineas[li - 1];
                int i = 0;
                while (i < linea.length()) {
                    char c = linea.charAt(i);

                    if (Character.isWhitespace(c)) {
                        i++;
                        continue;
                    }

                    if (i + 1 < linea.length() && linea.substring(i, i + 2).equals("--"))
                        break;

                    String dos = (i + 1 < linea.length()) ? linea.substring(i, i + 2) : "";
                    if (RELACIONALES.containsKey(dos)) {
                        tokens.add(new Token(dos, 8, RELACIONALES.get(dos), li));
                        i += 2;
                        continue;
                    }

                    if (RELACIONALES.containsKey(String.valueOf(c))) {
                        tokens.add(new Token(String.valueOf(c), 8, RELACIONALES.get(String.valueOf(c)), li));
                        i++;
                        continue;
                    }

                    if (OPERADORES.containsKey(c)) {
                        tokens.add(new Token(String.valueOf(c), 7, OPERADORES.get(c), li));
                        i++;
                        continue;
                    }

                    if (c == '\'') {
                        boolean es_suelta = false;
                        if (!tokens.isEmpty()) {
                            Token ultimo = tokens.get(tokens.size() - 1);
                            if (ultimo.linea == li && (ultimo.tipo == 4 || ultimo.tipo == 6)) {
                                es_suelta = true;
                            }
                        }

                        if (es_suelta) {
                            tokens.add(new Token("'", 5, 54, li));
                            i++;
                        } else {
                            int j = i + 1;
                            while (j < linea.length() && linea.charAt(j) != '\'')
                                j++;

                            if (j >= linea.length()) {
                                erroresLexicos
                                        .add(new ErrorSQL(2, 205, li, "Se esperaba Delimitador (comilla de cierre ')"));
                                String valor = linea.substring(i + 1, j);
                                Token t = new Token(valor, 6, 0, li);
                                t.sub = 62;
                                t.es_const = true;
                                tokens.add(t);
                                i = j;
                            } else {
                                String valor = linea.substring(i + 1, j);
                                Token t = new Token(valor, 6, 0, li);
                                t.sub = 62;
                                t.es_const = true;
                                tokens.add(t);
                                i = j + 1;
                            }
                        }
                        continue;
                    }

                    if (DELIMITADORES.containsKey(c)) {
                        tokens.add(new Token(String.valueOf(c), 5, DELIMITADORES.get(c), li));
                        i++;
                        continue;
                    }

                    if (Character.isDigit(c)) {
                        int j = i;
                        while (j < linea.length() && (Character.isDigit(linea.charAt(j)) || linea.charAt(j) == '.'))
                            j++;
                        Token t = new Token(linea.substring(i, j), 6, 0, li);
                        t.sub = 61;
                        t.es_const = true;
                        tokens.add(t);
                        i = j;
                        continue;
                    }

                    if (Character.isLetter(c) || c == '_') {
                        int j = i;
                        while (j < linea.length() && (Character.isLetterOrDigit(linea.charAt(j))
                                || linea.charAt(j) == '_' || linea.charAt(j) == '#'))
                            j++;
                        String palabra = linea.substring(i, j).toUpperCase();
                        if (PALABRAS_RESERVADAS.containsKey(palabra)) {
                            tokens.add(new Token(palabra, 1, PALABRAS_RESERVADAS.get(palabra), li));
                        } else {
                            Token t = new Token(palabra, 4, 0, li);
                            t.es_ident = true;
                            tokens.add(t);
                        }
                        i = j;
                        continue;
                    }

                    erroresLexicos.add(new ErrorSQL(1, 101, li, "Símbolo desconocido: '" + c + "'"));
                    Token desc = new Token(String.valueOf(c), 9, 101, li);
                    desc.desconocido = true;
                    tokens.add(desc);
                    i++;
                }
            }
        }

        static ResultadoAnalisis analizarTodo(String sql) {
            tokenizar(sql);
            ResultadoAnalisis res = new ResultadoAnalisis();

            Map<String, Integer> mapaIdent = new HashMap<>();
            List<Token> listaConst = new ArrayList<>();
            int contIdent = 401;
            int contConst = 600;

            for (Token t : tokens) {
                if (t.es_ident && !mapaIdent.containsKey(t.lexema)) {
                    mapaIdent.put(t.lexema, contIdent++);
                }
                if (t.es_const) {
                    boolean encontrado = listaConst.stream().anyMatch(c -> c.lexema.equals(t.lexema) && c.sub == t.sub);
                    if (!encontrado) {
                        Token cte = new Token(t.lexema, t.tipo, contConst++, t.linea);
                        cte.sub = t.sub;
                        listaConst.add(cte);
                    }
                }
            }

            for (int idx = 0; idx < tokens.size(); idx++) {
                Token t = tokens.get(idx);
                int codigo;
                String tokenDisplay;

                if (t.tipo == 4) {
                    codigo = mapaIdent.get(t.lexema);
                    tokenDisplay = t.lexema;
                } else if (t.es_const) {
                    codigo = listaConst.stream().filter(c -> c.lexema.equals(t.lexema) && c.sub == t.sub).findFirst()
                            .get().codigo;
                    tokenDisplay = "CONSTANTE";
                } else if (t.desconocido) {
                    codigo = 101;
                    tokenDisplay = t.lexema;
                } else {
                    codigo = t.codigo;
                    tokenDisplay = t.lexema;
                }
                res.filasTokens.add(new Object[] { idx + 1, t.linea, tokenDisplay, t.tipo, codigo });
            }

            res.errores.addAll(erroresLexicos);
            ParserSintactico parser = new ParserSintactico(tokens);
            res.errores.addAll(parser.parsear());

            for (TablaSemantica tabla : parser.tablasSemanticas) {
                for (Columna columna : tabla.columnas) {
                    res.filasSemantica.add(new Object[] { tabla.nombre, columna.nombre, columna.tipo,
                            columna.notNull ? "YES" : "NO", columna.restriccion });
                }
                for (String restr : tabla.restricciones) {
                    res.filasSemantica.add(new Object[] { tabla.nombre, "", "", "", restr });
                }
            }

            return res;
        }
    }

    // --- PARSER SINTACTICO ---
    static class ParserSintactico {
        List<Token> toks = new ArrayList<>();
        List<ErrorSQL> errores = new ArrayList<>();
        List<TablaSemantica> tablasSemanticas = new ArrayList<>();
        TablaSemantica tablaActual;
        int pos = 0;

        ParserSintactico(List<Token> tokens) {
            for (Token t : tokens)
                if (t.tipo != 9)
                    toks.add(t);
        }

        Token ver() {
            return pos < toks.size() ? toks.get(pos) : null;
        }

        Token consumir() {
            return toks.get(pos++);
        }

        int lineaActual() {
            return ver() != null ? ver().linea : (toks.isEmpty() ? 0 : toks.get(toks.size() - 1).linea);
        }

        Token esperarTipo(int tipo, int codErr, String desc) {
            Token t = ver();
            if (t != null && t.tipo == tipo)
                return consumir();
            errores.add(new ErrorSQL(2, codErr, lineaActual(), desc));
            return null;
        }

        Token esperarReservada(String palabra) {
            Token t = ver();
            if (t != null && t.tipo == 1 && t.lexema.equals(palabra))
                return consumir();
            errores.add(new ErrorSQL(2, 201, lineaActual(), "Se esperaba Palabra Reservada '" + palabra + "'"));
            return null;
        }

        Token esperarDelimitador(String simbolo) {
            Token t = ver();
            if (t != null && t.tipo == 5 && t.lexema.equals(simbolo))
                return consumir();
            errores.add(new ErrorSQL(2, 205, lineaActual(), "Se esperaba Delimitador '" + simbolo + "'"));
            return null;
        }

        boolean esReservada(String palabra) {
            Token t = ver();
            return t != null && t.tipo == 1 && t.lexema.equals(palabra);
        }

        boolean esTipo(int tipo) {
            Token t = ver();
            return t != null && t.tipo == tipo;
        }

        List<ErrorSQL> parsear() {
            while (ver() != null) {
                Token t = ver();
                if (t.tipo == 1 && t.lexema.equals("SELECT"))
                    parsearSelect();
                else if (t.tipo == 1 && t.lexema.equals("CREATE"))
                    parsearCreateTable();
                else if (t.tipo == 1 && t.lexema.equals("INSERT"))
                    parsearInsert();
                else if (t.tipo == 5 && t.lexema.equals(";"))
                    consumir();
                else {
                    errores.add(new ErrorSQL(2, 201, t.linea, "Se esperaba SELECT, CREATE o INSERT al inicio"));
                    break;
                }

                if (ver() != null && ver().tipo == 5 && ver().lexema.equals(";")) {
                    consumir();
                }
            }
            return errores;
        }

        void parsearSelect() {
            consumir();
            if (esTipo(7) && ver().lexema.equals("*"))
                consumir();
            else {
                esperarTipo(4, 204, "Se esperaba Identificador (columna)");
                if (ver() != null && ver().tipo == 5 && ver().lexema.equals(".")) {
                    consumir();
                    esperarTipo(4, 204, "Se esperaba Identificador después de '.'");
                }
                while (ver() != null && ver().tipo == 5 && ver().lexema.equals(",")) {
                    consumir();
                    Token t = ver();
                    if (t == null || (t.tipo == 1 && t.lexema.equals("FROM"))) {
                        errores.add(new ErrorSQL(2, 204, lineaActual(), "Se esperaba Identificador después de ','"));
                        break;
                    }
                    esperarTipo(4, 204, "Se esperaba Identificador después de ','");
                    if (ver() != null && ver().tipo == 5 && ver().lexema.equals(".")) {
                        consumir();
                        esperarTipo(4, 204, "Se esperaba Identificador después de '.'");
                    }
                }
            }
            esperarReservada("FROM");
            Token t = ver();
            if (t == null || t.tipo == 1)
                errores.add(new ErrorSQL(2, 204, lineaActual(), "Se esperaba Identificador (tabla)"));
            else {
                esperarTipo(4, 204, "Se esperaba Identificador (tabla)");
                if (esTipo(4))
                    consumir();
                while (ver() != null && ver().tipo == 5 && ver().lexema.equals(",")) {
                    consumir();
                    t = ver();
                    if (t == null || (t.tipo == 1 && t.lexema.equals("WHERE"))) {
                        errores.add(new ErrorSQL(2, 204, lineaActual(), "Se esperaba Identificador después de ','"));
                        break;
                    }
                    esperarTipo(4, 204, "Se esperaba Identificador (tabla)");
                    if (esTipo(4))
                        consumir();
                }
            }
            if (esReservada("WHERE")) {
                consumir();
                parsearCondicion();
            }
        }

        // Inicio de la regla LL
        void parsearCondicion() {
            esperarTipo(4, 204, "Se esperaba Identificador en condición");
            if (ver() != null && ver().tipo == 5 && ver().lexema.equals(".")) {
                consumir();
                esperarTipo(4, 204, "Se esperaba Identificador después de '.'");
            }
            Token t = ver();
            if (t != null && t.tipo == 1 && t.lexema.equals("IN")) {
                consumir();
                esperarDelimitador("(");
                if (esReservada("SELECT"))
                    parsearSelect();
                else {
                    Token t2 = ver();
                    if (t2 != null && (t2.tipo == 4 || t2.tipo == 6))
                        consumir();
                    else
                        errores.add(new ErrorSQL(2, 206, lineaActual(), "Se esperaba Constante o Identificador"));
                    while (ver() != null && ver().tipo == 5 && ver().lexema.equals(",")) {
                        consumir();
                        t2 = ver();
                        if (t2 != null && (t2.tipo == 4 || t2.tipo == 6))
                            consumir();
                        else
                            errores.add(new ErrorSQL(2, 206, lineaActual(), "Se esperaba Constante o Identificador"));
                    }
                }
                esperarDelimitador(")");
            } else if (t != null && t.tipo == 8) {
                consumir();
                Token t2 = ver();
                if (t2 != null && (t2.tipo == 4 || t2.tipo == 6)) {
                    consumir();
                    if (ver() != null && ver().tipo == 5 && ver().lexema.equals(".")) {
                        consumir();
                        esperarTipo(4, 204, "Se esperaba Identificador después de '.'");
                    }
                } else
                    errores.add(new ErrorSQL(2, 206, lineaActual(), "Se esperaba Constante o Identificador"));
            } else {
                Token t2 = ver();
                if (t2 != null && t2.tipo == 5 && t2.lexema.equals("("))
                    errores.add(new ErrorSQL(2, 201, t2.linea, "Se esperaba Palabra Reservada (IN)"));
                else
                    errores.add(new ErrorSQL(2, 208, lineaActual(), "Se esperaba Operador Relacional"));
                return;
            }

            if (esReservada("AND") || esReservada("OR")) {
                consumir();
                parsearCondicion();
            } else {
                t = ver();
                if (t != null && !(t.tipo == 5 && t.lexema.equals(")")))
                    errores.add(new ErrorSQL(2, 201, t.linea, "Se esperaba Palabra Reservada (AND/OR)"));
            }
        }

        void parsearCreateTable() {
            consumir();
            esperarReservada("TABLE");
            Token tablaToken = ver();
            if (tablaToken != null && tablaToken.tipo == 4) {
                tablaActual = new TablaSemantica(tablaToken.lexema);
                tablasSemanticas.add(tablaActual);
            }
            esperarTipo(4, 204, "Se esperaba nombre de tabla");
            esperarDelimitador("(");
            parsearDefColumnas();
            esperarDelimitador(")");
            tablaActual = null;
        }

        void parsearDefColumnas() {
            parsearDefColODist();
            while (ver() != null && ver().tipo == 5 && ver().lexema.equals(",")) {
                consumir();
                parsearDefColODist();
            }
        }

        void parsearDefColODist() {
            if (ver() != null && ver().tipo == 1 && ver().lexema.equals("CONSTRAINT")) {
                parsearConstraint();
            } else {
                Token nombreColumna = ver();
                esperarTipo(4, 204, "Se esperaba nombre de columna");
                boolean notNull = false;
                String tipoDato = "";
                String restriccion = "";

                Token t = ver();
                // MODIFICADO: Ahora soporta CHAR, NUMERIC y DATE
                if (t != null && t.tipo == 1 && (t.lexema.equals("CHAR") || t.lexema.equals("NUMERIC") || t.lexema.equals("DATE"))) {
                    tipoDato = t.lexema;
                    consumir();
                    // AÑADIDO: Soporta parámetros (precision, scale) para NUMERIC
                    if (ver() != null && ver().lexema.equals("(")) {
                        consumir();
                        Token tam = esperarTipo(6, 206, "Se esperaba tamaño numérico");
                        if (tam != null)
                            tipoDato += "(" + tam.lexema;
                        // AÑADIDO: Soporte para segundo parámetro (escala) en NUMERIC(p,s)
                        if (ver() != null && ver().tipo == 5 && ver().lexema.equals(",")) {
                            consumir();
                            Token escala = esperarTipo(6, 206, "Se esperaba escala numérica");
                            if (escala != null)
                                tipoDato += "," + escala.lexema;
                        }
                        if (tipoDato.endsWith("("))
                            tipoDato = tipoDato.substring(0, tipoDato.length() - 1);
                        else
                            tipoDato += ")";
                        esperarDelimitador(")");
                    }
                } else {
                    errores.add(new ErrorSQL(2, 201, lineaActual(), "Se esperaba tipo de dato (CHAR, NUMERIC o DATE)"));
                }
                if (esReservada("NOT")) {
                    consumir();
                    esperarReservada("NULL");
                    notNull = true;
                }
                if (tablaActual != null && nombreColumna != null) {
                    tablaActual.columnas.add(new Columna(nombreColumna.lexema, tipoDato, notNull, restriccion));
                }
            }
        }

        void parsearConstraint() {
            consumir();
            Token nombreConstraint = esperarTipo(4, 204, "Se esperaba nombre de constante");
            Token t = ver();
            if (t != null && t.tipo == 1 && t.lexema.equals("PRIMARY")) {
                consumir();
                esperarReservada("KEY");
                esperarDelimitador("(");
                StringBuilder cols = new StringBuilder();
                Token col = esperarTipo(4, 204, "Se esperaba columna en PRIMARY KEY");
                if (col != null)
                    cols.append(col.lexema);
                while (ver() != null && ver().lexema.equals(",")) {
                    consumir();
                    col = esperarTipo(4, 204, "Se esperaba columna");
                    if (col != null)
                        cols.append(", ").append(col.lexema);
                }
                esperarDelimitador(")");
                if (tablaActual != null)
                    tablaActual.restricciones.add("CONSTRAINT " + (nombreConstraint != null ? nombreConstraint.lexema : "") + " PRIMARY KEY(" + cols + ")");
            } else if (t != null && t.tipo == 1 && t.lexema.equals("FOREIGN")) {
                consumir();
                esperarReservada("KEY");
                esperarDelimitador("(");
                Token col = esperarTipo(4, 204, "Se esperaba columna en FOREIGN KEY");
                esperarDelimitador(")");
                esperarReservada("REFERENCES");
                Token tablaRef = esperarTipo(4, 204, "Se esperaba tabla referenciada");
                esperarDelimitador("(");
                Token colRef = esperarTipo(4, 204, "Se esperaba columna referenciada");
                esperarDelimitador(")");
                if (tablaActual != null)
                    tablaActual.restricciones.add("CONSTRAINT " + (nombreConstraint != null ? nombreConstraint.lexema : "")
                            + " FOREIGN KEY(" + (col != null ? col.lexema : "") + ") REFERENCES "
                            + (tablaRef != null ? tablaRef.lexema : "") + "(" + (colRef != null ? colRef.lexema : "") + ")");
            } else if (t != null && t.tipo == 1 && t.lexema.equals("CHECK")) {
                consumir();
                esperarDelimitador("(");
                Token col = esperarTipo(4, 204, "Se esperaba columna en CHECK");
                String expr = "";
                if (ver() != null && ver().tipo == 8) {
                    expr = ver().lexema;
                    consumir();
                    Token valor = esperarTipo(6, 206, "Se esperaba valor en CHECK");
                    if (valor != null)
                        expr += " " + valor.lexema;
                }
                esperarDelimitador(")");
                if (tablaActual != null)
                    tablaActual.restricciones.add("CONSTRAINT " + (nombreConstraint != null ? nombreConstraint.lexema : "")
                            + " CHECK(" + (col != null ? col.lexema : "") + " " + expr + ")");
            } else {
                errores.add(new ErrorSQL(2, 201, lineaActual(), "Se esperaba PRIMARY, FOREIGN o CHECK después de CONSTRAINT"));
            }
        }

        void parsearInsert() {
            consumir();
            esperarReservada("INTO");
            esperarTipo(4, 204, "Se esperaba nombre de tabla");
            esperarDelimitador("(");
            esperarTipo(4, 204, "Se esperaba columna");
            while (ver() != null && ver().lexema.equals(",")) {
                consumir();
                esperarTipo(4, 204, "Se esperaba columna");
            }
            esperarDelimitador(")");
            esperarReservada("VALUES");
            esperarDelimitador("(");
            Token t = ver();
            if (t != null && (t.tipo == 4 || t.tipo == 6))
                consumir();
            else
                errores.add(new ErrorSQL(2, 206, lineaActual(), "Se esperaba valor o constante"));
            while (ver() != null && ver().lexema.equals(",")) {
                consumir();
                t = ver();
                if (t != null && (t.tipo == 4 || t.tipo == 6))
                    consumir();
                else
                    errores.add(new ErrorSQL(2, 206, lineaActual(), "Se esperaba valor después de ','"));
            }
            esperarDelimitador(")");
        }
    }

    // --- INTERFAZ GRAFICA ---
    private JTextArea txtSql;
    private JTabbedPane tabbedPane;
    private DefaultTableModel modTokens, modErr, modSem;
    private JLabel lblResultado;

    public pr06() {
        // Habilita el "Look and Feel" nativo de Windows (u OS host)
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        setTitle("Parser SQL - Reunión Natural Integrada");
        setSize(850, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        construirUI();
    }

    private void construirUI() {
        JPanel panelNorte = new JPanel(new BorderLayout(5, 5));
        panelNorte.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Título superior
        JLabel lblIngreso = new JLabel("Ingrese SQL:");
        lblIngreso.setFont(new Font("Tahoma", Font.BOLD, 12));
        panelNorte.add(lblIngreso, BorderLayout.NORTH);

        // Área de texto principal
        txtSql = new JTextArea(8, 50);
        txtSql.setFont(new Font("Monospaced", Font.PLAIN, 13));
        panelNorte.add(new JScrollPane(txtSql), BorderLayout.CENTER);

        // Botón ancho de analizar
        JButton btnAnalizar = new JButton("Analizar");
        btnAnalizar.setFont(new Font("Tahoma", Font.BOLD, 12));
        btnAnalizar.setPreferredSize(new Dimension(0, 30));
        panelNorte.add(btnAnalizar, BorderLayout.SOUTH);

        add(panelNorte, BorderLayout.NORTH);

        tabbedPane = new JTabbedPane();
        tabbedPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

        // Pestaña 1: Tokens
        modTokens = new DefaultTableModel(new String[] { "No.", "Línea", "Lexema", "Tipo", "Código" }, 0);
        JTable tablaTokens = new JTable(modTokens);
        tablaTokens.getTableHeader().setReorderingAllowed(false);
        tabbedPane.addTab("Tokens", new JScrollPane(tablaTokens));

        // Pestaña 2: Errores / Resultado
        JPanel panelErrores = new JPanel(new BorderLayout());
        lblResultado = new JLabel(" ");
        lblResultado.setFont(new Font("Tahoma", Font.BOLD, 12));
        lblResultado.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        panelErrores.add(lblResultado, BorderLayout.NORTH);

        modErr = new DefaultTableModel(new String[] { "No.", "Tipo", "Código", "Línea", "Descripción" }, 0);
        JTable tablaErrores = new JTable(modErr);
        tablaErrores.getTableHeader().setReorderingAllowed(false);
        panelErrores.add(new JScrollPane(tablaErrores), BorderLayout.CENTER);
        tabbedPane.addTab("Errores / Resultado", panelErrores);

        // Pestaña 3: Tabla Semántica
        modSem = new DefaultTableModel(new String[] { "Tabla", "Columna", "Tipo", "NOT NULL", "Restricción" }, 0);
        JTable tablaSemantica = new JTable(modSem);
        tablaSemantica.getTableHeader().setReorderingAllowed(false);
        tabbedPane.addTab("Tabla Semántica", new JScrollPane(tablaSemantica));

        add(tabbedPane, BorderLayout.CENTER);

        // Acción del botón
        btnAnalizar.addActionListener(e -> analizar());
    }

    private void analizar() {
        String sql = txtSql.getText().trim();
        if (sql.isEmpty()) {
            limpiarTablas();
            lblResultado.setText(" ");
            return;
        }

        limpiarTablas();
        ResultadoAnalisis res = Analizador.analizarTodo(sql);

        // Llenar tabla de Tokens
        for (Object[] fila : res.filasTokens) {
            modTokens.addRow(fila);
        }

        for (Object[] fila : res.filasSemantica) {
            modSem.addRow(fila);
        }

        // Mostrar errores o éxito
        if (!res.errores.isEmpty()) {
            int i = 1;
            for (ErrorSQL err : res.errores) {
                modErr.addRow(new Object[] { i++, err.tipo, err.codigo, err.linea, err.descripcion });
            }
            if (res.filasSemantica.isEmpty()) {
                tabbedPane.setSelectedIndex(1);
            } else {
                tabbedPane.setSelectedIndex(1);
            }
            lblResultado.setText("Se encontraron " + res.errores.size() + " error(es).");
            lblResultado.setForeground(Color.RED);
        } else {
            lblResultado.setText("Análisis sintáctico completado. Sentencia libre de errores.");
            lblResultado.setForeground(new Color(0, 128, 0)); // Verde oscuro
            tabbedPane.setSelectedIndex(2);
        }
    }

    private void limpiarTablas() {
        modTokens.setRowCount(0);
        modErr.setRowCount(0);
        modSem.setRowCount(0);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new pr06().setVisible(true);
        });
    }
}
