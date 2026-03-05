import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EscanerSQL extends JFrame {

    private JTextArea txtEntrada;
    private JTable tablaIdentificadores;
    private JTable tablaConstantes;
    private DefaultTableModel modeloIdentificadores;
    private DefaultTableModel modeloConstantes;

    private static final Set<String> PALABRAS_RESERVADAS = new HashSet<>(Arrays.asList(
            "SELECT", "FROM", "WHERE", "AND", "OR", "NOT", "INSERT", "DELETE", "UPDATE"
    ));

    public EscanerSQL() {
        setTitle("Escáner SQL - Tablas Dinámicas");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        JPanel panelNorte = new JPanel(new BorderLayout());
        panelNorte.setBorder(BorderFactory.createTitledBorder("Módulo de Entrada (Sentencia SQL)"));
        
        txtEntrada = new JTextArea(8, 40);
        txtEntrada.setFont(new Font("Monospaced", Font.PLAIN, 14));
        
        txtEntrada.setText("SELECT ANOMBRE, CALIFICACION, TURNO\n" +
                           "FROM ALUMNOS, INSCRITOS, MATERIAS, CARRERAS\n" +
                           "WHERE MNOMBRE='PROGSIST' AND TURNO = 'TV'\n" +
                           "AND CNOMBRE='IDS' AND SEMESTRE='EJ2026' AND CALIFICACION >= 60");
        JScrollPane scrollEntrada = new JScrollPane(txtEntrada);
        panelNorte.add(scrollEntrada, BorderLayout.CENTER);

        JButton btnAnalizar = new JButton("Analizar Consulta");
        btnAnalizar.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnAnalizar.addActionListener(e -> moduloAnalisis());
        panelNorte.add(btnAnalizar, BorderLayout.SOUTH); 

        add(panelNorte, BorderLayout.NORTH);

        JPanel panelCentro = new JPanel(new GridLayout(1, 2, 10, 10));
        panelCentro.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        modeloIdentificadores = new DefaultTableModel(new Object[]{"Identificador", "Valor (ID)", "Línea"}, 0);
        tablaIdentificadores = new JTable(modeloIdentificadores);
        JScrollPane scrollId = new JScrollPane(tablaIdentificadores);
        scrollId.setBorder(BorderFactory.createTitledBorder("Tabla de Identificadores (400+)"));
        
        modeloConstantes = new DefaultTableModel(new Object[]{"Constante", "Valor (ID)", "Línea"}, 0);
        tablaConstantes = new JTable(modeloConstantes);
        JScrollPane scrollConst = new JScrollPane(tablaConstantes);
        scrollConst.setBorder(BorderFactory.createTitledBorder("Tabla de Constantes (600+)"));

        panelCentro.add(scrollId);
        panelCentro.add(scrollConst);

        add(panelCentro, BorderLayout.CENTER);
    }

    private void moduloAnalisis() {
        modeloIdentificadores.setRowCount(0);
        modeloConstantes.setRowCount(0);

        String texto = txtEntrada.getText();
        texto = texto.replace("’", "'").replace("‘", "'");

        String[] lineas = texto.split("\n");

        Map<String, Simbolo> mapaIdentificadores = new LinkedHashMap<>();
        Map<String, Simbolo> mapaConstantes = new LinkedHashMap<>();

        int contadorId = 401;
        int contadorConst = 600;

        Pattern patron = Pattern.compile("('[^']*')|([a-zA-Z_][a-zA-Z0-9_]*)|([0-9]+)");

        for (int i = 0; i < lineas.length; i++) {
            int numeroLinea = i + 1;
            String lineaActual = lineas[i];
            Matcher matcher = patron.matcher(lineaActual);

            while (matcher.find()) {
                String token = matcher.group();

                if (token.startsWith("'")) {
                    String valorLimpio = token.replace("'", ""); 
                    
                    if (!mapaConstantes.containsKey(valorLimpio)) {
                        mapaConstantes.put(valorLimpio, new Simbolo(contadorConst++, numeroLinea));
                    } else {
                        mapaConstantes.get(valorLimpio).agregarLinea(numeroLinea);
                    }
                }
                else if (token.matches("^[0-9]+$")) {
                    if (!mapaConstantes.containsKey(token)) {
                        mapaConstantes.put(token, new Simbolo(contadorConst++, numeroLinea));
                    } else {
                        mapaConstantes.get(token).agregarLinea(numeroLinea);
                    }
                }
                else {
                    if (!PALABRAS_RESERVADAS.contains(token.toUpperCase())) {
                        if (!mapaIdentificadores.containsKey(token)) {
                            mapaIdentificadores.put(token, new Simbolo(contadorId++, numeroLinea));
                        } else {
                            mapaIdentificadores.get(token).agregarLinea(numeroLinea);
                        }
                    }
                }
            }
        }

        llenarTabla(modeloIdentificadores, mapaIdentificadores);
        llenarTabla(modeloConstantes, mapaConstantes);
    }

    private void llenarTabla(DefaultTableModel modelo, Map<String, Simbolo> datos) {
        for (Map.Entry<String, Simbolo> entry : datos.entrySet()) {
            modelo.addRow(new Object[]{
                entry.getKey(),
                entry.getValue().id,
                entry.getValue().obtenerLineas()
            });
        }
    }

    class Simbolo {
        int id;
        Set<Integer> lineas;

        public Simbolo(int id, int linea) {
            this.id = id;
            this.lineas = new LinkedHashSet<>();
            this.lineas.add(linea);
        }

        public void agregarLinea(int linea) {
            this.lineas.add(linea);
        }

        public String obtenerLineas() {
            return lineas.toString().replace("[", "").replace("]", "");
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            new EscanerSQL().setVisible(true);
        });
    }
}