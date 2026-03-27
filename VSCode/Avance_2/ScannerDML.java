package VSCode.Avance_2;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.*;
import java.util.List;

public class ScannerDML extends JFrame {

    // --- Diccionarios de Tokens ---
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

    // --- Componentes UI ---
    private JTextArea txtSql;
    private JLabel lblResultado;
    private DefaultTableModel modelSem, modelId, modelConst, modelErr;
    private JTabbedPane tabs;

    public ScannerDML() {
        setTitle("Scanner DML - Java Edition");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        construirUI();
    }

    private void construirUI() {
        // Panel Norte: Entrada
        JPanel pnlNorte = new JPanel(new BorderLayout(5, 5));
        pnlNorte.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        pnlNorte.add(new JLabel("Sentencia SQL:"), BorderLayout.NORTH);
        
        txtSql = new JTextArea(6, 50);
        txtSql.setFont(new Font("Monospaced", Font.PLAIN, 13));
        pnlNorte.add(new JScrollPane(txtSql), BorderLayout.CENTER);

        JPanel pnlBotones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnAnalizar = new JButton("Analizar");
        JButton btnLimpiar = new JButton("Limpiar");
        pnlBotones.add(btnAnalizar);
        pnlBotones.add(btnLimpiar);
        pnlNorte.add(pnlBotones, BorderLayout.SOUTH);

        add(pnlNorte, BorderLayout.NORTH);

        // Panel Central: Tabs
        tabs = new JTabbedPane();
        modelSem = new DefaultTableModel(new String[]{"No.", "Línea", "TOKEN", "Tipo", "Código"}, 0);
        modelId = new DefaultTableModel(new String[]{"Identificador", "Valor", "Línea(s)"}, 0);
        modelConst = new DefaultTableModel(new String[]{"No.", "Constante", "Tipo", "Valor"}, 0);
        modelErr = new DefaultTableModel(new String[]{"No.", "Tipo", "Código", "Línea", "Descripción"}, 0);

        tabs.addTab("Tabla Semántica", new JScrollPane(new JTable(modelSem)));
        tabs.addTab("Identificadores", new JScrollPane(new JTable(modelId)));
        tabs.addTab("Constantes", new JScrollPane(new JTable(modelConst)));

        JPanel pnlErrores = new JPanel(new BorderLayout());
        lblResultado = new JLabel(" ");
        lblResultado.setFont(new Font("Arial", Font.BOLD, 12));
        pnlErrores.add(lblResultado, BorderLayout.NORTH);
        pnlErrores.add(new JScrollPane(new JTable(modelErr)), BorderLayout.CENTER);
        tabs.addTab("Errores / Resultado", pnlErrores);

        add(tabs, BorderLayout.CENTER);

        // Eventos
        btnAnalizar.addActionListener(e -> ejecutarAnalisis());
        btnLimpiar.addActionListener(e -> limpiar());
    }

    // --- Lógica del Analizador ---
    private void ejecutarAnalisis() {
        String sql = txtSql.getText().trim();
        if (sql.isEmpty()) return;

        limpiarTablas();
        
        List<Map<String, Object>> tokens = tokenizar(sql);
        List<Map<String, Object>> erroresLexicos = (List<Map<String, Object>>) tokens.remove(tokens.size() - 1).get("errores");
        
        // Simulación de análisis semántico y recolección de IDs/Constantes
        Map<String, Map<String, Object>> mapaIdent = new HashMap<>();
        List<Map<String, Object>> listaConst = new ArrayList<>();
        int contIdent = 401;
        int contConst = 600;

        for (int i = 0; i < tokens.size(); i++) {
            Map<String, Object> t = tokens.get(i);
            String lexema = (String) t.get("lexema");
            
            if (t.getOrDefault("es_ident", false).equals(true)) {
                if (!mapaIdent.containsKey(lexema)) {
                    Map<String, Object> info = new HashMap<>();
                    info.put("valor", contIdent++);
                    info.put("lineas", new TreeSet<Integer>());
                    mapaIdent.put(lexema, info);
                }
                ((TreeSet<Integer>) mapaIdent.get(lexema).get("lineas")).add((Integer) t.get("linea"));
                t.put("codigo", mapaIdent.get(lexema).get("valor"));
            }

            if (t.getOrDefault("es_const", false).equals(true)) {
                boolean existe = false;
                for (Map<String, Object> c : listaConst) {
                    if (c.get("lexema").equals(lexema)) { existe = true; break; }
                }
                if (!existe) {
                    Map<String, Object> cData = new HashMap<>();
                    cData.put("lexema", lexema);
                    cData.put("sub", t.get("sub"));
                    cData.put("no", i + 1);
                    cData.put("valor", contConst++);
                    listaConst.add(cData);
                }
                // Buscar valor asignado
                for (Map<String, Object> c : listaConst) {
                    if (c.get("lexema").equals(lexema)) t.put("codigo", c.get("valor"));
                }
            }

            modelSem.addRow(new Object[]{i + 1, t.get("linea"), t.get("es_const").equals(true) ? "CONSTANTE" : lexema, t.get("tipo"), t.get("codigo")});
        }

        // Llenar tablas de apoyo
        mapaIdent.forEach((k, v) -> modelId.addRow(new Object[]{k, v.get("valor"), v.get("lineas").toString()}));
        listaConst.forEach(c -> modelConst.addRow(new Object[]{c.get("no"), c.get("lexema"), c.get("sub"), c.get("valor")}));

        if (!erroresLexicos.isEmpty()) {
            lblResultado.setText("Se encontraron " + erroresLexicos.size() + " error(es).");
            lblResultado.setForeground(Color.RED);
            for (int i = 0; i < erroresLexicos.size(); i++) {
                Map<String, Object> e = erroresLexicos.get(i);
                modelErr.addRow(new Object[]{i + 1, e.get("tipo"), e.get("codigo"), e.get("linea"), e.get("descripcion")});
            }
            tabs.setSelectedIndex(3);
        } else {
            lblResultado.setText("Sentencia libre de errores. (Código 200)");
            lblResultado.setForeground(new Color(0, 128, 0));
        }
    }

    private List<Map<String, Object>> tokenizar(String sql) {
        List<Map<String, Object>> tokens = new ArrayList<>();
        List<Map<String, Object>> errores = new ArrayList<>();
        String[] lineas = sql.split("\n");

        for (int li = 0; li < lineas.length; li++) {
            String linea = lineas[li];
            int i = 0;
            while (i < linea.length()) {
                char c = linea.charAt(i);
                if (Character.isWhitespace(c)) { i++; continue; }

                // Comentarios
                if (i + 1 < linea.length() && linea.substring(i, i + 2).equals("--")) break;

                // Relacionales 2 caracteres
                if (i + 1 < linea.length()) {
                    String dos = linea.substring(i, i + 2);
                    if (RELACIONALES.containsKey(dos)) {
                        tokens.add(crearToken(dos, 8, RELACIONALES.get(dos), li + 1));
                        i += 2; continue;
                    }
                }

                String uno = String.valueOf(c);
                if (RELACIONALES.containsKey(uno)) {
                    tokens.add(crearToken(uno, 8, RELACIONALES.get(uno), li + 1));
                    i++; continue;
                }

                if (OPERADORES.containsKey(uno)) {
                    tokens.add(crearToken(uno, 7, OPERADORES.get(uno), li + 1));
                    i++; continue;
                }

                if (DELIMITADORES.containsKey(uno)) {
                    tokens.add(crearToken(uno, 5, DELIMITADORES.get(uno), li + 1));
                    i++; continue;
                }

                // Números
                if (Character.isDigit(c)) {
                    int j = i;
                    while (j < linea.length() && (Character.isDigit(linea.charAt(j)) || linea.charAt(j) == '.')) j++;
                    Map<String, Object> t = crearToken(linea.substring(i, j), 6, 0, li + 1);
                    t.put("es_const", true); t.put("sub", 61);
                    tokens.add(t);
                    i = j; continue;
                }

                // Identificadores / Reservadas
                if (Character.isLetter(c) || c == '_') {
                    int j = i;
                    while (j < linea.length() && (Character.isLetterOrDigit(linea.charAt(j)) || linea.charAt(j) == '_' || linea.charAt(j) == '#')) j++;
                    String palabra = linea.substring(i, j);
                    String up = palabra.toUpperCase();
                    if (PALABRAS_RESERVADAS.containsKey(up)) {
                        tokens.add(crearToken(up, 1, PALABRAS_RESERVADAS.get(up), li + 1));
                    } else {
                        Map<String, Object> t = crearToken(palabra, 4, 0, li + 1);
                        t.put("es_ident", true);
                        tokens.add(t);
                    }
                    i = j; continue;
                }

                // Error léxico
                Map<String, Object> err = new HashMap<>();
                err.put("tipo", 1); err.put("codigo", 101); err.put("linea", li + 1);
                err.put("descripcion", "Símbolo desconocido: '" + c + "'");
                errores.add(err);
                i++;
            }
        }
        Map<String, Object> resErr = new HashMap<>();
        resErr.put("errores", errores);
        tokens.add(resErr);
        return tokens;
    }

    private Map<String, Object> crearToken(String lex, int tipo, int cod, int lin) {
        Map<String, Object> t = new HashMap<>();
        t.put("lexema", lex); t.put("tipo", tipo); t.put("codigo", cod); t.put("linea", lin);
        t.put("es_const", false); t.put("es_ident", false);
        return t;
    }

    private void limpiar() {
        txtSql.setText("");
        limpiarTablas();
        lblResultado.setText(" ");
    }

    private void limpiarTablas() {
        modelSem.setRowCount(0); modelId.setRowCount(0);
        modelConst.setRowCount(0); modelErr.setRowCount(0);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ScannerDML().setVisible(true));
    }
}