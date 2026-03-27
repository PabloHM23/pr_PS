package VSCode.practicas;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.*;
import java.util.List;

public class pr03 extends JFrame {

    // --- Diccionarios de Tokens (Mismos que el anterior) ---
    private static final Map<String, Integer> PALABRAS_RESERVADAS = new HashMap<>();
    private static final Map<String, Integer> DELIMITADORES = new HashMap<>();
    private static final Map<String, Integer> OPERADORES = new HashMap<>();
    private static final Map<String, Integer> RELACIONALES = new HashMap<>();

    static {
        String[] pr = {"SELECT", "FROM", "WHERE", "IN", "AND", "OR", "CREATE", "TABLE", "CHAR", "NUMERIC", "NOT", "NULL", "CONSTRAINT", "KEY", "PRIMARY", "FOREIGN", "REFERENCES", "INSERT", "INTO", "VALUES"};
        int[] prCod = {10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29};
        for (int i = 0; i < pr.length; i++) PALABRAS_RESERVADAS.put(pr[i], prCod[i]);
        DELIMITADORES.put(",", 50); DELIMITADORES.put(".", 51); DELIMITADORES.put("(", 52); DELIMITADORES.put(")", 53); DELIMITADORES.put("'", 54);
        OPERADORES.put("+", 70); OPERADORES.put("-", 71); OPERADORES.put("*", 72); OPERADORES.put("/", 73);
        RELACIONALES.put(">=", 84); RELACIONALES.put("<=", 85); RELACIONALES.put(">", 81); RELACIONALES.put("<", 82); RELACIONALES.put("=", 83);
    }

    private JTextArea txtSql;
    private JLabel lblResultado;
    private DefaultTableModel modelSem, modelErr;
    private JTabbedPane tabs;
    private List<Map<String, Object>> tokensActuales;
    private int pos;
    private List<Map<String, Object>> erroresSintacticos;

    public pr03() {
        setTitle("Parser SQL - Reunión Natural Integrada");
        setSize(900, 650);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        construirUI();
    }

    // --- MÉTODOS DEL PARSER (NUEVA LÓGICA) ---

    private void analizarSintactico(List<Map<String, Object>> tokens) {
        this.tokensActuales = tokens;
        this.pos = 0;
        this.erroresSintacticos = new ArrayList<>();

        if (tokensActuales.isEmpty()) return;

        Map<String, Object> t = ver();
        if (t != null && t.get("lexema").equals("SELECT")) {
            parsearSelect();
        } else {
            registrarError(201, "Se esperaba SELECT al inicio");
        }
    }

    private void parsearSelect() {
        consumir(); // Consume 'SELECT'

        // Columnas
        if (esTipo(7) && ver().get("lexema").equals("*")) {
            consumir();
        } else {
            esperarIdentificador("columna");
            while (esDelimitador(",")) {
                consumir();
                esperarIdentificador("columna");
            }
        }

        esperarReservada("FROM");

        // --- LÓGICA DE REUNIÓN NATURAL INTEGRADA ---
        parsearOrigenDatos();
        while (esDelimitador(",")) {
            consumir();
            parsearOrigenDatos();
        }

        if (esReservada("WHERE")) {
            consumir();
            parsearCondicion();
        }
    }

    private void parsearOrigenDatos() {
        if (esDelimitador("(")) {
            consumir(); // (
            parsearSelect(); // Subconsulta recursiva
            esperarDelimitador(")");
            esperarIdentificador("Alias de subconsulta");
        } else {
            esperarIdentificador("tabla");
        }
    }

    private void parsearCondicion() {
        esperarIdentificador("atributo");
        if (esDelimitador(".")) {
            consumir();
            esperarIdentificador("atributo");
        }

        Map<String, Object> t = ver();
        if (t != null && t.get("lexema").equals("IN")) {
            consumir();
            esperarDelimitador("(");
            if (esReservada("SELECT")) parsearSelect();
            else {
                esperarValor();
                while (esDelimitador(",")) { consumir(); esperarValor(); }
            }
            esperarDelimitador(")");
        } else if (esTipo(8)) { // Relacional
            consumir();
            esperarValor();
        }

        if (esReservada("AND") || esReservada("OR")) {
            consumir();
            parsearCondicion();
        }
    }

    // --- MÉTODOS DE SOPORTE ---

    private Map<String, Object> ver() { return pos < tokensActuales.size() ? tokensActuales.get(pos) : null; }
    private Map<String, Object> consumir() { return tokensActuales.get(pos++); }
    
    private boolean esTipo(int tipo) { 
        Map<String, Object> t = ver(); 
        return t != null && (int)t.get("tipo") == tipo; 
    }

    private boolean esReservada(String lex) {
        Map<String, Object> t = ver();
        return t != null && (int)t.get("tipo") == 1 && t.get("lexema").equals(lex);
    }

    private boolean esDelimitador(String lex) {
        Map<String, Object> t = ver();
        return t != null && (int)t.get("tipo") == 5 && t.get("lexema").equals(lex);
    }

    private void esperarReservada(String lex) {
        if (esReservada(lex)) consumir();
        else registrarError(201, "Se esperaba '" + lex + "'");
    }

    private void esperarDelimitador(String lex) {
        if (esDelimitador(lex)) consumir();
        else registrarError(205, "Se esperaba delimitador '" + lex + "'");
    }

    private void esperarIdentificador(String msg) {
        if (esTipo(4)) consumir();
        else registrarError(204, "Se esperaba Identificador (" + msg + ")");
    }

    private void esperarValor() {
        if (esTipo(4) || esTipo(6)) {
            consumir();
            if (esDelimitador(".")) { consumir(); esperarIdentificador("atributo"); }
        } else registrarError(206, "Se esperaba constante o identificador");
    }

    private void registrarError(int cod, String desc) {
        Map<String, Object> err = new HashMap<>();
        err.put("tipo", 2);
        err.put("codigo", cod);
        err.put("linea", ver() != null ? ver().get("linea") : tokensActuales.get(tokensActuales.size()-1).get("linea"));
        err.put("descripcion", desc);
        erroresSintacticos.add(err);
    }

    // --- INTERFAZ Y TOKENIZADOR (SIMILAR AL ANTERIOR) ---

    private void ejecutarAnalisis() {
        String sql = txtSql.getText().trim();
        if (sql.isEmpty()) return;
        limpiarTablas();

        List<Map<String, Object>> tokens = tokenizar(sql); // (Implementación interna)
        analizarSintactico(new ArrayList<>(tokens)); // Clonamos para no afectar

        // Llenar tabla semántica
        for (int i = 0; i < tokens.size(); i++) {
            Map<String, Object> t = tokens.get(i);
            modelSem.addRow(new Object[]{i + 1, t.get("linea"), t.get("lexema"), t.get("tipo"), t.get("codigo")});
        }

        // Mostrar errores (Léxicos + Sintácticos)
        if (!erroresSintacticos.isEmpty()) {
            lblResultado.setText("Errores encontrados: " + erroresSintacticos.size());
            lblResultado.setForeground(Color.RED);
            for (int i = 0; i < erroresSintacticos.size(); i++) {
                Map<String, Object> e = erroresSintacticos.get(i);
                modelErr.addRow(new Object[]{i+1, "Sintáctico", e.get("codigo"), e.get("linea"), e.get("descripcion")});
            }
            tabs.setSelectedIndex(1);
        } else {
            lblResultado.setText("Consulta válida (Formato Natural Integrado reconocido).");
            lblResultado.setForeground(new Color(0, 100, 0));
        }
    }

    // ... (El resto de métodos UI y Tokenizar se mantienen iguales al código anterior) ...
    // Para brevedad, omito el tokenizar que ya tienes, pero asegúrate de incluirlo.

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new pr03().setVisible(true));
    }

    private void construirUI() {
        JPanel pnlNorte = new JPanel(new BorderLayout(5, 5));
        pnlNorte.add(new JLabel("Ingrese SQL (Soporta Reunión Natural Integrada):"), BorderLayout.NORTH);
        txtSql = new JTextArea(8, 50);
        pnlNorte.add(new JScrollPane(txtSql), BorderLayout.CENTER);
        JButton btn = new JButton("Analizar");
        btn.addActionListener(e -> ejecutarAnalisis());
        pnlNorte.add(btn, BorderLayout.SOUTH);
        add(pnlNorte, BorderLayout.NORTH);

        tabs = new JTabbedPane();
        modelSem = new DefaultTableModel(new String[]{"No.", "Línea", "Lexema", "Tipo", "Código"}, 0);
        modelErr = new DefaultTableModel(new String[]{"No.", "Tipo", "Código", "Línea", "Descripción"}, 0);
        tabs.addTab("Tokens", new JScrollPane(new JTable(modelSem)));
        
        JPanel pnlRes = new JPanel(new BorderLayout());
        lblResultado = new JLabel(" ");
        pnlRes.add(lblResultado, BorderLayout.NORTH);
        pnlRes.add(new JScrollPane(new JTable(modelErr)), BorderLayout.CENTER);
        tabs.addTab("Errores", pnlRes);
        add(tabs, BorderLayout.CENTER);
    }

    private void limpiarTablas() { modelSem.setRowCount(0); modelErr.setRowCount(0); }

    // Tokenizador básico para que el ejemplo funcione
    private List<Map<String, Object>> tokenizar(String sql) {
        List<Map<String, Object>> lista = new ArrayList<>();
        // Lógica simplificada: separa por espacios y limpia signos
        String[] parts = sql.replace("(", " ( ").replace(")", " ) ").replace(",", " , ").split("\\s+");
        int linea = 1;
        for (String p : parts) {
            if (p.isEmpty()) continue;
            Map<String, Object> t = new HashMap<>();
            t.put("lexema", p.toUpperCase());
            t.put("linea", linea);
            if (PALABRAS_RESERVADAS.containsKey(p.toUpperCase())) { t.put("tipo", 1); t.put("codigo", PALABRAS_RESERVADAS.get(p.toUpperCase())); }
            else if (DELIMITADORES.containsKey(p)) { t.put("tipo", 5); t.put("codigo", DELIMITADORES.get(p)); }
            else { t.put("tipo", 4); t.put("codigo", 400); }
            lista.add(t);
        }
        return lista;
    }
}