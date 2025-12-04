import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CreditsPanel extends JFrame {
    
    public CreditsPanel() {
        super("Créditos - ParaGym");
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setSize(600, 500);
        setResizable(false);
        
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        
        JPanel logoPanel = new JPanel();
        logoPanel.setBackground(Color.WHITE);
        try {
            ImageIcon logoIcon = new ImageIcon("Images/LogoUP.png");
            Image scaledLogo = logoIcon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
            JLabel logoLabel = new JLabel(new ImageIcon(scaledLogo));
            logoPanel.add(logoLabel);
        } catch (Exception e) {
            JLabel logoPlaceholder = new JLabel("UNIVERSIDAD PANAMERICANA");
            logoPlaceholder.setFont(new Font("Arial", Font.BOLD, 14));
            logoPlaceholder.setForeground(new Color(0, 32, 96));
            logoPanel.add(logoPlaceholder);
        }
        
        JLabel titleLabel = new JLabel("ParaGym - Simulación de Gimnasio", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(new Color(0, 32, 96));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JSeparator separator1 = new JSeparator();
        separator1.setMaximumSize(new Dimension(500, 2));
        
        JLabel materiaLabel = new JLabel("Materia:", SwingConstants.LEFT);
        materiaLabel.setFont(new Font("Arial", Font.BOLD, 14));
        materiaLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel materiaNombre = new JLabel("Fundamentos de Programación en Paralelo", SwingConstants.LEFT);
        materiaNombre.setFont(new Font("Arial", Font.PLAIN, 13));
        materiaNombre.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel profesorLabel = new JLabel("Profesor:", SwingConstants.LEFT);
        profesorLabel.setFont(new Font("Arial", Font.BOLD, 14));
        profesorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel profesorNombre = new JLabel("Dr. Juan Carlos López Pimentel", SwingConstants.LEFT);
        profesorNombre.setFont(new Font("Arial", Font.PLAIN, 13));
        profesorNombre.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JSeparator separator2 = new JSeparator();
        separator2.setMaximumSize(new Dimension(500, 2));
        
        JLabel estudiantesLabel = new JLabel("Equipo de desarrollo:", SwingConstants.LEFT);
        estudiantesLabel.setFont(new Font("Arial", Font.BOLD, 14));
        estudiantesLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        String[] estudiantes = {
            "Jesus Abel Gutierrez Calvillo",
            "Diego Sebastian Montoya Rodriguez",
            "Jose Bernardo Sandoval"
        };
        
        JPanel estudiantesPanel = new JPanel();
        estudiantesPanel.setLayout(new BoxLayout(estudiantesPanel, BoxLayout.Y_AXIS));
        estudiantesPanel.setBackground(Color.WHITE);
        
        for (String estudiante : estudiantes) {
            JLabel estLabel = new JLabel("• " + estudiante);
            estLabel.setFont(new Font("Arial", Font.PLAIN, 13));
            estLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            estudiantesPanel.add(estLabel);
            estudiantesPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        }
        
        JSeparator separator3 = new JSeparator();
        separator3.setMaximumSize(new Dimension(500, 2));
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd 'de' MMMM 'de' yyyy");
        String fechaActual = sdf.format(new Date());
        
        JLabel fechaLabel = new JLabel("Fecha: " + fechaActual, SwingConstants.CENTER);
        fechaLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        fechaLabel.setForeground(Color.GRAY);
        fechaLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        mainPanel.add(logoPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        mainPanel.add(separator1);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        mainPanel.add(materiaLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        mainPanel.add(materiaNombre);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        mainPanel.add(profesorLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        mainPanel.add(profesorNombre);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        mainPanel.add(separator2);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        mainPanel.add(estudiantesLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(estudiantesPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        mainPanel.add(separator3);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(fechaLabel);
        
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        getContentPane().add(scrollPane);
        
        setLocationRelativeTo(null);
    }
}