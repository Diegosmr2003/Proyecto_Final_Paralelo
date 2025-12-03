public class ClaseGrupo {

    private final int capacidad;
    private final int duracionMs;

    private int esperando = 0;
    private boolean claseEnCurso = false;
    private boolean cerrar = false;

    private boolean noHabraMasClientes = false;

    public ClaseGrupo(int capacidad, int duracionMs) {
        this.capacidad = capacidad;
        this.duracionMs = duracionMs;
    }

    // ===== GETTERS PÚBLICOS =====
    public int getCapacidad() { return capacidad; }
    public int getDuracionMs() { return duracionMs; }
    public int getEsperando() { return esperando; }
    public boolean isClaseEnCurso() { return claseEnCurso; }
    public boolean isCerrar() { return cerrar; }
    public boolean isNoHabraMasClientes() { return noHabraMasClientes; }

    // ===== SETTERS PACKAGE-PRIVATE (para Instructor) =====
    void setEsperando(int e) { esperando = e; }
    void setClaseEnCurso(boolean c) { claseEnCurso = c; }

    public synchronized void cancelar() {
        cerrar = true;
        notifyAll();
    }

    public synchronized void noHabraMasClientes() {
        noHabraMasClientes = true;
        notifyAll();
    }

    public boolean unirseYEsperarInicio(String nombreCliente) throws InterruptedException {
        synchronized (this) {

            if (cerrar)
                return false;

            while (claseEnCurso && !cerrar) {
                Log.log(nombreCliente + " espera a que termine la clase actual");
                wait();
                if (cerrar)
                    return false;
            }

            if (cerrar)
                return false;

            esperando++;
            Log.log(nombreCliente + " se inscribe a la clase (" + esperando + "/" + capacidad + ")");

            if (esperando == capacidad || noHabraMasClientes)
                notifyAll();

            while (!claseEnCurso && !cerrar) {
                wait();
                if (cerrar)
                    return false;
            }

            if (cerrar)
                return false;

            return true;
        }
    }

    public void esperarFinClase(String nombreCliente) throws InterruptedException {
        synchronized (this) {
            while (claseEnCurso && !cerrar) {
                wait();
            }
            if (!cerrar) {
                Log.log(nombreCliente + " termina la clase grupal");
            }
        }
    }

}