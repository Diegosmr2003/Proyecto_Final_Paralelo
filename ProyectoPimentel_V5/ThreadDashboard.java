import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ThreadDashboard extends JPanel {
    // Estados globales
    private final JLabel lblRun   = new JLabel("0");
    private final JLabel lblTimed = new JLabel("0");
    private final JLabel lblBlocked = new JLabel("0");
    private final JLabel lblTerm  = new JLabel("0");
    private final JLabel lblTotal = new JLabel("0");

    // Tabla por tipo de agente
    private final JTable tablaTipos;
    private final DefaultTableModel modeloTabla;

    // Tabla de Buffers
    private final JTable tablaBuffers;
    private final DefaultTableModel modeloBuffers;

    // Tabla de Zonas Criticas
    private final JTable tablaZonasCriticas;
    private final DefaultTableModel modeloZonasCriticas;

    private SimulationController controller;
    private final Timer timer;

    public ThreadDashboard() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ===== PANEL SUPERIOR: Estados globales =====
        JPanel panelEstados = new JPanel(new GridLayout(5, 2, 8, 4));
        panelEstados.setBorder(BorderFactory.createTitledBorder("Estados globales de Threads"));

        Font bold = new Font("Arial", Font.BOLD, 14);
        for (JLabel l : new JLabel[]{lblRun, lblTimed, lblBlocked, lblTerm, lblTotal}) {
            l.setFont(bold);
            l.setHorizontalAlignment(SwingConstants.CENTER);
        }

        panelEstados.add(new JLabel("RUNNABLE:"));
        panelEstados.add(lblRun);
        panelEstados.add(new JLabel("TIMED_WAITING:"));
        panelEstados.add(lblTimed);
        panelEstados.add(new JLabel("BLOCKED:"));
        panelEstados.add(lblBlocked);
        panelEstados.add(new JLabel("TERMINATED:"));
        panelEstados.add(lblTerm);
        panelEstados.add(new JLabel("TOTAL:"));
        panelEstados.add(lblTotal);

        // ===== TABLA: Estados por tipo de agente =====
        String[] cols = {"Tipo de Agente", "RUNNABLE", "TIMED_WAITING", "BLOCKED", "TERMINATED", "TOTAL"};
        modeloTabla = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaTipos = new JTable(modeloTabla);
        tablaTipos.setRowHeight(25);
        tablaTipos.setFont(new Font("Arial", Font.PLAIN, 12));
        
        JScrollPane scrollTabla = new JScrollPane(tablaTipos);
        scrollTabla.setBorder(BorderFactory.createTitledBorder("Estados por tipo de agente"));
        scrollTabla.setPreferredSize(new Dimension(800, 150));

        // ===== PANEL CENTRAL: Estados + Tabla de Agentes =====
        JPanel panelSuperior = new JPanel(new BorderLayout(5, 5));
        panelSuperior.add(panelEstados, BorderLayout.NORTH);
        panelSuperior.add(scrollTabla, BorderLayout.CENTER);

        // ===== PANEL DE RESUMEN GLOBAL =====
        JPanel panelResumenGlobal = new JPanel(new GridLayout(1, 2, 15, 0));
        panelResumenGlobal.setBorder(BorderFactory.createTitledBorder("Resumen Global (Buffers y Zonas Críticas)"));

        // TABLA DE BUFFERS
        String[] colsBuffers = {"Buffer", "Agentes que lo usan", "Estado actual"};
        modeloBuffers = new DefaultTableModel(colsBuffers, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaBuffers = new JTable(modeloBuffers);
        tablaBuffers.setRowHeight(25);
        tablaBuffers.setFont(new Font("Arial", Font.PLAIN, 12));
        tablaBuffers.getColumnModel().getColumn(0).setPreferredWidth(120);
        tablaBuffers.getColumnModel().getColumn(1).setPreferredWidth(200);
        tablaBuffers.getColumnModel().getColumn(2).setPreferredWidth(100);
        
        JScrollPane scrollBuffers = new JScrollPane(tablaBuffers);
        scrollBuffers.setBorder(BorderFactory.createTitledBorder("Buffers"));

        // TABLA DE ZONAS CRITICAS
        String[] colsZonas = {"Zona Crítica", "Agentes que la usan", "Estado"};
        modeloZonasCriticas = new DefaultTableModel(colsZonas, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaZonasCriticas = new JTable(modeloZonasCriticas);
        tablaZonasCriticas.setRowHeight(25);
        tablaZonasCriticas.setFont(new Font("Arial", Font.PLAIN, 12));
        tablaZonasCriticas.getColumnModel().getColumn(0).setPreferredWidth(150);
        tablaZonasCriticas.getColumnModel().getColumn(1).setPreferredWidth(150);
        tablaZonasCriticas.getColumnModel().getColumn(2).setPreferredWidth(200);
        
        JScrollPane scrollZonas = new JScrollPane(tablaZonasCriticas);
        scrollZonas.setBorder(BorderFactory.createTitledBorder("Zonas Críticas"));

        panelResumenGlobal.add(scrollBuffers);
        panelResumenGlobal.add(scrollZonas);

        add(panelSuperior, BorderLayout.NORTH);
        add(panelResumenGlobal, BorderLayout.CENTER);

        timer = new Timer(100, e -> refresh());
        timer.start();
    }

    public void setController(SimulationController controller) {
        this.controller = controller;
        updateResumen();
    }

    private void refresh() {
        List<Thread> ts = snapshot();
        int runnable = 0, timed = 0, blocked = 0, term = 0;

        Map<String, int[]> tipos = new LinkedHashMap<>();

        for (Thread t : ts) {
            Thread.State s = t.getState();
            String tipo;
            if (t.getName().startsWith("Cliente-")) {
                tipo = "Cliente";
            } else if (t.getName().startsWith("Limpieza-")) {
                tipo = "Limpieza";
            } else if (t.getName().startsWith("Instructor-")) {
                tipo = "Instructor";
            } else if (t.getName().startsWith("Recepcionista-")) {
                tipo = "Recepcionista";
            } else {
                tipo = t.getClass().getSimpleName();
            }

            tipos.putIfAbsent(tipo, new int[5]); 
            int[] arr = tipos.get(tipo);

            switch (s) {
                case RUNNABLE -> { arr[0]++; runnable++; }
                case TIMED_WAITING -> { arr[1]++; timed++; }
                case BLOCKED -> { arr[2]++; blocked++; }
                case TERMINATED -> { arr[3]++; term++; }
                default -> {} 
            }
            arr[4]++; 
        }

        lblRun.setText(String.valueOf(runnable));
        lblTimed.setText(String.valueOf(timed));
        lblBlocked.setText(String.valueOf(blocked));
        lblTerm.setText(String.valueOf(term));
        lblTotal.setText(String.valueOf(ts.size()));

        modeloTabla.setRowCount(0);
        for (Map.Entry<String, int[]> e : tipos.entrySet()) {
            int[] a = e.getValue();
            modeloTabla.addRow(new Object[]{
                    e.getKey(), a[0], a[1], a[2], a[3], a[4]
            });
        }

        updateResumen();
    }

    private void updateResumen() {
        if (controller == null) return;
        Params p = controller.getParams();
        if (p == null) return;

        int nClientes = p.numClientes;
        int nLimpieza = p.numLimpieza;

        // ACTUALIZAR TABLA DE BUFFERS 
        modeloBuffers.setRowCount(0);

        // Buffer Toallas
        int agentesToallas = nClientes + nLimpieza;
        String estadoToallas = getEstadoBuffer(controller.bufToallas);
        modeloBuffers.addRow(new Object[]{
            "Toallas",
            agentesToallas + " (Clientes: " + nClientes + ", Limpieza: " + nLimpieza + ")",
            estadoToallas
        });

        // Buffer DuchasLibres
        int agentesDuchasLibres = nClientes + nLimpieza;
        String estadoDuchasLibres = getEstadoBuffer(controller.bufDuchasLibres);
        modeloBuffers.addRow(new Object[]{
            "DuchasLibres",
            agentesDuchasLibres + " (Clientes: " + nClientes + ", Limpieza: " + nLimpieza + ")",
            estadoDuchasLibres
        });

        // Buffer DuchasSucias
        int agentesDuchasSucias = nClientes + nLimpieza;
        String estadoDuchasSucias = getEstadoBuffer(controller.bufDuchasSucias);
        modeloBuffers.addRow(new Object[]{
            "DuchasSucias",
            agentesDuchasSucias + " (Clientes: " + nClientes + ", Limpieza: " + nLimpieza + ")",
            estadoDuchasSucias
        });

        // ACTUALIZAR TABLA DE ZONAS CRÍTICAS 
        modeloZonasCriticas.setRowCount(0);

        // Torniquete
        modeloZonasCriticas.addRow(new Object[]{
            "Torniquete",
            nClientes + " (Clientes)",
            "Zona sincronizada (synchronized)"
        });

        // Máquinas (Caminadora, Bicicleta, Elíptica)
        modeloZonasCriticas.addRow(new Object[]{
            "Caminadora-01",
            nClientes + " (Clientes)",
            "Zona sincronizada (synchronized)"
        });

        modeloZonasCriticas.addRow(new Object[]{
            "Bicicleta-01",
            nClientes + " (Clientes)",
            "Zona sincronizada (synchronized)"
        });

        modeloZonasCriticas.addRow(new Object[]{
            "Eliptica-01",
            nClientes + " (Clientes)",
            "Zona sincronizada (synchronized)"
        });

        // ClaseGrupo
        modeloZonasCriticas.addRow(new Object[]{
            "ClaseGrupo",
            nClientes + " (Clientes)",
            "Zona sincronizada (synchronized)"
        });
    }

    private String getEstadoBuffer(Buffer buffer) {
        if (buffer == null) return "N/A";
        return buffer.getCantidad() + "/" + buffer.getCapacidad();
    }

    private List<Thread> snapshot() {
        if (controller == null) return new ArrayList<>();
        return controller.snapshotAgentes();
    }
}