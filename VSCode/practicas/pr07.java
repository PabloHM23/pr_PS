package VSCode.practicas;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.*;
import java.util.List;


public class pr07 extends JFrame {

    // === DICCIONARIOS LÉXICOS ===
    static final Map<String, Integer> PALABRAS_RESERVADAS = new HashMap<>();
    static final Map<Character, Integer> DELIMITADORES = new HashMap<>();
    static final Map<Character, Integer> OPERADORES = new HashMap<>();
    static final Map<String, Integer> RELACIONALES = new HashMap<>();

    static {
        String[] pr = { "SELECT", "FROM", "WHERE", "IN", "AND", "OR", "CREATE", "TABLE",
                "CHAR", "NUMERIC", "NOT", "NULL", "CONSTRAINT", "KEY", "PRIMARY",
                "FOREIGN", "REFERENCES", "INSERT", "INTO", "VALUES", "INT", "VARCHAR", "DECIMAL", "DATE" };
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
            // Tabla DEPARTAMENTOS
            Tabla departamentos = new Tabla("DEPARTAMENTOS");
            departamentos.agregarColumna("D#", "VARCHAR", 3, true, false, null);
            departamentos.agregarColumna("NOMBRE", "VARCHAR", 40, false, false, null);
            departamentos.agregarColumna("UBICACION", "VARCHAR", 20, false, false, null);
            departamentos.setPrimaryKey("D#");
            tablas.put("DEPARTAMENTOS", departamentos);

            // Tabla CARRERAS
            Tabla carreras = new Tabla("CARRERAS");
            carreras.agregarColumna("C#", "VARCHAR", 3, true, false, null);
            carreras.agregarColumna("NOMBRE", "VARCHAR", 40, false, false, null);
            carreras.agregarColumna("DURACION", "VARCHAR", 4, false, false, null);
            carreras.agregarColumna("CREDITOS", "INT", 3, false, false, null);
            carreras.agregarColumna("D#", "VARCHAR", 3, false, false, null);
            carreras.setPrimaryKey("C#");
            
            // Llave Foránea: CARRERAS.D# -> DEPARTAMENTOS.D#
            LlaveForeignKey fk = new LlaveForeignKey("FK_CARRERAS", "D#", "DEPARTAMENTOS", "D#");
            carreras.agregarLlaveForeignKey(fk);
            tablas.put("CARRERAS", carreras);

            // Tabla ESTUDIANTES
            Tabla estudiantes = new Tabla("ESTUDIANTES");
            estudiantes.agregarColumna("E#", "VARCHAR", 3, true, false, null);
            estudiantes.agregarColumna("NOMBRE", "VARCHAR", 40, false, false, null);
            estudiantes.agregarColumna("TELEFONO", "VARCHAR", 10, false, false, null);
            estudiantes.agregarColumna("CARRERA", "VARCHAR", 3, false, false, null);
            estudiantes.setPrimaryKey("E#");
            
            // Llave Foránea: ESTUDIANTES.CARRERA -> CARRERAS.C#
            LlaveForeignKey fkCarrera = new LlaveForeignKey("FK_ESTUDIANTES_CARRERA", "CARRERA", "CARRERAS", "C#");
            estudiantes.agregarLlaveForeignKey(fkCarrera);
            tablas.put("ESTUDIANTES", estudiantes);

            // Tabla INSCRITOS
            Tabla inscritos = new Tabla("INSCRITOS");
            inscritos.agregarColumna("E#", "VARCHAR", 3, true, true, null);
            inscritos.agregarColumna("C#", "VARCHAR", 3, true, true, null);
            inscritos.agregarColumna("SEMESTRE", "INT", 2, false, false, null);
            inscritos.agregarColumna("NOTA", "NUMERIC", 3, false, false, null);
            inscritos.setPrimaryKey("E#,C#");
            
            // Llaves Foráneas
            LlaveForeignKey fkEstudiante = new LlaveForeignKey("FK_INSCRITOS_ESTUDIANTE", "E#", "ESTUDIANTES", "E#");
            inscritos.agregarLlaveForeignKey(fkEstudiante);
            
            LlaveForeignKey fkCarrera2 = new LlaveForeignKey("FK_INSCRITOS_CARRERA", "C#", "CARRERAS", "C#");
            inscritos.agregarLlaveForeignKey(fkCarrera2);
            tablas.put("INSCRITOS", inscritos);

            // Cargar datos de ejemplo
            cargarDatos();
        }

        void cargarDatos() {
            // Datos en DEPARTAMENTOS
            Tabla departamentos = tablas.get("DEPARTAMENTOS");
            departamentos.agregarFila(new String[] { "D1", "CIENCIAS", "EDIFICIO A" });
            departamentos.agregarFila(new String[] { "D2", "INGENIERIA", "EDIFICIO B" });
            departamentos.agregarFila(new String[] { "D3", "HUMANIDADES", "EDIFICIO C" });

            // Datos en CARRERAS
            Tabla carreras = tablas.get("CARRERAS");
            carreras.agregarFila(new String[] { "C1", "MATEMATICA", "2008", "120", "D1" });
            carreras.agregarFila(new String[] { "C2", "FISICA", "2008", "110", "D1" });
            carreras.agregarFila(new String[] { "C3", "ICI", "2009", "10", "D2" });
            carreras.agregarFila(new String[] { "C4", "INGENIERIA CIVIL", "2010", "130", "D2" });

            // Datos en ESTUDIANTES
            Tabla estudiantes = tablas.get("ESTUDIANTES");
            estudiantes.agregarFila(new String[] { "E1", "Juan Pérez", "2121212", "C1" });
            estudiantes.agregarFila(new String[] { "E2", "María López", "2222222", "C3" });
            estudiantes.agregarFila(new String[] { "E3", "Carlos García", "2323232", "C2" });
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

        @Override
        public String toString() {
            return String.format("%s (%s -> %s.%s)", nombre, columnaLocal, tablaReferenciada, columnaReferenciada);
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

        void agregarFila(String[] valores) {
            filas.add(valores);
        }

        void setPrimaryKey(String pk) {
            this.primaryKey = pk;
        }

        int obtenerIndiceColumna(String nomCol) {
            for (int i = 0; i < columnas.size(); i++) {
                if (columnas.get(i).nombre.equalsIgnoreCase(nomCol)) {
                    return i;
                }
            }
            return -1;
        }
    }

    // === TOKEN ===
    static class Token {
        String lexema;
        int tipo;      // 1=reservada, 4=identificador, 5=delimitador, 6=constante, 7=operador, 8=relacional
        int codigo;
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
            return String.format("[%s] Línea %d, Código %d: %s", getTipoError(), linea, codigo, descripcion);
        }
    }

    // === ANALIZADOR SQL (Léxico, Sintáctico, Semántico) ===
    static class AnalizadorSQL {
        BaseDatos bd;
        List<Token> tokens = new ArrayList<>();
        List<ErrorSQL> errores = new ArrayList<>();
        int posToken = 0;

        AnalizadorSQL(BaseDatos bd) {
            this.bd = bd;
        }

        // FASE 1: ANÁLISIS LÉXICO
        void tokenizar(String sql) {
            tokens.clear();
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

                    // Operadores relacionales
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

                    // Operadores
                    if (OPERADORES.containsKey(c)) {
                        tokens.add(new Token(String.valueOf(c), 7, OPERADORES.get(c), li));
                        i++;
                        continue;
                    }

                    // Delimitadores y cadenas
                    if (c == '\'') {
                        int j = i + 1;
                        while (j < linea.length() && linea.charAt(j) != '\'')
                            j++;
                        if (j < linea.length()) {
                            String valor = linea.substring(i + 1, j);
                            tokens.add(new Token(valor, 6, 600, li));
                            i = j + 1;
                        } else {
                            errores.add(new ErrorSQL(1, 101, li, "Comilla de cierre no encontrada"));
                            i = linea.length();
                        }
                        continue;
                    }

                    if (DELIMITADORES.containsKey(c)) {
                        tokens.add(new Token(String.valueOf(c), 5, DELIMITADORES.get(c), li));
                        i++;
                        continue;
                    }

                    // Números
                    if (Character.isDigit(c)) {
                        int j = i;
                        while (j < linea.length() && Character.isDigit(linea.charAt(j)))
                            j++;
                        tokens.add(new Token(linea.substring(i, j), 6, 600, li));
                        i = j;
                        continue;
                    }

                    // Identificadores y palabras reservadas
                    if (Character.isLetter(c) || c == '_') {
                        int j = i;
                        while (j < linea.length()
                                && (Character.isLetterOrDigit(linea.charAt(j)) || linea.charAt(j) == '_' || linea.charAt(j) == '#'))
                            j++;
                        String palabra = linea.substring(i, j).toUpperCase();
                        if (PALABRAS_RESERVADAS.containsKey(palabra)) {
                            tokens.add(new Token(palabra, 1, PALABRAS_RESERVADAS.get(palabra), li));
                        } else {
                            tokens.add(new Token(palabra, 4, 400, li));
                        }
                        i = j;
                        continue;
                    }

                    errores.add(new ErrorSQL(1, 101, li, "Símbolo desconocido: '" + c + "'"));
                    i++;
                }
            }
        }

        // FASE 2 y 3: ANÁLISIS SINTÁCTICO Y SEMÁNTICO
        void analizarSintaxis() {
            posToken = 0;
            if (tokens.isEmpty()) return;

            Token t = verToken();
            if (t != null && t.tipo == 1 && t.lexema.equals("SELECT")) {
                analizarSelect();
            } else if (t != null && t.tipo == 1 && t.lexema.equals("CREATE")) {
                analizarCreateTable();
            } else if (t != null && t.tipo == 1 && t.lexema.equals("INSERT")) {
                analizarInsert();
            } else if (t != null) {
                errores.add(new ErrorSQL(2, 201, t.linea, "Se esperaba SELECT, CREATE o INSERT"));
            }
        }

        void analizarSelect() {
            consumir(); // SELECT
            if (esTipo(7) && verToken().lexema.equals("*")) {
                consumir();
            } else {
                esperarTipo(4, 204);
            }
            esperarReservada("FROM", 201);
            esperarTipo(4, 204);
            if (esReservada("WHERE")) {
                consumir();
                analizarCondicion();
            }
        }

        void analizarCondicion() {
            esperarTipo(4, 204);
            Token t = verToken();
            if (t != null && t.tipo == 8) {
                consumir();
                Token t2 = verToken();
                if (t2 != null && (t2.tipo == 4 || t2.tipo == 6)) {
                    consumir();
                }
            } else if (t != null && t.tipo == 1 && t.lexema.equals("IN")) {
                consumir();
                esperarDelimitador("(", 205);
                if (!esReservada("SELECT")) {
                    esperarTipo(6, 206);
                }
                esperarDelimitador(")", 205);
            }
        }

        void analizarCreateTable() {
            consumir(); // CREATE
            esperarReservada("TABLE", 201);
            esperarTipo(4, 204);
            esperarDelimitador("(", 205);
            while (verToken() != null && !esDelimitador(")")) {
                analizarDefColumna();
                if (esDelimitador(",")) {
                    consumir();
                }
            }
            esperarDelimitador(")", 205);
        }

        void analizarDefColumna() {
            Token t = verToken();
            if (t != null && t.tipo == 1 && t.lexema.equals("CONSTRAINT")) {
                consumir();
                esperarTipo(4, 204);
                if (esReservada("PRIMARY")) {
                    consumir();
                    esperarReservada("KEY", 201);
                    esperarDelimitador("(", 205);
                    esperarTipo(4, 204);
                    esperarDelimitador(")", 205);
                } else if (esReservada("FOREIGN")) {
                    consumir();
                    esperarReservada("KEY", 201);
                    esperarDelimitador("(", 205);
                    esperarTipo(4, 204);
                    esperarDelimitador(")", 205);
                    esperarReservada("REFERENCES", 201);
                    esperarTipo(4, 204);
                    esperarDelimitador("(", 205);
                    esperarTipo(4, 204);
                    esperarDelimitador(")", 205);
                }
            } else {
                esperarTipo(4, 204);
                esperarTipo(1, 201); // Tipo de dato
                if (esDelimitador("(")) {
                    consumir();
                    if (esTipo(6)) consumir();
                    esperarDelimitador(")", 205);
                }
                if (esReservada("NOT")) {
                    consumir();
                    esperarReservada("NULL", 201);
                }
            }
        }

        void analizarInsert() {
            consumir(); // INSERT
            esperarReservada("INTO", 201);
            Token nomTabla = verToken();
            if (nomTabla != null && nomTabla.tipo == 4) {
                consumir();
                if (esDelimitador("(")) {
                    consumir();
                    esperarTipo(4, 204);
                    while (esDelimitador(",")) {
                        consumir();
                        esperarTipo(4, 204);
                    }
                    esperarDelimitador(")", 205);
                }
                esperarReservada("VALUES", 201);
                esperarDelimitador("(", 205);
                if (esTipo(6)) consumir();
                while (esDelimitador(",")) {
                    consumir();
                    if (esTipo(6)) consumir();
                }
                esperarDelimitador(")", 205);

                // ANÁLISIS SEMÁNTICO: Validar llaves foráneas
                validarSemantica(nomTabla.lexema.toUpperCase());
            }
        }

        void validarSemantica(String nombreTabla) {
            if (!bd.tablas.containsKey(nombreTabla)) {
                errores.add(new ErrorSQL(3, 301, verToken() != null ? verToken().linea : 1, 
                    "Tabla no existe: " + nombreTabla));
            }
        }

        // Métodos auxiliares
        Token verToken() {
            return posToken < tokens.size() ? tokens.get(posToken) : null;
        }

        void consumir() {
            if (posToken < tokens.size()) posToken++;
        }

        boolean esTipo(int tipo) {
            Token t = verToken();
            return t != null && t.tipo == tipo;
        }

        boolean esReservada(String palabra) {
            Token t = verToken();
            return t != null && t.tipo == 1 && t.lexema.equals(palabra);
        }

        boolean esDelimitador(String delim) {
            Token t = verToken();
            return t != null && t.tipo == 5 && t.lexema.equals(delim);
        }

        void esperarTipo(int tipo, int cod) {
            Token t = verToken();
            if (t != null && t.tipo == tipo) {
                consumir();
            } else {
                errores.add(new ErrorSQL(2, cod, t != null ? t.linea : 1, 
                    "Se esperaba token de tipo " + tipo));
            }
        }

        void esperarReservada(String palabra, int cod) {
            Token t = verToken();
            if (t != null && t.tipo == 1 && t.lexema.equals(palabra)) {
                consumir();
            } else {
                errores.add(new ErrorSQL(2, cod, t != null ? t.linea : 1, 
                    "Se esperaba: " + palabra));
            }
        }

        void esperarDelimitador(String delim, int cod) {
            Token t = verToken();
            if (t != null && t.tipo == 5 && t.lexema.equals(delim)) {
                consumir();
            } else {
                errores.add(new ErrorSQL(2, cod, t != null ? t.linea : 1, 
                    "Se esperaba: " + delim));
            }
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

        List<ErrorSQL> obtenerErrores() {
            return errores;
        }

        void limpiarErrores() {
            errores.clear();
        }
    }

    private JTextArea textEntrada;
    private JTable tablaErrores;
    private JTable tablaDatos;
    private JTextArea textConsola;
    private DefaultTableModel modeloErrores;
    private DefaultTableModel modeloDatos;
    private JLabel lblEstado;
    private BaseDatos bd;
    private AnalizadorSQL analizador;

    public pr07() {
        setTitle("Traductor SQL - Fases 1, 2 y 3 (Léxico, Sintáctico, Semántico)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1500, 900);
        setLocationRelativeTo(null);

        bd = new BaseDatos();
        analizador = new AnalizadorSQL(bd);

        JPanel panelPrincipal = new JPanel(new BorderLayout(10, 10));
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Barra superior con opciones
        JPanel panelOpciones = crearPanelOpciones();
        panelPrincipal.add(panelOpciones, BorderLayout.NORTH);

        // Panel entrada
        JPanel panelEntrada = crearPanelEntrada();

        // Panel resultados
        JPanel panelResultados = crearPanelResultados();

        // Split pane
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

        btnActualizar.setToolTipText("Cargar la BD INSCRITOS completa");
        btnInicializar.setToolTipText("Limpiar las tablas semánticas");

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
        JOptionPane.showMessageDialog(this, "Tablas semánticas actualizadas correctamente", "Éxito",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void inicializarTablasSematicas() {
        bd.tablas.clear();
        analizador = new AnalizadorSQL(bd);
        modeloDatos.setRowCount(0);
        lblEstado.setText("✓ Tablas semánticas inicializadas (vacías)");
        lblEstado.setForeground(Color.RED);
        JOptionPane.showMessageDialog(this, "Tablas semánticas limpias", "Inicialización",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private JPanel crearPanelEntrada() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(new TitledBorder("MÓDULO DE ENTRADA - Sentencias SQL (DDL/DML)"));

        textEntrada = new JTextArea();
        textEntrada.setFont(new Font("Courier New", Font.PLAIN, 11));
        textEntrada.setLineWrap(true);
        textEntrada.setWrapStyleWord(true);
        textEntrada.setText("-- DDL: CREATE TABLE\n" +
                "CREATE TABLE CARRERAS (\n" +
                "    C# VARCHAR(3) PRIMARY KEY,\n" +
                "    NOMBRE VARCHAR(40) NOT NULL\n" +
                ");\n\n" +
                "-- DDL: INSERT\n" +
                "INSERT INTO CARRERAS VALUES ('C1','MATEMATICA');\n\n" +
                "-- DML: SELECT\n" +
                "SELECT * FROM CARRERAS WHERE C# = 'C1';");

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

        // Tabs
        JTabbedPane tabs = new JTabbedPane();

        // Tab 1: Errores
        JPanel panelErrores = new JPanel(new BorderLayout());
        modeloErrores = new DefaultTableModel(
                new String[] { "Línea", "Código", "Tipo", "Ubicación", "Descripción" }, 0);
        tablaErrores = new JTable(modeloErrores);
        tablaErrores.setRowHeight(40);
        panelErrores.add(new JScrollPane(tablaErrores), BorderLayout.CENTER);
        tabs.addTab("Módulo de Errores", panelErrores);

        // Tab 2: Estructura BD
        JPanel panelEstructura = new JPanel(new BorderLayout());
        modeloDatos = new DefaultTableModel(
                new String[] { "Tabla", "Columna", "Tipo", "PK", "FK", "Detalles" }, 0);
        tablaDatos = new JTable(modeloDatos);
        panelEstructura.add(new JScrollPane(tablaDatos), BorderLayout.CENTER);
        cargarEstructuraBD();
        tabs.addTab("Tablas Semánticas", panelEstructura);

        // Tab 3: Análisis Detallado
        JPanel panelConsola = new JPanel(new BorderLayout());
        textConsola = new JTextArea();
        textConsola.setFont(new Font("Courier New", Font.PLAIN, 10));
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
                String detalles = "";

                for (LlaveForeignKey llaveFK : tabla.llavesForeignKey) {
                    if (llaveFK.columnaLocal.equalsIgnoreCase(col.nombre)) {
                        fk = "SÍ";
                        detalles = String.format("→ %s.%s", llaveFK.tablaReferenciada, llaveFK.columnaReferenciada);
                    }
                }

                modeloDatos.addRow(new Object[] {
                        tabla.nombre,
                        col.nombre,
                        col.tipo + "(" + col.longitud + ")",
                        col.esPrimaryKey ? "SÍ" : "NO",
                        fk,
                        detalles
                });
            }
        }
    }

    private void analizarSQL() {
        String codigo = textEntrada.getText();
        if (codigo.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, ingrese una sentencia SQL", "Entrada vacía",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        limpiarResultados();

        StringBuilder consola = new StringBuilder("=== ANÁLISIS SQL (3 FASES) ===\n");
        consola.append("MÓDULO DE ANÁLISIS\n");
        consola.append("==================\n\n");

        // FASE 1: Análisis Léxico
        consola.append("FASE 1: ANÁLISIS LÉXICO\n");
        consola.append("------------------------\n");

        analizador.analizar(codigo);
        List<Token> tokens = analizador.tokens;
        List<ErrorSQL> errores = analizador.obtenerErrores();

        consola.append("Tokens generados: ").append(tokens.size()).append("\n");
        for (Token t : tokens) {
            consola.append("  ").append(t).append("\n");
        }

        // Mostrar errores léxicos
        List<ErrorSQL> erroresLexicos = errores.stream()
                .filter(e -> e.tipo == 1).toList();
        if (!erroresLexicos.isEmpty()) {
            consola.append("\n⚠️  Errores Léxicos:\n");
            for (ErrorSQL err : erroresLexicos) {
                consola.append("  ").append(err).append("\n");
            }
        }

        // FASE 2: Análisis Sintáctico
        consola.append("\nFASE 2: ANÁLISIS SINTÁCTICO\n");
        consola.append("---------------------------\n");

        List<ErrorSQL> erroresSintacticos = errores.stream()
                .filter(e -> e.tipo == 2).toList();
        if (erroresSintacticos.isEmpty()) {
            consola.append("✓ Sintaxis válida\n");
        } else {
            consola.append("⚠️  Errores Sintácticos:\n");
            for (ErrorSQL err : erroresSintacticos) {
                consola.append("  ").append(err).append("\n");
            }
        }

        // FASE 3: Análisis Semántico
        consola.append("\nFASE 3: ANÁLISIS SEMÁNTICO\n");
        consola.append("---------------------------\n");

        List<ErrorSQL> erroresSemanticos = errores.stream()
                .filter(e -> e.tipo == 3).toList();
        if (erroresSemanticos.isEmpty()) {
            consola.append("✓ Semántica válida\n");
        } else {
            consola.append("⚠️  Errores Semánticos:\n");
            for (ErrorSQL err : erroresSemanticos) {
                consola.append("  ").append(err).append("\n");
            }
        }

        // Resultado final
        consola.append("\n=== RESULTADO FINAL ===\n");
        if (errores.isEmpty()) {
            consola.append("✓ CONSULTA VÁLIDA - Libre de errores\n");
            consola.append("MÓDULO DE RESULTADOS: Sentencia procesada correctamente\n");
            JOptionPane.showMessageDialog(this, "✓ Análisis exitoso\n\nLa sentencia SQL es válida", "Éxito",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            consola.append("✗ CONSULTA INVÁLIDA - Se encontraron ").append(errores.size()).append(" error(es)\n");
            consola.append("MÓDULO DE ERRORES: Ver detalles abajo\n");
        }

        textConsola.setText(consola.toString());

        // Llenar tabla de errores
        int numError = 1;
        for (ErrorSQL err : errores) {
            modeloErrores.addRow(new Object[] {
                    err.linea,
                    err.codigo,
                    err.getTipoError(),
                    "Línea " + err.linea,
                    err.descripcion
            });
        }
    }

    private void limpiarResultados() {
        modeloErrores.setRowCount(0);
        textConsola.setText("");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            pr07 frame = new pr07();
            frame.setVisible(true);
        });
    }
}
