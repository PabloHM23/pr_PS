package VSCode.practicas;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.*;
import java.util.List;

public class pr03 extends JFrame {

    private static final Map<String, Integer> PALABRAS_RESERVADAS = new HashMap<>();
    private static final Map<String, Integer> DELIMITADORES = new HashMap<>();
    private static final Map<String, Integer> OPERADORES = new HashMap<>();
    private static final Map<String, Integer> RELACIONALES = new HashMap<>();

    static {
        String[] pr = { "SELECT", "FROM", "WHERE", "IN", "AND", "OR", "CREATE", "TABLE",
                "CHAR", "NUMERIC", "NOT", "NULL", "CONSTRAINT", "KEY", "PRIMARY",
                "FOREIGN", "REFERENCES", "INSERT", "INTO", "VALUES" };
        int[] prCod = { 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29 };
        for (int i = 0; i < pr.length; i++)
            PALABRAS_RESERVADAS.put(pr[i], prCod[i]);

        DELIMITADORES.put(",", 50);
        DELIMITADORES.put(".", 51);
        DELIMITADORES.put("(", 52);
        DELIMITADORES.put(")", 53);
        DELIMITADORES.put("'", 54);
        DELIMITADORES.put(";", 55);

        OPERADORES.put("+", 70);
        OPERADORES.put("-", 71);
        OPERADORES.put("*", 72);
        OPERADORES.put("/", 73);

        RELACIONALES.put(">=", 84);
        RELACIONALES.put("<=", 85);
        RELACIONALES.put(">", 81);
        RELACIONALES.put("<", 82);
        RELACIONALES.put("=", 83);
        RELACIONALES.put("<>", 86);
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
        setSize(950, 680);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        construirUI();
    }

    // =========================================================
    // PARSER SINTÁCTICO
    // =========================================================

    private void analizarSintactico(List<Map<String, Object>> tokens) {
        this.tokensActuales = tokens;
        this.pos = 0;
        this.erroresSintacticos = new ArrayList<>();

        if (tokensActuales.isEmpty())
            return;

        if (esReservada("SELECT")) {
            parsearSelect();
        } else {
            registrarError(201, "Se esperaba SELECT al inicio");
        }
    }

    private void parsearSelect() {
        esperarReservada("SELECT");

        // --- Lista de columnas ---
        parsearListaColumnas();

        // --- FROM ---
        esperarReservada("FROM");

        // --- Origen de datos (puede ser tabla o subconsulta) ---
        parsearOrigenDatos();

        // Reunión Natural: FROM tabla, (SELECT ...) alias
        while (esDelimitador(",")) {
            consumir(); // consume ','
            parsearOrigenDatos();
        }

        // --- WHERE opcional ---
        if (esReservada("WHERE")) {
            consumir();
            parsearCondicion();
        }
    }

    /** col | tabla.col | * */
    private void parsearListaColumnas() {
        if (esTipo(7) && verLexema("*")) {
            consumir(); // SELECT *
        } else {
            parsearReferenciaColumna();
            while (esDelimitador(",")) {
                consumir();
                parsearReferenciaColumna();
            }
        }
    }

    /** id [. id] */
    private void parsearReferenciaColumna() {
        if (esTipo(4)) {
            consumir();
            if (esDelimitador(".")) {
                consumir();
                esperarIdentificador("nombre de columna");
            }
        } else {
            registrarError(204, "Se esperaba nombre de columna o identificador");
        }
    }

    /**
     * origen ::= identificador (tabla simple)
     * | '(' SELECT ... ')' alias (subconsulta con alias)
     */
    private void parsearOrigenDatos() {
        if (esDelimitador("(")) {
            consumir(); // '('
            if (esReservada("SELECT")) {
                parsearSelect(); // subconsulta recursiva
            } else {
                registrarError(201, "Se esperaba SELECT dentro de subconsulta");
            }
            esperarDelimitador(")");
            // Alias obligatorio después de la subconsulta
            esperarIdentificador("alias de subconsulta");
        } else {
            esperarIdentificador("nombre de tabla");
        }
    }

    /**
     * condicion ::= expr_comp [ (AND|OR) condicion ]*
     *
     * expr_comp ::= ref_valor op_relacional ref_valor
     * | ref_valor IN '(' lista_valores | SELECT ')'
     */
    private void parsearCondicion() {
        // Parte izquierda
        parsearRefValor();

        Map<String, Object> t = ver();
        if (t == null)
            return;

        if (esReservada("IN")) {
            consumir(); // IN
            esperarDelimitador("(");
            if (esReservada("SELECT")) {
                parsearSelect();
            } else {
                // lista de valores
                parsearValorLiteral();
                while (esDelimitador(",")) {
                    consumir();
                    parsearValorLiteral();
                }
            }
            esperarDelimitador(")");

        } else if (esTipo(8)) { // operador relacional: =, <>, >, <, >=, <=
            consumir();
            parsearRefValor();

        } else {
            registrarError(207, "Se esperaba operador relacional o IN después del atributo");
        }

        // AND / OR encadenado
        if (esReservada("AND") || esReservada("OR")) {
            consumir();
            parsearCondicion();
        }
    }

    /** ref_valor ::= id [. id] | constante_numerica | constante_cadena */
    private void parsearRefValor() {
        if (esTipo(4)) { // Identificador
            consumir();
            if (esDelimitador(".")) {
                consumir();
                esperarIdentificador("atributo");
            }
        } else if (esTipo(6)) { // Número
            consumir();
        } else if (esTipo(9)) { // Cadena entre comillas 'MESSI LIONEL'
            consumir();
        } else {
            registrarError(206, "Se esperaba identificador, número o cadena");
        }
    }

    /** Igual a parsearRefValor pero dentro de lista IN */
    private void parsearValorLiteral() {
        parsearRefValor();
    }

    // =========================================================
    // UTILIDADES DEL PARSER
    // =========================================================

    private Map<String, Object> ver() {
        return pos < tokensActuales.size() ? tokensActuales.get(pos) : null;
    }

    private boolean verLexema(String lex) {
        Map<String, Object> t = ver();
        return t != null && t.get("lexema").equals(lex);
    }

    private Map<String, Object> consumir() {
        return tokensActuales.get(pos++);
    }

    private boolean esTipo(int tipo) {
        Map<String, Object> t = ver();
        return t != null && (int) t.get("tipo") == tipo;
    }

    private boolean esReservada(String lex) {
        Map<String, Object> t = ver();
        return t != null && (int) t.get("tipo") == 1 && t.get("lexema").equals(lex);
    }

    private boolean esDelimitador(String lex) {
        Map<String, Object> t = ver();
        return t != null && (int) t.get("tipo") == 5 && t.get("lexema").equals(lex);
    }

    private void esperarReservada(String lex) {
        if (esReservada(lex))
            consumir();
        else
            registrarError(201, "Se esperaba palabra reservada '" + lex + "'");
    }

    private void esperarDelimitador(String lex) {
        if (esDelimitador(lex))
            consumir();
        else
            registrarError(205, "Se esperaba delimitador '" + lex + "'");
    }

    private void esperarIdentificador(String msg) {
        if (esTipo(4))
            consumir();
        else
            registrarError(204, "Se esperaba identificador (" + msg + ")");
    }

    private void registrarError(int cod, String desc) {
        Map<String, Object> err = new HashMap<>();
        err.put("tipo", 2);
        err.put("codigo", cod);
        Object linea = ver() != null
                ? ver().get("linea")
                : (tokensActuales.isEmpty() ? 1 : tokensActuales.get(tokensActuales.size() - 1).get("linea"));
        err.put("linea", linea);
        err.put("descripcion", desc);
        erroresSintacticos.add(err);
    }

    // TOKENIZADOR

    private List<Map<String, Object>> tokenizar(String sql) {
        List<Map<String, Object>> lista = new ArrayList<>();
        int linea = 1;
        int i = 0;
        int n = sql.length();

        while (i < n) {
            char c = sql.charAt(i);

            // Nueva línea
            if (c == '\n') {
                linea++;
                i++;
                continue;
            }

            // Espacios / tabuladores
            if (Character.isWhitespace(c)) {
                i++;
                continue;
            }

            // Cadena literal '...'
            if (c == '\'') {
                int start = i;
                i++; // saltar comilla de apertura
                StringBuilder sb = new StringBuilder();
                while (i < n && sql.charAt(i) != '\'') {
                    if (sql.charAt(i) == '\n')
                        linea++;
                    sb.append(sql.charAt(i));
                    i++;
                }
                if (i < n)
                    i++; // saltar comilla de cierre
                Map<String, Object> t = new HashMap<>();
                t.put("lexema", "'" + sb.toString() + "'");
                t.put("tipo", 9); // constante cadena
                t.put("codigo", 90);
                t.put("linea", linea);
                lista.add(t);
                continue;
            }

            // Operadores relacionales de 2 chars: >= <= <>
            if (i + 1 < n) {
                String dos = "" + c + sql.charAt(i + 1);
                if (RELACIONALES.containsKey(dos)) {
                    Map<String, Object> t = new HashMap<>();
                    t.put("lexema", dos);
                    t.put("tipo", 8);
                    t.put("codigo", RELACIONALES.get(dos));
                    t.put("linea", linea);
                    lista.add(t);
                    i += 2;
                    continue;
                }
            }

            // Operador relacional de 1 char: = > <
            String uno = String.valueOf(c);
            if (RELACIONALES.containsKey(uno)) {
                Map<String, Object> t = new HashMap<>();
                t.put("lexema", uno);
                t.put("tipo", 8);
                t.put("codigo", RELACIONALES.get(uno));
                t.put("linea", linea);
                lista.add(t);
                i++;
                continue;
            }

            // Operadores aritméticos * + - /
            if (OPERADORES.containsKey(uno)) {
                Map<String, Object> t = new HashMap<>();
                t.put("lexema", uno);
                t.put("tipo", 7);
                t.put("codigo", OPERADORES.get(uno));
                t.put("linea", linea);
                lista.add(t);
                i++;
                continue;
            }

            // Delimitadores , . ( ) ;
            if (DELIMITADORES.containsKey(uno)) {
                Map<String, Object> t = new HashMap<>();
                t.put("lexema", uno);
                t.put("tipo", 5);
                t.put("codigo", DELIMITADORES.get(uno));
                t.put("linea", linea);
                lista.add(t);
                i++;
                continue;
            }

            // Número
            if (Character.isDigit(c)) {
                StringBuilder sb = new StringBuilder();
                while (i < n && (Character.isDigit(sql.charAt(i)) || sql.charAt(i) == '.')) {
                    sb.append(sql.charAt(i++));
                }
                Map<String, Object> t = new HashMap<>();
                t.put("lexema", sb.toString());
                t.put("tipo", 6);
                t.put("codigo", 60);
                t.put("linea", linea);
                lista.add(t);
                continue;
            }

            // Identificador o Palabra Reservada
            if (Character.isLetter(c) || c == '_') {
                StringBuilder sb = new StringBuilder();
                while (i < n && (Character.isLetterOrDigit(sql.charAt(i)) || sql.charAt(i) == '_')) {
                    sb.append(sql.charAt(i++));
                }
                String lexema = sb.toString();
                String upper = lexema.toUpperCase();
                Map<String, Object> t = new HashMap<>();
                t.put("linea", linea);
                if (PALABRAS_RESERVADAS.containsKey(upper)) {
                    t.put("lexema", upper);
                    t.put("tipo", 1);
                    t.put("codigo", PALABRAS_RESERVADAS.get(upper));
                } else {
                    t.put("lexema", upper); // guardamos en mayúsculas
                    t.put("tipo", 4);
                    t.put("codigo", 400);
                }
                lista.add(t);
                continue;
            }

            // Carácter desconocido → error léxico
            Map<String, Object> t = new HashMap<>();
            t.put("lexema", uno);
            t.put("tipo", -1); // error léxico
            t.put("codigo", -1);
            t.put("linea", linea);
            lista.add(t);
            i++;
        }
        return lista;
    }

    // LÓGICA DE ANÁLISIS COMPLETO

    private void ejecutarAnalisis() {
        String sql = txtSql.getText().trim();
        if (sql.isEmpty())
            return;
        limpiarTablas();

        List<Map<String, Object>> tokens = tokenizar(sql);
        analizarSintactico(new ArrayList<>(tokens));

        // Llenar tabla de tokens
        int rowNum = 1;
        for (Map<String, Object> t : tokens) {
            int tipo = (int) t.get("tipo");
            if (tipo == -1) {
                modelErr.addRow(new Object[] { modelErr.getRowCount() + 1, "Léxico", -1, t.get("linea"),
                        "Carácter desconocido: " + t.get("lexema") });
            } else {
                modelSem.addRow(new Object[] { rowNum++, t.get("linea"), t.get("lexema"), tipo, t.get("codigo") });
            }
        }

        // Errores sintácticos
        for (Map<String, Object> e : erroresSintacticos) {
            modelErr.addRow(new Object[] { modelErr.getRowCount() + 1, "Sintáctico",
                    e.get("codigo"), e.get("linea"), e.get("descripcion") });
        }

        int totalErr = modelErr.getRowCount();
        if (totalErr > 0) {
            lblResultado.setText("Errores encontrados: " + totalErr);
            lblResultado.setForeground(Color.RED);
            tabs.setSelectedIndex(1);
        } else {
            lblResultado.setText("✔ Consulta válida (Subconsulta / Reunión Natural Integrada reconocida).");
            lblResultado.setForeground(new Color(0, 120, 0));
            tabs.setSelectedIndex(0);
        }
    }

    private void construirUI() {
        JPanel pnlNorte = new JPanel(new BorderLayout(5, 5));
        pnlNorte.setBorder(BorderFactory.createEmptyBorder(8, 8, 4, 8));
        pnlNorte.add(new JLabel("Ingrese SQL:"),
                BorderLayout.NORTH);

        txtSql = new JTextArea(10, 60);
        txtSql.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        // Texto de ejemplo con Reunión Natural
        txtSql.setText(
                "SELECT C.M,MNOMBRE\n" +
                        "FROM MATERIAS, (SELECT M\n" +
                        "                FROM INSCRITOS\n" +
                        "                WHERE A IN (SELECT A\n" +
                        "                             FROM ALUMNOS\n" +
                        "                             WHERE ANOMBRE='MESSI LIONEL')) C\n" +
                        "WHERE MATERIAS.M=C.M");
        pnlNorte.add(new JScrollPane(txtSql), BorderLayout.CENTER);

        JButton btn = new JButton("Analizar");
        btn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        btn.addActionListener(e -> ejecutarAnalisis());
        pnlNorte.add(btn, BorderLayout.SOUTH);
        add(pnlNorte, BorderLayout.NORTH);

        tabs = new JTabbedPane();

        modelSem = new DefaultTableModel(new String[] { "No.", "Línea", "Lexema", "Tipo", "Código" }, 0);
        JTable tblSem = new JTable(modelSem);
        tabs.addTab("Tokens", new JScrollPane(tblSem));

        modelErr = new DefaultTableModel(new String[] { "No.", "Tipo Error", "Código", "Línea", "Descripción" }, 0);
        JTable tblErr = new JTable(modelErr);
        JPanel pnlRes = new JPanel(new BorderLayout(4, 4));
        lblResultado = new JLabel(" ");
        lblResultado.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        lblResultado.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        pnlRes.add(lblResultado, BorderLayout.NORTH);
        pnlRes.add(new JScrollPane(tblErr), BorderLayout.CENTER);
        tabs.addTab("Errores / Resultado", pnlRes);

        add(tabs, BorderLayout.CENTER);
    }

    private void limpiarTablas() {
        modelSem.setRowCount(0);
        modelErr.setRowCount(0);
        lblResultado.setText(" ");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new pr03().setVisible(true));
    }
}