package VSCode.Avance_5;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.*;
import java.util.List;

public class Scanner5 extends JFrame {

    // ── ESTRUCTURAS DE DATOS GLOBALES ─────────────────────────────────────────

    private static final Map<String, Integer> PALABRAS_RESERVADAS = new HashMap<>();
    private static final Map<Character, Integer> DELIMITADORES = new HashMap<>();
    private static final Map<Character, Integer> OPERADORES = new HashMap<>();
    private static final Map<String, Integer> RELACIONALES = new HashMap<>();

    static {
        PALABRAS_RESERVADAS.put("SELECT", 10); PALABRAS_RESERVADAS.put("FROM", 11);
        PALABRAS_RESERVADAS.put("WHERE", 12);  PALABRAS_RESERVADAS.put("IN", 13);
        PALABRAS_RESERVADAS.put("AND", 14);    PALABRAS_RESERVADAS.put("OR", 15);
        PALABRAS_RESERVADAS.put("CREATE", 16); PALABRAS_RESERVADAS.put("TABLE", 17);
        PALABRAS_RESERVADAS.put("CHAR", 18);   PALABRAS_RESERVADAS.put("NUMERIC", 19);
        PALABRAS_RESERVADAS.put("NOT", 20);    PALABRAS_RESERVADAS.put("NULL", 21);
        PALABRAS_RESERVADAS.put("CONSTRAINT", 22); PALABRAS_RESERVADAS.put("KEY", 23);
        PALABRAS_RESERVADAS.put("PRIMARY", 24); PALABRAS_RESERVADAS.put("FOREIGN", 25);
        PALABRAS_RESERVADAS.put("REFERENCES", 26); PALABRAS_RESERVADAS.put("INSERT", 27);
        PALABRAS_RESERVADAS.put("INTO", 28);   PALABRAS_RESERVADAS.put("VALUES", 29);
        PALABRAS_RESERVADAS.put("DISTINCT", 30);

        DELIMITADORES.put(',', 50); DELIMITADORES.put('.', 51);
        DELIMITADORES.put('(', 52); DELIMITADORES.put(')', 53);
        DELIMITADORES.put('\'', 54); DELIMITADORES.put(';', 55);

        OPERADORES.put('+', 70); OPERADORES.put('-', 71);
        OPERADORES.put('*', 72); OPERADORES.put('/', 73);

        RELACIONALES.put(">=", 84); RELACIONALES.put("<=", 85);
        RELACIONALES.put(">", 81);  RELACIONALES.put("<", 82);
        RELACIONALES.put("=", 83);
    }

    // Clases auxiliares
    static class Tabla {
        int no, no_atributos, no_restricciones;
        String nombre;
        boolean de_bd;
        public Tabla(int no, String nombre, int no_atributos, int no_restricciones) {
            this.no = no; this.nombre = nombre; this.no_atributos = no_atributos; this.no_restricciones = no_restricciones;
        }
    }

    static class Atributo {
        int no_tabla, no_atr, longitud, no_nulo;
        String nombre, tipo;
        boolean de_bd;
        public Atributo(int no_tabla, int no_atr, String nombre, String tipo, int longitud, int no_nulo) {
            this.no_tabla = no_tabla; this.no_atr = no_atr; this.nombre = nombre; this.tipo = tipo; this.longitud = longitud; this.no_nulo = no_nulo;
        }
    }

    static class Restriccion {
        int no_tabla, no_res, tipo;
        String nombre, atr_asoc, tabla_ref, atr_ref;
        boolean de_bd;
        public Restriccion(int no_tabla, int no_res, int tipo, String nombre, String atr_asoc, String tabla_ref, String atr_ref) {
            this.no_tabla = no_tabla; this.no_res = no_res; this.tipo = tipo; this.nombre = nombre; this.atr_asoc = atr_asoc; this.tabla_ref = tabla_ref; this.atr_ref = atr_ref;
        }
    }

    static class Token {
        String lexema;
        int tipo, codigo = -1, linea, sub = -1;
        boolean es_const, es_ident, desconocido;
        public Token(String lexema, int tipo, int codigo, int linea) { this.lexema = lexema; this.tipo = tipo; this.codigo = codigo; this.linea = linea; }
    }

    static class ErrorDML {
        int tipo, codigo, linea;
        String descripcion;
        public ErrorDML(int tipo, int codigo, int linea, String descripcion) {
            this.tipo = tipo; this.codigo = codigo; this.linea = linea; this.descripcion = descripcion;
        }
    }

    private List<Tabla> ts_tablas = new ArrayList<>();
    private List<Atributo> ts_atributos = new ArrayList<>();
    private List<Restriccion> ts_restricciones = new ArrayList<>();

    // ── INTERFAZ GRÁFICA (SWING) ──────────────────────────────────────────────
    
    private JTextArea txt_sql;
    private JLabel lbl_resultado;
    private JLabel lbl_estadoTS;
    private JTable arbol_sem, arbol_id, arbol_ct, arbol_ts_tablas, arbol_ts_atributos, arbol_ts_restricciones, arbol_err;
    private DefaultTableModel mod_sem, mod_id, mod_ct, mod_tablas, mod_atributos, mod_restricciones, mod_err;
    private JTabbedPane nb;

    public Scanner5() {
        setTitle("Scanner SQL");
        setSize(1000, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        construirUI();
        ts_inicializar();
    }

    private void construirUI() {
        Container cp = getContentPane();
        cp.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.weightx = 1.0;

        // 1. PANEL SUPERIOR: Opciones de Tablas Semánticas
        JPanel panelOpcionesTS = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelOpcionesTS.setBorder(BorderFactory.createTitledBorder("OPCIONES DE TABLAS SEMÁNTICAS"));
        
        JButton btnActualizar = new JButton("✓ Actualizar Tablas Semánticas");
        JButton btnInicializar = new JButton("X Inicializar Tablas Semánticas");
        lbl_estadoTS = new JLabel("Estado: Esperando...");
        lbl_estadoTS.setForeground(new Color(0, 150, 0)); 
        
        btnActualizar.addActionListener(e -> ts_actualizar());
        btnInicializar.addActionListener(e -> ts_inicializar());

        panelOpcionesTS.add(btnActualizar);
        panelOpcionesTS.add(btnInicializar);
        panelOpcionesTS.add(Box.createHorizontalStrut(10));
        panelOpcionesTS.add(lbl_estadoTS);

        gbc.gridy = 0;
        gbc.weighty = 0.0;
        cp.add(panelOpcionesTS, gbc);

        // 2. PANEL CENTRAL: Módulo de Entrada
        JPanel panelEntrada = new JPanel(new BorderLayout());
        panelEntrada.setBorder(BorderFactory.createTitledBorder("MÓDULO DE ENTRADA - Sentencias SQL (DDL/DML)"));
        
        txt_sql = new JTextArea(10, 50);
        txt_sql.setFont(new Font("Monospaced", Font.PLAIN, 13));
        panelEntrada.add(new JScrollPane(txt_sql), BorderLayout.CENTER);

        JPanel panelBotonesEntrada = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnAnalizar = new JButton("☐ Analizar SQL");
        JButton btnLimpiar = new JButton("X Limpiar");
        
        btnAnalizar.addActionListener(e -> analizarSQL());
        btnLimpiar.addActionListener(e -> limpiar());

        panelBotonesEntrada.add(btnAnalizar);
        panelBotonesEntrada.add(btnLimpiar);
        panelEntrada.add(panelBotonesEntrada, BorderLayout.SOUTH);

        gbc.gridy = 1;
        gbc.weighty = 0.4;
        cp.add(panelEntrada, gbc);

        // 3. PANEL INFERIOR: Módulo de Resultados
        JPanel panelResultados = new JPanel(new BorderLayout());
        panelResultados.setBorder(BorderFactory.createTitledBorder("MÓDULO DE RESULTADOS"));
        
        nb = new JTabbedPane();
        
        mod_sem = new DefaultTableModel(new String[]{"No.", "Linea", "TOKEN", "Tipo", "Codigo"}, 0);
        arbol_sem = new JTable(mod_sem);
        nb.addTab("Tabla Semantica", new JScrollPane(arbol_sem));

        mod_id = new DefaultTableModel(new String[]{"Identificador", "Valor", "Linea(s)"}, 0);
        arbol_id = new JTable(mod_id);
        nb.addTab("Identificadores", new JScrollPane(arbol_id));

        mod_ct = new DefaultTableModel(new String[]{"No.", "Constante", "Tipo", "Valor"}, 0);
        arbol_ct = new JTable(mod_ct);
        nb.addTab("Constantes", new JScrollPane(arbol_ct));

        mod_tablas = new DefaultTableModel(new String[]{"No. Tabla", "Nombre", "No. Atributos", "No. Restricciones"}, 0);
        arbol_ts_tablas = new JTable(mod_tablas);
        nb.addTab("TS Tablas", new JScrollPane(arbol_ts_tablas));

        mod_atributos = new DefaultTableModel(new String[]{"No. Tabla", "No. Atributo", "Nombre", "Tipo", "Longitud", "No Nulo"}, 0);
        arbol_ts_atributos = new JTable(mod_atributos);
        nb.addTab("TS Atributos", new JScrollPane(arbol_ts_atributos));

        mod_restricciones = new DefaultTableModel(new String[]{"No. Tabla", "No. Restriccion", "Tipo", "Nombre", "Atr. Asoc.", "Tabla Ref.", "Atr. Ref."}, 0);
        arbol_ts_restricciones = new JTable(mod_restricciones);
        nb.addTab("TS Restricciones", new JScrollPane(arbol_ts_restricciones));

        JPanel panelErrores = new JPanel(new BorderLayout(5,5));
        lbl_resultado = new JLabel(" ");
        lbl_resultado.setFont(new Font("Arial", Font.BOLD, 12));
        panelErrores.add(lbl_resultado, BorderLayout.NORTH);
        
        mod_err = new DefaultTableModel(new String[]{"No.", "Tipo", "Codigo", "Linea", "Descripcion"}, 0);
        arbol_err = new JTable(mod_err);
        panelErrores.add(new JScrollPane(arbol_err), BorderLayout.CENTER);
        nb.addTab("Errores", panelErrores);

        panelResultados.add(nb, BorderLayout.CENTER);

        gbc.gridy = 2;
        gbc.weighty = 0.6;
        cp.add(panelResultados, gbc);
    }

    // ── LÓGICA DE TABLAS SEMÁNTICAS ───────────────────────────────────────────
    
    private void ts_inicializar() {
        ts_tablas.clear();
        ts_atributos.clear();
        ts_restricciones.clear();
        refrescarTS();
        lbl_estadoTS.setText("Estado: Tablas semánticas inicializadas (Vacías)");
        lbl_estadoTS.setForeground(new Color(0, 100, 200)); 
    }

    private void ts_actualizar() {
        ts_tablas.clear(); ts_atributos.clear(); ts_restricciones.clear();

        ts_tablas.add(new Tabla(1, "DEPARTAMENTOS", 2, 1));
        ts_tablas.add(new Tabla(2, "CARRERAS", 5, 2));
        ts_tablas.add(new Tabla(3, "ALUMNOS", 5, 2));
        ts_tablas.add(new Tabla(4, "MATERIAS", 4, 2));
        ts_tablas.add(new Tabla(5, "PROFESORES", 7, 2));
        ts_tablas.add(new Tabla(6, "INSCRITOS", 7, 4));

        ts_atributos.add(new Atributo(1, 1, "D#", "CHAR", 2, 1));
        ts_atributos.add(new Atributo(1, 2, "DNOMBRE", "CHAR", 6, 1));
        ts_atributos.add(new Atributo(2, 3, "C#", "CHAR", 2, 1));
        ts_atributos.add(new Atributo(2, 4, "CNOMBRE", "CHAR", 3, 1));
        ts_atributos.add(new Atributo(2, 5, "VIGENCIA", "CHAR", 4, 1));
        ts_atributos.add(new Atributo(2, 6, "SEMESTRES", "NUMERIC", 2, 1));
        ts_atributos.add(new Atributo(2, 7, "D#", "CHAR", 2, 1));
        ts_atributos.add(new Atributo(3, 8, "A#", "CHAR", 2, 1));
        ts_atributos.add(new Atributo(3, 9, "ANOMBRE", "CHAR", 20, 1));
        ts_atributos.add(new Atributo(3, 10, "GENERACION", "CHAR", 4, 1));
        ts_atributos.add(new Atributo(3, 11, "SEXO", "CHAR", 1, 1));
        ts_atributos.add(new Atributo(3, 12, "C#", "CHAR", 2, 1));
        ts_atributos.add(new Atributo(4, 13, "M#", "CHAR", 2, 1));
        ts_atributos.add(new Atributo(4, 14, "MNOMBRE", "CHAR", 6, 1));
        ts_atributos.add(new Atributo(4, 15, "CREDITOS", "NUMERIC", 2, 1));
        ts_atributos.add(new Atributo(4, 16, "C#", "CHAR", 2, 1));
        ts_atributos.add(new Atributo(5, 17, "P#", "CHAR", 2, 1));
        ts_atributos.add(new Atributo(5, 18, "PNOMBRE", "CHAR", 20, 1));
        ts_atributos.add(new Atributo(5, 19, "EDAD", "NUMERIC", 2, 1));
        ts_atributos.add(new Atributo(5, 20, "SEXO", "CHAR", 1, 1));
        ts_atributos.add(new Atributo(5, 21, "ESP", "CHAR", 4, 1));
        ts_atributos.add(new Atributo(5, 22, "GRADO", "CHAR", 3, 1));
        ts_atributos.add(new Atributo(5, 23, "D#", "CHAR", 2, 1));
        ts_atributos.add(new Atributo(6, 24, "R#", "CHAR", 3, 1));
        ts_atributos.add(new Atributo(6, 25, "A#", "CHAR", 2, 1));
        ts_atributos.add(new Atributo(6, 26, "M#", "CHAR", 2, 1));
        ts_atributos.add(new Atributo(6, 27, "P#", "CHAR", 2, 1));
        ts_atributos.add(new Atributo(6, 28, "TURNO", "CHAR", 1, 1));
        ts_atributos.add(new Atributo(6, 29, "SEMESTRE", "CHAR", 6, 1));
        ts_atributos.add(new Atributo(6, 30, "CALIFICACION", "NUMERIC", 3, 1));

        ts_restricciones.add(new Restriccion(1, 1, 1, "PK_DEPARTAMENTOS", "1", "-", "-"));
        ts_restricciones.add(new Restriccion(2, 2, 1, "PK_CARRERAS", "3", "-", "-"));
        ts_restricciones.add(new Restriccion(2, 3, 2, "FK_CARRERAS", "7", "1", "1"));
        ts_restricciones.add(new Restriccion(3, 4, 1, "PK_ALUMNOS", "8", "-", "-"));
        ts_restricciones.add(new Restriccion(3, 5, 2, "FK_ALUMNOS", "12", "2", "3"));
        ts_restricciones.add(new Restriccion(4, 6, 1, "PK_MATERIAS", "13", "-", "-"));
        ts_restricciones.add(new Restriccion(4, 7, 2, "FK_MATERIAS", "16", "2", "3"));
        ts_restricciones.add(new Restriccion(5, 8, 1, "PK_PROFESORES", "17", "-", "-"));
        ts_restricciones.add(new Restriccion(5, 9, 2, "FK_PROFESORES", "23", "1", "1"));
        ts_restricciones.add(new Restriccion(6, 10, 1, "PK_INSCRITOS", "24", "-", "-"));
        ts_restricciones.add(new Restriccion(6, 11, 2, "FK_INSCRITOS_01", "25", "3", "8"));
        ts_restricciones.add(new Restriccion(6, 12, 2, "FK_INSCRITOS_02", "26", "4", "13"));
        ts_restricciones.add(new Restriccion(6, 13, 2, "FK_INSCRITOS_03", "27", "5", "17"));

        for(Tabla t : ts_tablas) t.de_bd = true;
        for(Atributo a : ts_atributos) a.de_bd = true;
        for(Restriccion r : ts_restricciones) r.de_bd = true;
        
        refrescarTS();
        lbl_estadoTS.setText("Estado: Tablas semánticas cargadas (BD Inscritos)");
        lbl_estadoTS.setForeground(new Color(0, 150, 0)); 
    }

    private void refrescarTS() {
        mod_tablas.setRowCount(0); mod_atributos.setRowCount(0); mod_restricciones.setRowCount(0);
        for(Tabla t : ts_tablas) mod_tablas.addRow(new Object[]{t.no, t.nombre, t.no_atributos, t.no_restricciones});
        for(Atributo a : ts_atributos) mod_atributos.addRow(new Object[]{a.no_tabla, a.no_atr, a.nombre, a.tipo, a.longitud, a.no_nulo});
        for(Restriccion r : ts_restricciones) mod_restricciones.addRow(new Object[]{r.no_tabla, r.no_res, r.tipo, r.nombre, r.atr_asoc, r.tabla_ref, r.atr_ref});
    }

    private void limpiarArboles() {
        mod_sem.setRowCount(0); mod_id.setRowCount(0); mod_ct.setRowCount(0); mod_err.setRowCount(0);
    }

    private void limpiar() {
        txt_sql.setText("");
        limpiarArboles();
        lbl_resultado.setText(" ");
    }

    // ── LÓGICA DE ANÁLISIS CON PREVENCIÓN DE CIERRE (Punto 5) ─────────────────

    private void analizarSQL() {
        String sql = txt_sql.getText().trim();
        if (sql.isEmpty()) return;
        limpiarArboles();

        // Envoltorio try-catch crítico para cumplir regla de "no terminación abrupta"
        try {
            ts_tablas.removeIf(t -> !t.de_bd);
            ts_atributos.removeIf(a -> !a.de_bd);
            ts_restricciones.removeIf(r -> !r.de_bd);

            List<ErrorDML> erroresLexicos = new ArrayList<>();
            List<Token> brutos = tokenizar(sql, erroresLexicos);

            Map<String, Map<String, Object>> mapaIdent = new LinkedHashMap<>();
            List<Map<String, Object>> listaConst = new ArrayList<>();
            int contIdent = 401;
            int contConst = 600;

            for (int i = 0; i < brutos.size(); i++) {
                Token t = brutos.get(i);
                if (t.es_ident) {
                    mapaIdent.putIfAbsent(t.lexema, new HashMap<>());
                    mapaIdent.get(t.lexema).putIfAbsent("valor", contIdent++);
                    mapaIdent.get(t.lexema).putIfAbsent("lineas", new ArrayList<Integer>());
                    List<Integer> lineas = (List<Integer>) mapaIdent.get(t.lexema).get("lineas");
                    if (!lineas.contains(t.linea)) lineas.add(t.linea);
                }
                if (t.es_const) {
                    boolean encontrado = false;
                    for (Map<String, Object> c : listaConst) {
                        if (c.get("lexema").equals(t.lexema) && c.get("sub").equals(t.sub)) {
                            encontrado = true; break;
                        }
                    }
                    if (!encontrado) {
                        Map<String, Object> cmap = new HashMap<>();
                        cmap.put("lexema", t.lexema); cmap.put("sub", t.sub);
                        cmap.put("no", i + 1); cmap.put("valor", contConst++);
                        listaConst.add(cmap);
                    }
                }
            }

            int index = 1;
            for (Token t : brutos) {
                String tokenDisplay = t.lexema;
                int codigo = t.codigo;

                if (t.tipo == 4) {
                    codigo = (int) mapaIdent.get(t.lexema).get("valor");
                } else if (t.es_const) {
                    for (Map<String, Object> c : listaConst) {
                        if (c.get("lexema").equals(t.lexema) && c.get("sub").equals(t.sub)) {
                            codigo = (int) c.get("valor"); break;
                        }
                    }
                    tokenDisplay = "CONSTANTE";
                } else if (t.desconocido) {
                    codigo = 101;
                }
                mod_sem.addRow(new Object[]{index++, t.linea, tokenDisplay, t.tipo, codigo});
            }

            for (Map.Entry<String, Map<String, Object>> entry : mapaIdent.entrySet()) {
                List<Integer> lineas = (List<Integer>) entry.getValue().get("lineas");
                String lineasStr = lineas.toString().replaceAll("[\\[\\]]", "");
                mod_id.addRow(new Object[]{entry.getKey(), entry.getValue().get("valor"), lineasStr});
            }

            for (Map<String, Object> c : listaConst) {
                mod_ct.addRow(new Object[]{c.get("no"), c.get("lexema"), c.get("sub"), c.get("valor")});
            }

            List<ErrorDML> erroresSintSem = analizadorSintactico(brutos);
            List<ErrorDML> todosErrores = new ArrayList<>(erroresLexicos);
            todosErrores.addAll(erroresSintSem);

            refrescarTS();

            if (!todosErrores.isEmpty()) {
                int errIdx = 1;
                for (ErrorDML e : todosErrores) {
                    mod_err.addRow(new Object[]{errIdx++, e.tipo, e.codigo, e.linea, e.descripcion});
                }
                lbl_resultado.setText("Se encontraron " + todosErrores.size() + " error(es).");
                lbl_resultado.setForeground(Color.RED);
                nb.setSelectedIndex(6); 
            } else {
                lbl_resultado.setText("Sentencia libre de errores. (Código 200)");
                lbl_resultado.setForeground(new Color(0, 150, 0));
            }
        } catch (Exception ex) {
            // Regla #5: Prevenir caida abrupta
            mod_err.addRow(new Object[]{"-", 0, 0, "-", "Se previno una terminación abrupta. Problema lógico atrapado: " + ex.getMessage()});
            lbl_resultado.setText("Error abrupto atrapado. Traductor sigue disponible.");
            lbl_resultado.setForeground(Color.RED);
            nb.setSelectedIndex(6);
        }
    }

    private List<Token> tokenizar(String sql, List<ErrorDML> errores) {
        List<Token> tokens = new ArrayList<>();
        String[] lineas = sql.split("\n");

        for (int li = 1; li <= lineas.length; li++) {
            String linea = lineas[li - 1];
            int i = 0;
            while (i < linea.length()) {
                char c = linea.charAt(i);
                if (Character.isWhitespace(c)) { i++; continue; }
                
                if (i + 1 < linea.length() && linea.substring(i, i + 2).equals("--")) break;

                if (i + 1 < linea.length() && RELACIONALES.containsKey(linea.substring(i, i + 2))) {
                    tokens.add(new Token(linea.substring(i, i + 2), 8, RELACIONALES.get(linea.substring(i, i + 2)), li));
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
                        while (j < linea.length() && linea.charAt(j) != '\'') j++;
                        if (j >= linea.length()) {
                            errores.add(new ErrorDML(2, 205, li, "Se esperaba Delimitador (comilla de cierre ')"));
                            Token t = new Token(linea.substring(i + 1, j), 6, -1, li);
                            t.es_const = true; t.sub = 62; tokens.add(t);
                            i = j;
                        } else {
                            Token t = new Token(linea.substring(i + 1, j), 6, -1, li);
                            t.es_const = true; t.sub = 62; tokens.add(t);
                            i = j + 1;
                        }
                    }
                    continue;
                }

                if (DELIMITADORES.containsKey(c)) {
                    tokens.add(new Token(String.valueOf(c), 5, DELIMITADORES.get(c), li));
                    i++; continue;
                }

                if (Character.isDigit(c)) {
                    int j = i;
                    while (j < linea.length() && (Character.isDigit(linea.charAt(j)) || linea.charAt(j) == '.')) j++;
                    Token t = new Token(linea.substring(i, j), 6, -1, li);
                    t.es_const = true; t.sub = 61; tokens.add(t);
                    i = j; continue;
                }

                if (Character.isLetter(c) || c == '_') {
                    int j = i;
                    while (j < linea.length() && (Character.isLetterOrDigit(linea.charAt(j)) || linea.charAt(j) == '_' || linea.charAt(j) == '#')) j++;
                    String palabra = linea.substring(i, j).toUpperCase();
                    if (PALABRAS_RESERVADAS.containsKey(palabra)) {
                        tokens.add(new Token(palabra, 1, PALABRAS_RESERVADAS.get(palabra), li));
                    } else {
                        Token t = new Token(palabra, 4, -1, li);
                        t.es_ident = true; tokens.add(t);
                    }
                    i = j; continue;
                }

                errores.add(new ErrorDML(1, 101, li, "Símbolo desconocido: '" + c + "'"));
                Token t = new Token(String.valueOf(c), 9, 101, li);
                t.desconocido = true; tokens.add(t);
                i++;
            }
        }
        return tokens;
    }

    // ── ANALIZADOR SINTÁCTICO / SEMÁNTICO ─────────────────────────────────────

    private List<ErrorDML> analizadorSintactico(List<Token> tokensEntrada) {
        List<ErrorDML> errores = new ArrayList<>();
        List<Token> toks = new ArrayList<>();
        for (Token t : tokensEntrada) if (t.tipo != 9) toks.add(t);
        if (toks.isEmpty()) return errores;

        int[] pos = {0};

        class Parser {
            Token ver() { return pos[0] < toks.size() ? toks.get(pos[0]) : null; }
            Token consumir() { return toks.get(pos[0]++); }
            int linea_actual() { Token t = ver(); return t != null ? t.linea : toks.get(toks.size() - 1).linea; }

            Token esperar_tipo(int tipo, int cod_err, String desc) {
                Token t = ver();
                if (t != null && t.tipo == tipo) return consumir();
                errores.add(new ErrorDML(2, cod_err, linea_actual(), desc));
                return null;
            }

            Token esperar_reservada(String palabra) {
                Token t = ver();
                if (t != null && t.tipo == 1 && t.lexema.equals(palabra)) return consumir();
                errores.add(new ErrorDML(2, 201, linea_actual(), "Se esperaba Palabra Reservada '" + palabra + "'"));
                return null;
            }

            Token esperar_delimitador(String simbolo) {
                Token t = ver();
                if (t != null && t.tipo == 5 && t.lexema.equals(simbolo)) return consumir();
                errores.add(new ErrorDML(2, 205, linea_actual(), "Se esperaba Delimitador '" + simbolo + "'"));
                return null;
            }

            boolean es_reservada(String palabra) { Token t = ver(); return t != null && t.tipo == 1 && t.lexema.equals(palabra); }
            boolean es_tipo(int tipo) { Token t = ver(); return t != null && t.tipo == tipo; }

            Tabla buscar_tabla(String nombre) {
                for (Tabla t : ts_tablas) if (t.nombre.equals(nombre)) return t;
                return null;
            }

            Atributo buscar_atributo_en_tabla(int no_tabla, String nombre_atr) {
                for (Atributo a : ts_atributos) if (a.no_tabla == no_tabla && a.nombre.equals(nombre_atr)) return a;
                return null;
            }

            Restriccion buscar_restriccion(String nombre) {
                for (Restriccion r : ts_restricciones) if (r.nombre.equals(nombre)) return r;
                return null;
            }

            class ColSelect { String tabla, col; int linea; ColSelect(String t, String c, int l){ tabla=t; col=c; linea=l;} }
            class TablaFrom { String nombre, alias; int linea; TablaFrom(String n, String a, int l){ nombre=n; alias=a; linea=l;} }
            class TablaValida { String nombre, alias; int no; TablaValida(String n, String a, int num){ nombre=n; alias=a; no=num;} }

            void validar_columna_select(String nombre_tabla_col, String nombre_col, int linea_col, Map<String, Integer> mapa_tablas, List<TablaValida> tablas_validas) {
                if (ts_tablas.isEmpty()) return;
                if (nombre_tabla_col != null) {
                    if (!mapa_tablas.containsKey(nombre_tabla_col)) {
                        errores.add(new ErrorDML(3, 315, linea_col, "El identificador \"" + nombre_tabla_col + "." + nombre_col + "\" no es válido."));
                        return;
                    }
                    int no_tabla = mapa_tablas.get(nombre_tabla_col);
                    if (buscar_atributo_en_tabla(no_tabla, nombre_col) == null) {
                        errores.add(new ErrorDML(3, 311, linea_col, "El nombre del atributo \"" + nombre_col + "\" no es válido."));
                    }
                } else {
                    List<TablaValida> encontrados = new ArrayList<>();
                    for (TablaValida tv : tablas_validas) {
                        if (buscar_atributo_en_tabla(tv.no, nombre_col) != null) encontrados.add(tv);
                    }
                    if (encontrados.isEmpty()) errores.add(new ErrorDML(3, 311, linea_col, "El nombre del atributo \"" + nombre_col + "\" no es válido."));
                    else if (encontrados.size() > 1) errores.add(new ErrorDML(3, 312, linea_col, "El nombre del atributo \"" + nombre_col + "\" es ambigüo."));
                }
            }

            Atributo obtener_atributo(String nombre_tabla_col, String nombre_col, Map<String, Integer> mapa_tablas) {
                if (nombre_col == null) return null;
                if (nombre_tabla_col != null && mapa_tablas.containsKey(nombre_tabla_col)) {
                    return buscar_atributo_en_tabla(mapa_tablas.get(nombre_tabla_col), nombre_col);
                }
                Set<Integer> uniqueTabs = new HashSet<>(mapa_tablas.values());
                for (int no_t : uniqueTabs) {
                    Atributo atr = buscar_atributo_en_tabla(no_t, nombre_col);
                    if (atr != null) return atr;
                }
                return null;
            }

            void validar_columna_condicion(String nombre_tabla_col, String nombre_col, int linea_col, Map<String, Integer> mapa_tablas, List<TablaValida> tablas_validas) {
                if (nombre_col == null || nombre_col.isEmpty()) return;
                validar_columna_select(nombre_tabla_col, nombre_col, linea_col, mapa_tablas, tablas_validas);
            }

            void parsear_condicion(Map<String, Integer> mapa_tablas, List<TablaValida> tablas_validas) {
                Token t_ident = ver();
                String nombre_izq = (t_ident != null && t_ident.tipo == 4) ? t_ident.lexema : null;
                int linea_izq = t_ident != null ? t_ident.linea : linea_actual();
                esperar_tipo(4, 204, "Se esperaba Identificador en condición");

                String nombre_tabla_izq = null, nombre_col_izq = nombre_izq;

                if (ver() != null && ver().tipo == 5 && ver().lexema.equals(".")) {
                    consumir();
                    Token t2 = ver();
                    nombre_tabla_izq = nombre_izq;
                    nombre_col_izq = t2 != null ? t2.lexema : "";
                    if (t2 != null) linea_izq = t2.linea;
                    esperar_tipo(4, 204, "Se esperaba Identificador después de '.'");
                }

                if (!ts_tablas.isEmpty() && !tablas_validas.isEmpty()) {
                    validar_columna_condicion(nombre_tabla_izq, nombre_col_izq, linea_izq, mapa_tablas, tablas_validas);
                }

                Token t = ver();
                if (t != null && t.tipo == 1 && t.lexema.equals("IN")) {
                    consumir(); esperar_delimitador("(");
                    if (es_reservada("SELECT")) parsear_select();
                    else {
                        Token t2 = ver();
                        if (t2 != null && (t2.tipo == 4 || t2.tipo == 6)) consumir();
                        else errores.add(new ErrorDML(2, 206, linea_actual(), "Se esperaba Constante o Identificador"));
                        while (ver() != null && ver().tipo == 5 && ver().lexema.equals(",")) {
                            consumir();
                            Token t3 = ver();
                            if (t3 != null && (t3.tipo == 4 || t3.tipo == 6)) consumir();
                            else errores.add(new ErrorDML(2, 206, linea_actual(), "Se esperaba Constante o Identificador"));
                        }
                    }
                    esperar_delimitador(")");
                } else if (t != null && t.tipo == 8) {
                    consumir();
                    Token t2 = ver();
                    
                    // Modificación: Revisión a la derecha del operador relacional (Caso ESTDAT vs constante numérica)
                    if (t2 != null && t2.tipo == 4) { // Identificador en vez de constante
                        String nombre_izq2 = t2.lexema;
                        String nombre_tabla_izq2 = null;
                        String nombre_col_izq2 = nombre_izq2;
                        int linea_izq2 = t2.linea;
                        consumir();

                        if (ver() != null && ver().tipo == 5 && ver().lexema.equals(".")) {
                            consumir();
                            Token t3 = ver();
                            if (t3 != null && t3.tipo == 4) {
                                nombre_tabla_izq2 = nombre_izq2;
                                nombre_col_izq2 = t3.lexema;
                                linea_izq2 = t3.linea;
                                consumir();
                            } else {
                                esperar_tipo(4, 204, "Se esperaba Identificador después de '.'");
                            }
                        }
                        if (!ts_tablas.isEmpty() && !tablas_validas.isEmpty()) {
                            validar_columna_condicion(nombre_tabla_izq2, nombre_col_izq2, linea_izq2, mapa_tablas, tablas_validas);
                        }
                    } else if (t2 != null && t2.tipo == 6) { // Constante literal pura
                        if (!ts_tablas.isEmpty() && !tablas_validas.isEmpty() && t2.sub == 61) {
                            Atributo atr_izq = obtener_atributo(nombre_tabla_izq, nombre_col_izq, mapa_tablas);
                            if (atr_izq != null && atr_izq.tipo.equals("CHAR")) {
                                errores.add(new ErrorDML(3, 313, t2.linea, "Error de conversión al convertir el valor del atributo '" + nombre_col_izq + "' del tipo char a tipo de dato int."));
                            }
                        }
                        consumir();
                    } else {
                        errores.add(new ErrorDML(2, 206, linea_actual(), "Se esperaba Constante o Identificador"));
                    }
                } else {
                    Token t2 = ver();
                    if (t2 != null && t2.tipo == 5 && t2.lexema.equals("(")) errores.add(new ErrorDML(2, 201, t2.linea, "Se esperaba Palabra Reservada (IN)"));
                    else errores.add(new ErrorDML(2, 208, linea_actual(), "Se esperaba Operador Relacional"));
                    return;
                }

                if (es_reservada("AND") || es_reservada("OR")) {
                    consumir(); parsear_condicion(mapa_tablas, tablas_validas);
                } else {
                    t = ver();
                    if (t != null && !(t.tipo == 5 && t.lexema.equals(")"))) {
                        errores.add(new ErrorDML(2, 201, t.linea, "Se esperaba Palabra Reservada (AND/OR)"));
                    }
                }
            }

            void parsear_select() {
                consumir(); // SELECT
                if (es_reservada("DISTINCT")) consumir();

                List<ColSelect> cols_select = new ArrayList<>();
                if (es_tipo(7) && ver().lexema.equals("*")) consumir();
                else {
                    Token t = ver();
                    if (t != null && t.tipo == 4) {
                        String nombre_col = t.lexema; int linea_col = t.linea; consumir();
                        if (ver() != null && ver().tipo == 5 && ver().lexema.equals(".")) {
                            consumir(); Token t2 = ver();
                            if (t2 != null && t2.tipo == 4) { cols_select.add(new ColSelect(nombre_col, t2.lexema, t2.linea)); consumir(); }
                            else esperar_tipo(4, 204, "Se esperaba Identificador después de '.'");
                        } else cols_select.add(new ColSelect(null, nombre_col, linea_col));
                    } else esperar_tipo(4, 204, "Se esperaba Identificador (columna)");

                    while (ver() != null && ver().tipo == 5 && ver().lexema.equals(",")) {
                        consumir(); Token t2 = ver();
                        if (t2 == null || (t2.tipo == 1 && t2.lexema.equals("FROM"))) {
                            errores.add(new ErrorDML(2, 204, linea_actual(), "Se esperaba Identificador después de ','")); break;
                        }
                        if (t2.tipo == 4) {
                            String nombre_col = t2.lexema; int linea_col = t2.linea; consumir();
                            if (ver() != null && ver().tipo == 5 && ver().lexema.equals(".")) {
                                consumir(); Token t3 = ver();
                                if (t3 != null && t3.tipo == 4) { cols_select.add(new ColSelect(nombre_col, t3.lexema, t3.linea)); consumir(); }
                                else esperar_tipo(4, 204, "Se esperaba Identificador después de '.'");
                            } else cols_select.add(new ColSelect(null, nombre_col, linea_col));
                        } else esperar_tipo(4, 204, "Se esperaba Identificador después de ','");
                    }
                }

                esperar_reservada("FROM");
                List<TablaFrom> tablas_from = new ArrayList<>();
                Token t = ver();
                if (t == null || t.tipo == 1) errores.add(new ErrorDML(2, 204, linea_actual(), "Se esperaba Identificador (tabla)"));
                else {
                    Token t_tab = ver(); String nombre_tab = t_tab.lexema; int linea_tab = t_tab.linea; consumir();
                    String alias = null;
                    if (es_tipo(4)) { alias = ver().lexema; consumir(); }
                    tablas_from.add(new TablaFrom(nombre_tab, alias, linea_tab));

                    while (ver() != null && ver().tipo == 5 && ver().lexema.equals(",")) {
                        consumir(); Token t2 = ver();
                        if (t2 == null || (t2.tipo == 1 && t2.lexema.equals("WHERE"))) {
                            errores.add(new ErrorDML(2, 204, linea_actual(), "Se esperaba Identificador después de ','")); break;
                        }
                        Token t_tab2 = ver(); String nombre_tab2 = t_tab2.lexema; int linea_tab2 = t_tab2.linea; consumir();
                        String alias2 = null;
                        if (es_tipo(4)) { alias2 = ver().lexema; consumir(); }
                        tablas_from.add(new TablaFrom(nombre_tab2, alias2, linea_tab2));
                    }
                }

                List<TablaValida> tablas_validas = new ArrayList<>();
                for (TablaFrom tf : tablas_from) {
                    Tabla obj = buscar_tabla(tf.nombre);
                    if (obj == null) errores.add(new ErrorDML(3, 314, tf.linea, "El nombre de la tabla \"" + tf.nombre + "\" no es válido."));
                    else tablas_validas.add(new TablaValida(tf.nombre, tf.alias, obj.no));
                }

                Map<String, Integer> mapa_tablas = new HashMap<>();
                for (TablaValida tv : tablas_validas) {
                    mapa_tablas.put(tv.nombre, tv.no);
                    if (tv.alias != null) mapa_tablas.put(tv.alias, tv.no);
                }

                for (ColSelect cs : cols_select) validar_columna_select(cs.tabla, cs.col, cs.linea, mapa_tablas, tablas_validas);

                if (es_reservada("WHERE")) { consumir(); parsear_condicion(mapa_tablas, tablas_validas); }
            }

            void parsear_create_table() {
                consumir(); esperar_reservada("TABLE");
                Token t_nombre = ver();
                if (t_nombre == null || t_nombre.tipo != 4) { esperar_tipo(4, 204, "Se esperaba nombre de tabla"); return; }
                consumir(); String nombre_tabla = t_nombre.lexema; int linea_tabla = t_nombre.linea;

                if (buscar_tabla(nombre_tabla) != null) errores.add(new ErrorDML(3, 306, linea_tabla, "El nombre del atributo \"" + nombre_tabla + "\" está duplicado."));
                esperar_delimitador("(");

                List<Atributo> columnas = new ArrayList<>();
                List<Restriccion> restricciones = new ArrayList<>();
                
                parsear_def_columna_o_constraint(columnas, restricciones, nombre_tabla, linea_tabla);
                while (ver() != null && ver().tipo == 5 && ver().lexema.equals(",")) {
                    consumir();
                    if (ver() != null && ver().tipo == 5 && (ver().lexema.equals(")") || ver().lexema.equals(";"))) break;
                    parsear_def_columna_o_constraint(columnas, restricciones, nombre_tabla, linea_tabla);
                }

                esperar_delimitador(")");

                if (buscar_tabla(nombre_tabla) == null) {
                    int no_t = ts_tablas.size() + 1;
                    ts_tablas.add(new Tabla(no_t, nombre_tabla, columnas.size(), restricciones.size()));
                    for (Atributo a : columnas) { a.no_tabla = no_t; ts_atributos.add(a); }
                    for (Restriccion r : restricciones) { r.no_tabla = no_t; ts_restricciones.add(r); }
                }
            }

            void parsear_def_columna_o_constraint(List<Atributo> columnas, List<Restriccion> restricciones, String nombre_tabla, int linea_tabla) {
                if (ver() != null && ver().tipo == 1 && ver().lexema.equals("CONSTRAINT")) {
                    parsear_constraint(columnas, restricciones, nombre_tabla, linea_tabla);
                } else if (ver() != null && ver().tipo == 4) {
                    Token t_col = consumir(); String nombre_col = t_col.lexema; int linea_col = t_col.linea;
                    boolean exists = false;
                    for (Atributo a : columnas) if (a.nombre.equals(nombre_col)) { exists = true; break; }
                    if (exists) errores.add(new ErrorDML(3, 302, linea_col, "El nombre del atributo \"" + nombre_col + "\" se especifica más de una vez."));

                    Token t = ver(); String tipo_dato = null; int longitud = 0;
                    if (t != null && t.tipo == 1 && (t.lexema.equals("CHAR") || t.lexema.equals("NUMERIC"))) {
                        tipo_dato = consumir().lexema;
                        if (ver() != null && ver().lexema.equals("(")) {
                            consumir(); Token t_lon = esperar_tipo(6, 206, "Se esperaba tamaño numérico");
                            if (t_lon != null) longitud = Integer.parseInt(t_lon.lexema);
                            esperar_delimitador(")");
                        }
                    } else {
                        errores.add(new ErrorDML(2, 201, linea_actual(), "Se esperaba tipo de dato (CHAR o NUMERIC)"));
                        while (ver() != null && !((ver().tipo == 5 && (ver().lexema.equals(",") || ver().lexema.equals(")"))) || (ver().tipo == 1 && ver().lexema.equals("CONSTRAINT")))) consumir();
                        return;
                    }

                    int no_nulo = 0;
                    if (es_reservada("NOT")) { consumir(); esperar_reservada("NULL"); no_nulo = 1; }

                    int no_atr = ts_atributos.size() + columnas.size() + 1;
                    columnas.add(new Atributo(0, no_atr, nombre_col, tipo_dato, longitud, no_nulo));
                } else if (ver() != null) consumir();
            }

            void parsear_constraint(List<Atributo> columnas, List<Restriccion> restricciones, String nombre_tabla, int linea_tabla) {
                consumir(); // CONSTRAINT
                Token t_nombre = ver();
                if (t_nombre == null || t_nombre.tipo != 4) { esperar_tipo(4, 204, "Se esperaba nombre de constraint"); return; }
                consumir(); String nombre_res = t_nombre.lexema; int linea_res = t_nombre.linea;

                boolean exists = buscar_restriccion(nombre_res) != null;
                for (Restriccion r : restricciones) if (r.nombre.equals(nombre_res)) exists = true;
                if (exists) errores.add(new ErrorDML(3, 306, linea_res, "El nombre de la restricción \"" + nombre_res + "\" esta duplicado."));

                Token t = ver();
                if (t != null && t.tipo == 1 && t.lexema.equals("PRIMARY")) {
                    consumir(); esperar_reservada("KEY"); esperar_delimitador("(");
                    Token t_col = ver(); String nombre_col_pk = t_col != null ? t_col.lexema : "";
                    int linea_col_pk = t_col != null ? t_col.linea : linea_res;
                    esperar_tipo(4, 204, "Se esperaba columna en PRIMARY KEY");

                    int idx_col = -1;
                    for (int i = 0; i < columnas.size(); i++) if (columnas.get(i).nombre.equals(nombre_col_pk)) idx_col = i;
                    if (idx_col == -1) errores.add(new ErrorDML(3, 303, linea_col_pk, "El nombre del atributo \"" + nombre_col_pk + "\" no existe en la tabla \"" + nombre_tabla + "\"."));
                    
                    while (ver() != null && ver().lexema.equals(",")) {
                        consumir(); Token t_col2 = ver(); String nom2 = t_col2 != null ? t_col2.lexema : "";
                        int lin2 = t_col2 != null ? t_col2.linea : linea_res;
                        esperar_tipo(4, 204, "Se esperaba columna");
                        boolean c_exists = false;
                        for (Atributo a : columnas) if (a.nombre.equals(nom2)) c_exists = true;
                        if (!c_exists) errores.add(new ErrorDML(3, 303, lin2, "El nombre del atributo \"" + nom2 + "\" no existe en la tabla \"" + nombre_tabla + "\"."));
                    }
                    esperar_delimitador(")");
                    
                    int no_res = ts_restricciones.size() + restricciones.size() + 1;
                    String atr_no = idx_col != -1 ? String.valueOf(columnas.get(idx_col).no_atr) : "-";
                    restricciones.add(new Restriccion(0, no_res, 1, nombre_res, atr_no, "-", "-"));
                } else if (t != null && t.tipo == 1 && t.lexema.equals("FOREIGN")) {
                    consumir(); esperar_reservada("KEY"); esperar_delimitador("(");
                    Token t_col = ver(); String nombre_col_fk = t_col != null ? t_col.lexema : "";
                    int linea_col_fk = t_col != null ? t_col.linea : linea_res;
                    esperar_tipo(4, 204, "Se esperaba columna en FOREIGN KEY");
                    esperar_delimitador(")");
                    esperar_reservada("REFERENCES");
                    
                    Token t_ref_tabla = ver(); String nombre_tabla_ref = t_ref_tabla != null ? t_ref_tabla.lexema : "";
                    esperar_tipo(4, 204, "Se esperaba tabla referenciada");
                    esperar_delimitador("(");
                    
                    Token t_ref_col = ver(); String nombre_col_ref = t_ref_col != null ? t_ref_col.lexema : "";
                    esperar_tipo(4, 204, "Se esperaba columna referenciada");
                    esperar_delimitador(")");

                    int idx_col_fk = -1;
                    for (int i = 0; i < columnas.size(); i++) if (columnas.get(i).nombre.equals(nombre_col_fk)) idx_col_fk = i;
                    if (idx_col_fk == -1) errores.add(new ErrorDML(3, 305, linea_col_fk, "Se hace referencia al atributo \"" + nombre_col_fk + "\" no válido en la tabla \"" + nombre_tabla + "\"."));

                    Tabla tabla_ref_obj = buscar_tabla(nombre_tabla_ref);
                    String tabla_ref_no = "-";
                    if (tabla_ref_obj == null) errores.add(new ErrorDML(3, 304, t_ref_tabla != null ? t_ref_tabla.linea : linea_res, "La tabla referenciada \"" + nombre_tabla_ref + "\" no existe."));
                    else tabla_ref_no = String.valueOf(tabla_ref_obj.no);

                    Atributo atr_ref_obj = tabla_ref_obj != null ? buscar_atributo_en_tabla(tabla_ref_obj.no, nombre_col_ref) : null;
                    String atr_ref_no = "-";
                    if (tabla_ref_obj != null && atr_ref_obj == null) errores.add(new ErrorDML(3, 305, t_ref_col != null ? t_ref_col.linea : linea_res, "Se hace referencia al atributo \"" + nombre_col_ref + "\" no válido en la tabla \"" + nombre_tabla_ref + "\"."));
                    else if (atr_ref_obj != null) atr_ref_no = String.valueOf(atr_ref_obj.no_atr);

                    int no_res = ts_restricciones.size() + restricciones.size() + 1;
                    String atr_fk_no = idx_col_fk != -1 ? String.valueOf(columnas.get(idx_col_fk).no_atr) : "-";
                    restricciones.add(new Restriccion(0, no_res, 2, nombre_res, atr_fk_no, tabla_ref_no, atr_ref_no));
                } else errores.add(new ErrorDML(2, 201, linea_actual(), "Se esperaba PRIMARY o FOREIGN después de CONSTRAINT"));
            }

            void parsear_insert() {
                consumir(); esperar_reservada("INTO");
                Token t_tabla = ver();
                String nombre_tabla = (t_tabla != null && t_tabla.tipo == 4) ? t_tabla.lexema.toUpperCase() : null;
                int linea_insert = t_tabla != null ? t_tabla.linea : linea_actual();
                esperar_tipo(4, 204, "Se esperaba nombre de tabla");

                Tabla info_tabla = nombre_tabla != null ? buscar_tabla(nombre_tabla) : null;
                List<Atributo> cols_tabla = new ArrayList<>();
                if (info_tabla != null) for (Atributo a : ts_atributos) if (a.no_tabla == info_tabla.no) cols_tabla.add(a);

                if (ver() != null && ver().tipo == 5 && ver().lexema.equals("(")) {
                    consumir(); esperar_tipo(4, 204, "Se esperaba columna");
                    while (ver() != null && ver().lexema.equals(",")) { consumir(); esperar_tipo(4, 204, "Se esperaba columna"); }
                    esperar_delimitador(")");
                }

                esperar_reservada("VALUES"); esperar_delimitador("(");
                List<Token> valores = new ArrayList<>();
                Token t = ver();
                if (t != null && (t.tipo == 4 || t.tipo == 6)) { valores.add(t); consumir(); }
                else {
                    errores.add(new ErrorDML(2, 206, linea_actual(), "Se esperaba Constante"));
                    while (ver() != null && !(ver().tipo == 5 && ver().lexema.equals(")"))) consumir();
                    if (ver() != null) consumir();
                    return;
                }

                while (ver() != null && ver().tipo == 5 && ver().lexema.equals(",")) {
                    consumir(); t = ver();
                    if (t != null && (t.tipo == 4 || t.tipo == 6)) {
                        valores.add(t); consumir();
                        Token t2 = ver();
                        if (t2 != null && (t2.tipo == 4 || t2.tipo == 6)) {
                            errores.add(new ErrorDML(2, 201, t2.linea, "Se esperaba Palabra Reservada (coma entre valores)"));
                            while (ver() != null && !(ver().tipo == 5 && ver().lexema.equals(")"))) consumir();
                            if (ver() != null) consumir(); return;
                        } else if (t2 != null && t2.tipo == 5 && t2.codigo == 54) {
                            errores.add(new ErrorDML(2, 201, t2.linea, "Se esperaba Palabra Reservada (coma entre valores)"));
                            while (ver() != null && !(ver().tipo == 5 && ver().lexema.equals(")"))) consumir();
                            if (ver() != null) consumir(); return;
                        }
                    } else {
                        errores.add(new ErrorDML(2, 206, linea_actual(), "Se esperaba Constante"));
                        while (ver() != null && !(ver().tipo == 5 && ver().lexema.equals(")"))) consumir();
                        if (ver() != null) consumir(); return;
                    }
                }
                esperar_delimitador(")");

                if (!cols_tabla.isEmpty() && valores.size() != cols_tabla.size()) {
                    errores.add(new ErrorDML(3, 307, linea_insert, "Los valores especificados no corresponden a la definición de la tabla."));
                    return;
                }

                if (!cols_tabla.isEmpty()) {
                    for (int i = 0; i < valores.size(); i++) {
                        if (i >= cols_tabla.size()) break;
                        Atributo col = cols_tabla.get(i);
                        Token val = valores.get(i);
                        if (val.tipo == 6 && val.sub == 62) {
                            if (col.tipo.equals("CHAR") && val.lexema.length() > col.longitud) {
                                errores.add(new ErrorDML(3, 308, val.linea, "Los datos de cadena o binarios se truncarían."));
                            }
                        }
                    }
                }
            }

            void ejecutar() {
                try {
                    while (ver() != null) {
                        Token t = ver();
                        if (t.tipo == 5 && t.lexema.equals(";")) { consumir(); continue; }
                        int errores_antes = errores.size();
                        if (t.tipo == 1 && t.lexema.equals("SELECT")) parsear_select();
                        else if (t.tipo == 1 && t.lexema.equals("CREATE")) parsear_create_table();
                        else if (t.tipo == 1 && t.lexema.equals("INSERT")) parsear_insert();
                        else {
                            errores.add(new ErrorDML(2, 201, t.linea, "Se esperaba SELECT, CREATE o INSERT al inicio"));
                            consumir(); continue;
                        }
                        if (errores.size() > errores_antes) {
                            while (ver() != null && !(ver().tipo == 5 && ver().lexema.equals(";"))) consumir();
                        }
                    }
                } catch (Exception e) {
                    errores.add(new ErrorDML(0, 0, linea_actual(), "Error lógico atrapado para evitar cierre: " + e.getMessage()));
                }
            }
        }
        new Parser().ejecutar();
        return errores;
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) {}
        SwingUtilities.invokeLater(() -> {
            new Scanner5().setVisible(true);
        });
    }
}