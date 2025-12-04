import javax.swing.*;
import java.awt.*;

public class ParaGym {

    private JFrame frame;         
    private JFrame frameTablero;  
    private JFrame frameLog;
    private JFrame frameGym;
    private JFrame frameBPMN; 
    private JFrame frameCredits;

    private JSpinner spClientes, spLimpieza, spToallasMax, spDuchasTot, spToallasIni;
    private JSpinner spPasoTorniquete, spUsoMaquina, spDucha, spLimpiezaDucha, spPausaLimpieza, spInterLlegada;
    private JSpinner spToallasPorLote;

    private JButton btnIniciar, btnDetener, btnVerTablero, btnVerLog, btnVerBPMN, btnCreditos;  

    private SimulationController controller;
    private ThreadDashboard dashboard;
    private JTextArea areaLog;
    private GymCanvas gymCanvas;
    private BPMNMonitor bpmnMonitor;  

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ParaGym().initUI());
    }

    private void initUI() {

        // Ventana de configuración
        frame = new JFrame("ParaGym - Configuración de Simulación");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 6, 5, 6);
        c.anchor = GridBagConstraints.WEST;

        // Cantidades
        spClientes    = new JSpinner(new SpinnerNumberModel(10, 1, 1000, 1));
        spLimpieza    = new JSpinner(new SpinnerNumberModel(2, 1, 200, 1));
        spToallasMax  = new JSpinner(new SpinnerNumberModel(8, 1, 5000, 1));
        spToallasIni  = new JSpinner(new SpinnerNumberModel(4, 0, 5000, 1));
        spDuchasTot   = new JSpinner(new SpinnerNumberModel(4, 1, 200, 1));

        // Tiempos
        spPasoTorniquete = new JSpinner(new SpinnerNumberModel(50, 1, 20000, 10));
        spUsoMaquina     = new JSpinner(new SpinnerNumberModel(300, 1, 60000, 50));
        spDucha          = new JSpinner(new SpinnerNumberModel(200, 1, 60000, 50));
        spLimpiezaDucha  = new JSpinner(new SpinnerNumberModel(120, 1, 60000, 50));
        spPausaLimpieza  = new JSpinner(new SpinnerNumberModel(80, 0, 60000, 10));
        spInterLlegada   = new JSpinner(new SpinnerNumberModel(40, 0, 60000, 5));

        // Producción
        spToallasPorLote = new JSpinner(new SpinnerNumberModel(2, 1, 1000, 1));

        int row = 0;
        addRow(form, c, row++, titleLabel("Cantidades"));
        addRow(form, c, row++, "Clientes:", spClientes);
        addRow(form, c, row++, "Limpieza:", spLimpieza);
        addRow(form, c, row++, "Toallas máx:", spToallasMax);
        addRow(form, c, row++, "Toallas iniciales:", spToallasIni);
        addRow(form, c, row++, "Duchas totales:", spDuchasTot);

        addRow(form, c, row++, titleLabel("Tiempos (ms)"));
        addRow(form, c, row++, "Paso torniquete:", spPasoTorniquete);
        addRow(form, c, row++, "Uso máquina:", spUsoMaquina);
        addRow(form, c, row++, "Ducha (cliente):", spDucha);
        addRow(form, c, row++, "Limpieza ducha:", spLimpiezaDucha);
        addRow(form, c, row++, "Pausa entre ciclos de limpieza:", spPausaLimpieza);
        addRow(form, c, row++, "Inter-arribo clientes:", spInterLlegada);

        addRow(form, c, row++, titleLabel("Producción"));
        addRow(form, c, row++, "Toallas por lote:", spToallasPorLote);

        // Botones
        btnIniciar    = new JButton("Iniciar simulación");
        btnDetener    = new JButton("Detener");
        btnVerTablero = new JButton("Ver tablero");
        btnVerLog     = new JButton("Ver log");
        btnVerBPMN    = new JButton("Ver Monitor BPMN"); 
        btnCreditos   = new JButton("Créditos"); 

        btnIniciar.addActionListener(e -> onIniciar());
        btnDetener.addActionListener(e -> onDetener());
        btnVerTablero.addActionListener(e -> mostrarTablero());
        btnVerLog.addActionListener(e -> mostrarLog());
        btnVerBPMN.addActionListener(e -> mostrarBPMN()); 
        btnCreditos.addActionListener(e -> mostrarCreditos()); 

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelBotones.add(btnIniciar);
        panelBotones.add(btnDetener);
        panelBotones.add(btnVerTablero);
        panelBotones.add(btnVerLog);
        panelBotones.add(btnVerBPMN); 
        panelBotones.add(btnCreditos); 

        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(form, BorderLayout.CENTER);
        frame.getContentPane().add(panelBotones, BorderLayout.SOUTH);

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        dashboard = new ThreadDashboard();
        frameTablero = new JFrame("Tablero de hilos y agentes");
        frameTablero.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        frameTablero.getContentPane().add(dashboard);
        frameTablero.pack();
        frameTablero.setLocationRelativeTo(frame);
        frameTablero.setVisible(false);

        areaLog = new JTextArea(25, 80);
        areaLog.setEditable(false);
        JScrollPane scrollLog = new JScrollPane(areaLog);

        frameLog = new JFrame("Log de simulación");
        frameLog.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        frameLog.getContentPane().add(scrollLog);
        frameLog.pack();
        frameLog.setLocationRelativeTo(frame);
        frameLog.setVisible(false);

        gymCanvas = new GymCanvas();
        frameGym = new JFrame("Vista del Gimnasio");
        frameGym.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        frameGym.getContentPane().add(gymCanvas);
        frameGym.pack();
        frameGym.setLocationRelativeTo(frame);
        frameGym.setVisible(false);

        bpmnMonitor = new BPMNMonitor();
        frameBPMN = bpmnMonitor;  
        frameBPMN.setLocationRelativeTo(frame);
        frameBPMN.setVisible(false);

        frameCredits = new CreditsPanel();
        frameCredits.setLocationRelativeTo(frame);
        frameCredits.setVisible(false);

        // Logger
        Log.setSalida(texto -> {
            areaLog.append(texto + "\n");
            areaLog.setCaretPosition(areaLog.getDocument().getLength());
        });
    }

    private JLabel titleLabel(String t) {
        JLabel l = new JLabel(t);
        l.setFont(l.getFont().deriveFont(Font.BOLD));
        return l;
    }

    private void onIniciar() {

        if (controller != null && controller.isCorriendo()) {
            Log.log("La simulación ya está corriendo.");
            return;
        }

        int toallasIni = (Integer) spToallasIni.getValue();
        int toallasMax = (Integer) spToallasMax.getValue();
        if (toallasIni > toallasMax) {
            toallasIni = toallasMax;
            spToallasIni.setValue(toallasIni);
        }

        Params p = new Params(
                (Integer) spClientes.getValue(),
                (Integer) spLimpieza.getValue(),
                toallasMax,
                (Integer) spDuchasTot.getValue(),
                toallasIni,
                (Integer) spPasoTorniquete.getValue(),
                (Integer) spUsoMaquina.getValue(),
                (Integer) spDucha.getValue(),
                (Integer) spLimpiezaDucha.getValue(),
                (Integer) spPausaLimpieza.getValue(),
                (Integer) spInterLlegada.getValue(),
                (Integer) spToallasPorLote.getValue()
        );

        controller = new SimulationController(p);

        controller.setGymCanvas(gymCanvas);

        dashboard.setController(controller);
        bpmnMonitor.setController(controller);  

        // Mostrar ventanas
        frameTablero.setVisible(true);
        frameLog.setVisible(true);
        frameGym.setVisible(true);
        frameBPMN.setVisible(true);  

        gymCanvas.iniciar();

        controller.iniciar();
    }

    private void onDetener() {
        if (controller != null) {
            controller.detener();
            Log.log("Solicitud de detener enviada.");
        }
        if (gymCanvas != null)
            gymCanvas.detener();
        if (bpmnMonitor != null)
            bpmnMonitor.detener();
    }

    private void mostrarTablero() {
        frameTablero.setVisible(true);
    }

    private void mostrarLog() {
        frameLog.setVisible(true);
    }

    private void mostrarBPMN() { 
        frameBPMN.setVisible(true);
    }

    private void mostrarCreditos() {
        frameCredits.setVisible(true);
    }

    private static void addRow(JPanel p, GridBagConstraints c, int row, String label, JComponent comp) {
        c.gridx = 0; c.gridy = row; c.gridwidth = 1;
        p.add(new JLabel(label), c);
        c.gridx = 1;
        p.add(comp, c);
    }

    private static void addRow(JPanel p, GridBagConstraints c, int row, JComponent compSpan2) {
        c.gridx = 0; c.gridy = row; c.gridwidth = 2;
        p.add(compSpan2, c);
        c.gridwidth = 1;
    }
}