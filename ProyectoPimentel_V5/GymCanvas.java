import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

public class GymCanvas extends JPanel implements Runnable {

    private Image fondoGimnasio;

    private Image imgCliente;
    private Image imgLimpieza;
    private Image imgInstructor;
    private Image imgRecepcionistaIzq;
    private Image imgRecepcionistaDer;

    private Thread animacion;
    private boolean ejecutando = false;

    private final List<EntidadVisual> entidades = new CopyOnWriteArrayList<>();
    private final Random rnd = new Random();

    private boolean recepcionistaVolteandoDerecha = true;
    private long ultimoCambioImagenRecepcionista = 0;
    private int recepcionistaOffset = 0;
    private boolean recepcionistaMoviendoDerecha = true;

    private int instructorOffset = 0;
    private boolean instructorMoviendoDerecha = true;


    public GymCanvas() {
        try {
            fondoGimnasio = new ImageIcon("Images/Gym.png").getImage();

            imgCliente       = new ImageIcon("Images/Flaco.png").getImage();
            imgLimpieza      = new ImageIcon("Images/Limpieza.png").getImage();
            imgInstructor    = new ImageIcon("Images/Instructor.png").getImage();
            imgRecepcionistaIzq = new ImageIcon("Images/PersonaRecepcion.png").getImage();
            imgRecepcionistaDer = new ImageIcon("Images/PersonaRecepcionDerecha.png").getImage();
        } catch (Exception e) {
            System.err.println("Error cargando imágenes: " + e.getMessage());
        }

        setPreferredSize(new Dimension(720, 600));
        setDoubleBuffered(true);
    }

    public void setCuadranteEntidad(String nombre, Rect nuevoCuadrante) {
        for (EntidadVisual e : entidades) {
            if (e.nombre.equals(nombre)) {
                e.cuadrante = nuevoCuadrante;
                return;
            }
        }
    }


    public void removerEntidad(String nombre) {
        entidades.removeIf(e -> e.nombre.equals(nombre));
        repaint();
    }


    public void cambiarImagen(String nombre, Image nuevaImagen) {
        for (EntidadVisual e : entidades) {
            if (e.nombre.equals(nombre)) {
                e.imagenPersonalizada = nuevaImagen;  
                repaint();
                return;
            }
        }
    }

    public void agregarEntidad(String nombre, int x, int y, Color color, Rect cuadrante) {
        entidades.add(new EntidadVisual(nombre, x, y, color, cuadrante));
        repaint();
    }

    public void moverEntidad(String nombre, int x, int y) {
        for (EntidadVisual e : entidades) {
            if (e.nombre.equals(nombre)) {
                e.x = x;
                e.y = y;
                break;
            }
        }
        repaint();
    }



    public void iniciar() {
        if (animacion == null) {
            ejecutando = true;
            animacion = new Thread(this);
            animacion.start();
        }
    }

    public void detener() {
        ejecutando = false;
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        g2.drawImage(fondoGimnasio, 0, 0, getWidth(), getHeight(), this);

        for (EntidadVisual e : entidades) {
            Image img = null;

            if (e.nombre.startsWith("Cliente"))
                img = imgCliente;
            else if (e.nombre.startsWith("Limpieza"))
                img = imgLimpieza;
            else if (e.nombre.startsWith("Instructor"))
                img = imgInstructor;
            else if (e.nombre.startsWith("Recepcionista"))
                img = recepcionistaVolteandoDerecha ? imgRecepcionistaDer : imgRecepcionistaIzq;


            Image imgFinal = (e.imagenPersonalizada != null ? e.imagenPersonalizada : img);

            g2.drawImage(imgFinal, e.x, e.y, 65, 65, this);

            g2.setColor(Color.WHITE);
            g2.drawString(e.nombre, e.x, e.y - 5);
        }
    }


    @Override
    public void run() {
        while (ejecutando) {

            long ahora = System.currentTimeMillis();

            if (ahora - ultimoCambioImagenRecepcionista > 700) {
                recepcionistaVolteandoDerecha = !recepcionistaVolteandoDerecha;
                ultimoCambioImagenRecepcionista = ahora;
            }

            for (EntidadVisual e : entidades) {
                if (e.nombre.startsWith("Recepcionista")) {
                    if (recepcionistaMoviendoDerecha) {
                        recepcionistaOffset++;
                        if (recepcionistaOffset > 12) recepcionistaMoviendoDerecha = false;
                    } else {
                        recepcionistaOffset--;
                        if (recepcionistaOffset < -12) recepcionistaMoviendoDerecha = true;
                    }
                    e.x = e.baseX + recepcionistaOffset;
                }
            }


            for (EntidadVisual e : entidades) {
                if (!e.nombre.startsWith("Instructor")) continue;

                if (instructorMoviendoDerecha) {
                    instructorOffset++;
                    if (instructorOffset > 18) instructorMoviendoDerecha = false;
                } else {
                    instructorOffset--;
                    if (instructorOffset < -18) instructorMoviendoDerecha = true;
                }

                e.x = e.baseX + instructorOffset;
            }


            for (EntidadVisual e : entidades) {

                if (e.nombre.startsWith("Instructor")) continue;
                if (e.nombre.startsWith("Recepcionista")) continue;

                int dx = rnd.nextInt(13) - 6;
                int dy = rnd.nextInt(13) - 6;

                e.x += dx;
                e.y += dy;

                if (e.x < e.cuadrante.minX) e.x = e.cuadrante.minX;
                if (e.x > e.cuadrante.maxX) e.x = e.cuadrante.maxX;
                if (e.y < e.cuadrante.minY) e.y = e.cuadrante.minY;
                if (e.y > e.cuadrante.maxY) e.y = e.cuadrante.maxY;
            }

            repaint();

            try { Thread.sleep(40); }
            catch (InterruptedException e) {}
        }
    }



    static class EntidadVisual {
        String nombre;
        int x, y;
        int baseX, baseY;
        Color color;
        Rect cuadrante;

        Image imagenPersonalizada = null;

        EntidadVisual(String nombre, int x, int y, Color color, Rect cuadrante) {
            this.nombre = nombre;
            this.x = this.baseX = x;
            this.y = this.baseY = y;
            this.color = color;
            this.cuadrante = cuadrante;
        }
    }

    static class Rect {
        int minX, maxX, minY, maxY;
        Rect(int minX, int maxX, int minY, int maxY) {
            this.minX = minX;
            this.maxX = maxX;
            this.minY = minY;
            this.maxY = maxY;
        }
    }
}
