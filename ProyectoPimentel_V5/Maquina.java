public class Maquina {
    private String nombre;
    private int usoMs;
    private boolean ocupada = false;

    public Maquina(String nombre, int usoMs) { 
        this.nombre = nombre; 
        this.usoMs = usoMs;
    }

    public synchronized void usar(String cliente) throws InterruptedException {
        while (ocupada) {
            wait();
        }
        ocupada = true;
        Log.log(cliente + " comenzo en maquina " + nombre);
        try {
            Thread.sleep(usoMs);
        } finally {
            Log.log(cliente + " terminó en maquina " + nombre);
            ocupada = false;
            notifyAll();
        }
    }
}
