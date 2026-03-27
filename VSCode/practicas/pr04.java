package VSCode.practicas;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

public class pr04 extends JFrame {

    // Componentes visuales
    private JTextArea txtEntrada;
    private JButton btnAnalizar;
    private JTable tablaResultados;
    private DefaultTableModel modeloTabla;

    // Diccionarios para el Módulo de Análisis
    private Map<String, Integer> catalogoTokens;
    private Map<Integer, String> tablaSintactica;

    public pr04() {
        configurarDiccionarios();
        configurarInterfazVisual();
    }

    private void configurarDiccionarios() {
        // 1. Catálogo de Tokens (Palabras reservadas y símbolos a su código)
        catalogoTokens = new HashMap<>();
        catalogoTokens.put("CREATE", 16);
        catalogoTokens.put("TABLE", 17);
        catalogoTokens.put("CHAR", 18);
        catalogoTokens.put("CONSTRAINT", 22);
        catalogoTokens.put("PRIMARY", 24);
        catalogoTokens.put("KEY", 23);
        catalogoTokens.put("(", 52);
        catalogoTokens.put(")", 53);
        catalogoTokens.put(";", 55);

        // 2. Tabla Sintáctica (Mapeo de Código de Token -> Reglas Asociadas)
        // Se registran solo las columnas que tienen datos en la tabla de la imagen.
        tablaSintactica = new HashMap<>();
        tablaSintactica.put(4, "202, 206");
        tablaSintactica.put(16, "200, 201, 215");
        tablaSintactica.put(18, "203");
        tablaSintactica.put(19, "203");
        tablaSintactica.put(20, "204");
        tablaSintactica.put(22, "206, 207");
        tablaSintactica.put(24, "208");
        tablaSintactica.put(25, "208");
        tablaSintactica.put(26, "209");
        tablaSintactica.put(27, "211");
        tablaSintactica.put(50, "204, 205, 209, 210, 214");
        tablaSintactica.put(53, "205, 209, 210, 214");
        tablaSintactica.put(54, "212, 213");
        tablaSintactica.put(61, "212, 213");
        tablaSintactica.put(199, "201, 215");
    }

    private void configurarInterfazVisual() {
        setTitle("Analizador Sintáctico DDL");
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Módulo de Entrada (UI)
        JPanel panelNorte = new JPanel(new BorderLayout());
        panelNorte.setBorder(BorderFactory.createTitledBorder("Módulo de Entrada: Sentencia DDL"));
        txtEntrada = new JTextArea(4, 50);
        txtEntrada.setText("CREATE TABLE CARRERAS(\nCAR# CHAR(2)\nCONSTRAINT PK_DEPARTAMENTOS PRIMARY KEY (D#));");
        panelNorte.add(new JScrollPane(txtEntrada), BorderLayout.CENTER);

        btnAnalizar = new JButton("Analizar Sentencia");
        panelNorte.add(btnAnalizar, BorderLayout.SOUTH);

        // Módulo de Resultados (UI)
        JPanel panelCentro = new JPanel(new BorderLayout());
        panelCentro.setBorder(BorderFactory.createTitledBorder("Módulo de Resultados"));
        String[] columnas = {"Token", "Código", "Reglas asociadas"};
        modeloTabla = new DefaultTableModel(columnas, 0);
        tablaResultados = new JTable(modeloTabla);
        panelCentro.add(new JScrollPane(tablaResultados), BorderLayout.CENTER);

        add(panelNorte, BorderLayout.NORTH);
        add(panelCentro, BorderLayout.CENTER);

        // Acción del botón (Módulo de Análisis)
        btnAnalizar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                realizarAnalisis();
            }
        });
    }

    private void realizarAnalisis() {
        // Limpiar tabla de resultados anteriores
        modeloTabla.setRowCount(0);

        // 1. Obtener texto
        String entrada = txtEntrada.getText().trim();
        if (entrada.isEmpty()) return;

        // 2. Preprocesar para separar símbolos de puntuación
        entrada = entrada.replaceAll("\\(", " ( ");
        entrada = entrada.replaceAll("\\)", " ) ");
        entrada = entrada.replaceAll(";", " ; ");
        
        // 3. Tokenizar (separar por espacios o saltos de línea)
        String[] tokensBrutos = entrada.split("\\s+");

        // 4. Analizar cada token
        for (String tokenStr : tokensBrutos) {
            if (tokenStr.isEmpty()) continue;

            // Determinar Código
            int codigo = obtenerCodigoToken(tokenStr);

            // Determinar Reglas Asociadas según la tabla sintáctica
            String reglas = tablaSintactica.getOrDefault(codigo, "No tiene reglas asociadas");

            // Enviar a Módulo de Resultados (Agregar a la tabla)
            modeloTabla.addRow(new Object[]{tokenStr, codigo, reglas});
        }
    }

    private int obtenerCodigoToken(String tokenStr) {
        String tokenUpper = tokenStr.toUpperCase();
        
        // Si es una palabra reservada o símbolo registrado
        if (catalogoTokens.containsKey(tokenUpper)) {
            return catalogoTokens.get(tokenUpper);
        }
        
        // Si es un número (ej. "2")
        if (tokenStr.matches("\\d+")) {
            return 61;
        }
        
        // Si no es ninguno de los anteriores, se asume que es un Identificador
        return 4;
    }

    public static void main(String[] args) {
        // Ejecutar en el hilo de eventos de Swing
        SwingUtilities.invokeLater(() -> {
            new pr04().setVisible(true);
        });
    }
}
