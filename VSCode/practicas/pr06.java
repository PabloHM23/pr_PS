package VSCode.practicas;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class pr06 extends JFrame {

    // === DICCIONARIOS LÉXICOS ===
    static final Map<String, Integer> PALABRAS_RESERVADAS = new HashMap<>();
    static final Map<Character, Integer> DELIMITADORES = new HashMap<>();
    static final Map<String, Integer> TIPOS_DATOS = new HashMap<>();

    static {
        // Palabras reservadas SQL
        String[] pr = { "CREATE", "TABLE", "PRIMARY", "KEY", "FOREIGN", "REFERENCES",
                "NOT", "NULL", "UNIQUE", "CHECK", "DEFAULT", "AUTO_INCREMENT",
                "CHAR", "VARCHAR", "INT", "INTEGER", "NUMERIC", "DECIMAL", "DATE", "DATETIME" };
        int prCode = 10;
        for (String p : pr)
            PALABRAS_RESERVADAS.put(p, prCode++);

        // Delimitadores
        DELIMITADORES.put(',', 50);
        DELIMITADORES.put('.', 51);
        DELIMITADORES.put('(', 52);
        DELIMITADORES.put(')', 53);
        DELIMITADORES.put(';', 54);

        // Tipos de datos
        TIPOS_DATOS.put("INT", 1);
        TIPOS_DATOS.put("INTEGER", 1);
        TIPOS_DATOS.put("VARCHAR", 2);
        TIPOS_DATOS.put("CHAR", 2);
        TIPOS_DATOS.put("NUMERIC", 3);
        TIPOS_DATOS.put("DECIMAL", 3);
        TIPOS_DATOS.put("DATE", 4);
        TIPOS_DATOS.put("DATETIME", 4);
    }

    // === ESTRUCTURAS DE DATOS ===
    static class Token {
        String lexema;
        int tipo;
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

    static class Atributo {
        String nombre;
        String tipo;
        String longitud;
        boolean notNull;
        boolean primaryKey;
        String valorDefault;

        Atributo(String nombre, String tipo) {
            this.nombre = nombre;
            this.tipo = tipo;
            this.longitud = "";
            this.notNull = false;
            this.primaryKey = false;
            this.valorDefault = "";
        }

        @Override
        public String toString() {
            return String.format("%s (%s%s)%s%s%s", 
                nombre, tipo, 
                longitud.isEmpty() ? "" : ", " + longitud,
                primaryKey ? " [PK]" : "",
                notNull ? " [NN]" : "",
                valorDefault.isEmpty() ? "" : " DEF: " + valorDefault);
        }
    }

    static class Tabla {
        String nombre;
        List<Atributo> atributos = new ArrayList<>();
        List<String> restricciones = new ArrayList<>();

        Tabla(String nombre) {
            this.nombre = nombre;
        }

        void agregarAtributo(Atributo a) {
            atributos.add(a);
        }

        void agregarRestriccion(String r) {
            restricciones.add(r);
        }
    }

    static class ErrorSQL {
        int tipo;
        int linea;
        String descripcion;

        ErrorSQL(int tipo, int linea, String descripcion) {
            this.tipo = tipo;
            this.linea = linea;
            this.descripcion = descripcion;
        }

        @Override
        public String toString() {
            String tipoStr = tipo == 1 ? "LÉXICO" : tipo == 2 ? "SINTÁCTICO" : "SEMÁNTICO";
            return String.format("[%s] Línea %d: %s", tipoStr, linea, descripcion);
        }
    }

    // === MÓDULO DE ANÁLISIS ===
    static class AnalizadorDDL {
        String codigo;
        List<Token> tokens = new ArrayList<>();
        List<ErrorSQL> errores = new ArrayList<>();
        List<Tabla> tablas = new ArrayList<>();
        int posToken = 0;

        AnalizadorDDL(String codigo) {
            this.codigo = codigo;
        }

        // Análisis léxico
        void tokenizar() {
            tokens.clear();
            errores.clear();
            String[] lineas = codigo.split("\n");

            for (int li = 1; li <= lineas.length; li++) {
                String linea = lineas[li - 1].trim();
                if (linea.isEmpty() || linea.startsWith("--"))
                    continue;

                int i = 0;
                while (i < linea.length()) {
                    char c = linea.charAt(i);

                    if (Character.isWhitespace(c)) {
                        i++;
                        continue;
                    }

                    if (DELIMITADORES.containsKey(c)) {
                        tokens.add(new Token(String.valueOf(c), 5, DELIMITADORES.get(c), li));
                        i++;
                        continue;
                    }

                    if (Character.isLetter(c) || c == '_') {
                        int j = i;
                        while (j < linea.length()
                                && (Character.isLetterOrDigit(linea.charAt(j)) || linea.charAt(j) == '_'))
                            j++;
                        String palabra = linea.substring(i, j).toUpperCase();
                        if (PALABRAS_RESERVADAS.containsKey(palabra)) {
                            tokens.add(new Token(palabra, 1, PALABRAS_RESERVADAS.get(palabra), li));
                        } else {
                            tokens.add(new Token(palabra, 4, 401, li)); // Identificador
                        }
                        i = j;
                        continue;
                    }

                    if (Character.isDigit(c)) {
                        int j = i;
                        while (j < linea.length() && Character.isDigit(linea.charAt(j)))
                            j++;
                        tokens.add(new Token(linea.substring(i, j), 6, 600, li)); // Número
                        i = j;
                        continue;
                    }

                    errores.add(new ErrorSQL(1, li, "Símbolo desconocido: '" + c + "'"));
                    i++;
                }
            }
        }

        // Análisis sintáctico
        void analizarEstructura() {
            posToken = 0;
            tablas.clear();

            while (posToken < tokens.size()) {
                Token t = verToken();
                if (t != null && t.tipo == 1 && t.lexema.equals("CREATE")) {
                    analizarCreateTable();
                } else {
                    avanzar();
                }
            }
        }

        void analizarCreateTable() {
            if (!verificarPalabra("CREATE")) {
                errores.add(new ErrorSQL(2, verToken().linea, "Se esperaba CREATE"));
                return;
            }
            avanzar();

            if (!verificarPalabra("TABLE")) {
                errores.add(new ErrorSQL(2, verToken().linea, "Se esperaba TABLE"));
                return;
            }
            avanzar();

            Token nomTabla = verToken();
            if (nomTabla == null || nomTabla.tipo != 4) {
                errores.add(new ErrorSQL(2, posToken > 0 ? tokens.get(posToken - 1).linea : 1,
                        "Se esperaba nombre de tabla"));
                return;
            }

            Tabla tabla = new Tabla(nomTabla.lexema);
            avanzar();

            if (!verificarDelimitador("(")) {
                errores.add(new ErrorSQL(2, verToken().linea, "Se esperaba '('"));
                return;
            }
            avanzar();

            analizarDefinicionTabla(tabla);

            tablas.add(tabla);
        }

        void analizarDefinicionTabla(Tabla tabla) {
            while (posToken < tokens.size() && !verificarDelimitador(")")) {
                Token t = verToken();

                if (t.tipo == 1 && t.lexema.equals("PRIMARY")) {
                    avanzar();
                    if (verificarPalabra("KEY")) {
                        avanzar();
                        if (verificarDelimitador("(")) {
                            avanzar();
                            Token nomCol = verToken();
                            if (nomCol != null && nomCol.tipo == 4) {
                                tabla.restricciones.add("PRIMARY KEY (" + nomCol.lexema + ")");
                                for (Atributo a : tabla.atributos) {
                                    if (a.nombre.equals(nomCol.lexema)) {
                                        a.primaryKey = true;
                                    }
                                }
                                avanzar();
                            }
                            if (verificarDelimitador(")"))
                                avanzar();
                        }
                    }
                } else if (t.tipo == 1 && t.lexema.equals("FOREIGN")) {
                    avanzar();
                    if (verificarPalabra("KEY")) {
                        avanzar();
                        // Ignorar detalles de foreign key por simplicidad
                        while (posToken < tokens.size() && !verificarDelimitador(",")
                                && !verificarDelimitador(")")) {
                            avanzar();
                        }
                    }
                } else if (t.tipo == 1 && t.lexema.equals("CONSTRAINT")) {
                    // Saltar constraint
                    while (posToken < tokens.size() && !verificarDelimitador(",")
                            && !verificarDelimitador(")")) {
                        avanzar();
                    }
                } else if (t.tipo == 4) {
                    analizarAtributo(tabla);
                }

                if (verificarDelimitador(",")) {
                    avanzar();
                } else if (!verificarDelimitador(")")) {
                    avanzar();
                }
            }

            if (verificarDelimitador(")"))
                avanzar();
        }

        void analizarAtributo(Tabla tabla) {
            Token nomCol = verToken();
            if (nomCol == null || nomCol.tipo != 4) {
                errores.add(new ErrorSQL(2, posToken > 0 ? tokens.get(posToken - 1).linea : 1,
                        "Se esperaba nombre de columna"));
                return;
            }

            Atributo attr = new Atributo(nomCol.lexema, "");
            avanzar();

            // Obtener tipo de dato
            Token tipoDato = verToken();
            if (tipoDato != null && tipoDato.tipo == 1) {
                attr.tipo = tipoDato.lexema;
                avanzar();

                // Verificar longitud
                if (verificarDelimitador("(")) {
                    avanzar();
                    Token longitud = verToken();
                    if (longitud != null && longitud.tipo == 6) {
                        attr.longitud = longitud.lexema;
                        avanzar();
                    }
                    if (verificarDelimitador(")"))
                        avanzar();
                }
            }

            // Analizar modificadores
            while (posToken < tokens.size() && verToken() != null && verToken().tipo == 1) {
                Token mod = verToken();
                if (mod.lexema.equals("NOT")) {
                    avanzar();
                    if (verificarPalabra("NULL")) {
                        attr.notNull = true;
                        avanzar();
                    }
                } else if (mod.lexema.equals("PRIMARY")) {
                    avanzar();
                    if (verificarPalabra("KEY")) {
                        attr.primaryKey = true;
                        avanzar();
                    }
                } else if (mod.lexema.equals("DEFAULT")) {
                    avanzar();
                    Token valorDef = verToken();
                    if (valorDef != null) {
                        attr.valorDefault = valorDef.lexema;
                        avanzar();
                    }
                } else {
                    break;
                }
            }

            tabla.agregarAtributo(attr);
        }

        Token verToken() {
            return posToken < tokens.size() ? tokens.get(posToken) : null;
        }

        void avanzar() {
            if (posToken < tokens.size())
                posToken++;
        }

        boolean verificarPalabra(String palabra) {
            Token t = verToken();
            return t != null && t.tipo == 1 && t.lexema.equals(palabra);
        }

        boolean verificarDelimitador(String delim) {
            Token t = verToken();
            return t != null && t.tipo == 5 && t.lexema.equals(delim);
        }

        void analizar() {
            tokenizar();
            analizarEstructura();
        }
    }

    // === MÓDULO DE ENTRADA y RESULTADOS (GUI) ===
    private JTextArea textEntrada;
    private JTable tablaSematica;
    private JTable tablaAtributos;
    private JTable tablaRestricciones;
    private JTextArea textErrores;
    private DefaultTableModel modeloSematica;
    private DefaultTableModel modeloAtributos;
    private DefaultTableModel modeloRestricciones;

    public pr06() {
        setTitle("Analizador DDL - Generador de Tabla Semántica");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);

        JPanel panelPrincipal = new JPanel(new BorderLayout(10, 10));
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // --- MÓDULO DE ENTRADA ---
        JPanel panelEntrada = crearPanelEntrada();

        // --- MÓDULO DE RESULTADOS ---
        JPanel panelResultados = crearPanelResultados();

        // Split pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panelEntrada, panelResultados);
        splitPane.setDividerLocation(400);
        panelPrincipal.add(splitPane, BorderLayout.CENTER);

        add(panelPrincipal);
    }

    private JPanel crearPanelEntrada() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(new TitledBorder("MÓDULO DE ENTRADA - Código DDL"));

        textEntrada = new JTextArea();
        textEntrada.setFont(new Font("Courier New", Font.PLAIN, 11));
        textEntrada.setLineWrap(true);
        textEntrada.setWrapStyleWord(true);
        textEntrada.setText("CREATE TABLE usuarios (\n" +
                "    id INT PRIMARY KEY,\n" +
                "    nombre VARCHAR(100) NOT NULL,\n" +
                "    email VARCHAR(100) UNIQUE,\n" +
                "    edad INT,\n" +
                "    fecha_registro DATE DEFAULT CURRENT_DATE\n" +
                ");");

        JScrollPane scrollEntrada = new JScrollPane(textEntrada);
        panel.add(scrollEntrada, BorderLayout.CENTER);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        JButton btnAnalizar = new JButton("Analizar DDL");
        JButton btnLimpiar = new JButton("Limpiar");

        btnAnalizar.addActionListener(e -> analizarDDL());
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

        // Tabs para resultados
        JTabbedPane tabs = new JTabbedPane();

        // Tab 1: Tabla Semántica de Tablas
        JPanel panelTablas = new JPanel(new BorderLayout());
        modeloSematica = new DefaultTableModel(
                new String[] { "Tabla", "# Atributos", "# Restricciones" }, 0);
        tablaSematica = new JTable(modeloSematica);
        panelTablas.add(new JScrollPane(tablaSematica), BorderLayout.CENTER);
        tabs.addTab("Tablas Definidas", panelTablas);

        // Tab 2: Atributos
        JPanel panelAtributos = new JPanel(new BorderLayout());
        modeloAtributos = new DefaultTableModel(
                new String[] { "Tabla", "Columna", "Tipo", "Longitud", "NOT NULL", "PRIMARY KEY", "Default" },
                0);
        tablaAtributos = new JTable(modeloAtributos);
        panelAtributos.add(new JScrollPane(tablaAtributos), BorderLayout.CENTER);
        tabs.addTab("Atributos", panelAtributos);

        // Tab 3: Restricciones
        JPanel panelRestricciones = new JPanel(new BorderLayout());
        modeloRestricciones = new DefaultTableModel(
                new String[] { "Tabla", "Restricción", "Tipo" }, 0);
        tablaRestricciones = new JTable(modeloRestricciones);
        panelRestricciones.add(new JScrollPane(tablaRestricciones), BorderLayout.CENTER);
        tabs.addTab("Restricciones", panelRestricciones);

        // Tab 4: Errores
        JPanel panelErrores = new JPanel(new BorderLayout());
        textErrores = new JTextArea();
        textErrores.setFont(new Font("Courier New", Font.PLAIN, 10));
        textErrores.setEditable(false);
        panelErrores.add(new JScrollPane(textErrores), BorderLayout.CENTER);
        tabs.addTab("Errores y Análisis", panelErrores);

        panel.add(tabs, BorderLayout.CENTER);
        panel.setBorder(new TitledBorder("MÓDULO DE RESULTADOS - Tabla Semántica"));

        return panel;
    }

    private void analizarDDL() {
        String codigo = textEntrada.getText();
        if (codigo.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, ingrese código DDL", "Entrada vacía",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        limpiarResultados();

        AnalizadorDDL analizador = new AnalizadorDDL(codigo);
        analizador.analizar();

        // Mostrar tokens (para depuración)
        StringBuilder tokens = new StringBuilder("=== TOKENS GENERADOS ===\n");
        for (Token t : analizador.tokens) {
            tokens.append(t).append("\n");
        }

        // Mostrar tablas
        tokens.append("\n=== TABLAS ANALIZADAS ===\n");
        for (Tabla tabla : analizador.tablas) {
            tokens.append("- Tabla: ").append(tabla.nombre).append("\n");
            for (Atributo a : tabla.atributos) {
                tokens.append("  ").append(a).append("\n");
            }
            for (String r : tabla.restricciones) {
                tokens.append("  Restricción: ").append(r).append("\n");
            }
        }

        // Mostrar errores
        if (!analizador.errores.isEmpty()) {
            tokens.append("\n=== ERRORES DETECTADOS ===\n");
            for (ErrorSQL err : analizador.errores) {
                tokens.append(err).append("\n");
            }
        }

        textErrores.setText(tokens.toString());

        // Llenar tabla semántica
        for (Tabla tabla : analizador.tablas) {
            modeloSematica.addRow(new Object[] { tabla.nombre, tabla.atributos.size(),
                    tabla.restricciones.size() });

            // Llenar atributos
            for (Atributo a : tabla.atributos) {
                modeloAtributos.addRow(new Object[] {
                        tabla.nombre,
                        a.nombre,
                        a.tipo,
                        a.longitud,
                        a.notNull ? "SÍ" : "NO",
                        a.primaryKey ? "SÍ" : "NO",
                        a.valorDefault
                });
            }

            // Llenar restricciones
            for (String r : tabla.restricciones) {
                String tipo = r.contains("PRIMARY") ? "PRIMARY KEY"
                        : r.contains("FOREIGN") ? "FOREIGN KEY" : "OTRA";
                modeloRestricciones.addRow(new Object[] { tabla.nombre, r, tipo });
            }
        }
    }

    private void limpiarResultados() {
        modeloSematica.setRowCount(0);
        modeloAtributos.setRowCount(0);
        modeloRestricciones.setRowCount(0);
        textErrores.setText("");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            pr06 frame = new pr06();
            frame.setVisible(true);
        });
    }
}
