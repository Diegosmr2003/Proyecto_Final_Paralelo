import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Map;

public class BPMNMonitor extends JFrame {
    
    private SimulationController controller;
    
    private final JTable tablaCliente;
    private final DefaultTableModel modeloCliente;
    
    private final JTable tablaLimpieza;
    private final DefaultTableModel modeloLimpieza;
    
    private final JTable tablaInstructor;
    private final DefaultTableModel modeloInstructor;
    
    private final JTable tablaRecepcionista;
    private final DefaultTableModel modeloRecepcionista;
    
    private final Timer timer;
    
    public BPMNMonitor() {
        super("Monitor de Estados BPMN - Todos los Agentes");
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setSize(700, 500);
        
        JTabbedPane tabbedPane = new JTabbedPane();
        
        String[] colsBPMN = {"Estado BPMN", "Cantidad de Agentes"};
        modeloCliente = new DefaultTableModel(colsBPMN, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaCliente = new JTable(modeloCliente);
        tablaCliente.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollCliente = new JScrollPane(tablaCliente);
        tabbedPane.addTab("👤 Cliente", scrollCliente);
        
        modeloLimpieza = new DefaultTableModel(colsBPMN, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaLimpieza = new JTable(modeloLimpieza);
        tablaLimpieza.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollLimpieza = new JScrollPane(tablaLimpieza);
        tabbedPane.addTab("Limpieza", scrollLimpieza);
        
        modeloInstructor = new DefaultTableModel(colsBPMN, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaInstructor = new JTable(modeloInstructor);
        tablaInstructor.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollInstructor = new JScrollPane(tablaInstructor);
        tabbedPane.addTab("Instructor", scrollInstructor);
        
        modeloRecepcionista = new DefaultTableModel(colsBPMN, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaRecepcionista = new JTable(modeloRecepcionista);
        tablaRecepcionista.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollRecepcionista = new JScrollPane(tablaRecepcionista);
        tabbedPane.addTab(" Recepcionista", scrollRecepcionista);
        
        add(tabbedPane, BorderLayout.CENTER);
        
        JPanel panelInfo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel lblInfo = new JLabel("Actualización automática cada 100ms");
        lblInfo.setFont(new Font("Arial", Font.ITALIC, 11));
        panelInfo.add(lblInfo);
        add(panelInfo, BorderLayout.SOUTH);
        
        timer = new Timer(100, e -> actualizarTodo());
        timer.start();
        
        setLocationRelativeTo(null);
    }
    
    public void setController(SimulationController controller) {
        this.controller = controller;
        actualizarTodo();
    }
    
    private void actualizarTodo() {
        if (controller == null) return;
        
        actualizarCliente();
        actualizarLimpieza();
        actualizarInstructor();
        actualizarRecepcionista();
    }
    
    private void actualizarCliente() {
        Map<Cliente.EstadoBPMN, Integer> mapa = controller.snapshotEstadosCliente();
        
        modeloCliente.setRowCount(0);
        for (Cliente.EstadoBPMN estado : Cliente.EstadoBPMN.values()) {
            int count = mapa.getOrDefault(estado, 0);
            modeloCliente.addRow(new Object[]{estado.name(), count});
        }
    }
    
    private void actualizarLimpieza() {
        Map<Limpieza.EstadoBPMN, Integer> mapa = controller.snapshotEstadosLimpieza();
        
        modeloLimpieza.setRowCount(0);
        for (Limpieza.EstadoBPMN estado : Limpieza.EstadoBPMN.values()) {
            int count = mapa.getOrDefault(estado, 0);
            modeloLimpieza.addRow(new Object[]{estado.name(), count});
        }
    }
    
    private void actualizarInstructor() {
        Map<Instructor.EstadoBPMN, Integer> mapa = controller.snapshotEstadosInstructor();
        
        modeloInstructor.setRowCount(0);
        for (Instructor.EstadoBPMN estado : Instructor.EstadoBPMN.values()) {
            int count = mapa.getOrDefault(estado, 0);
            modeloInstructor.addRow(new Object[]{estado.name(), count});
        }
    }
    
    private void actualizarRecepcionista() {
        Map<Recepcionista.EstadoBPMN, Integer> mapa = controller.snapshotEstadosRecepcionista();
        
        modeloRecepcionista.setRowCount(0);
        for (Recepcionista.EstadoBPMN estado : Recepcionista.EstadoBPMN.values()) {
            int count = mapa.getOrDefault(estado, 0);
            modeloRecepcionista.addRow(new Object[]{estado.name(), count});
        }
    }
    
    public void detener() {
        if (timer != null) {
            timer.stop();
        }
    }
}