public class Buffer {
    private String nombre;
    private int capacidad;
    private int cantidad;
    private boolean cerrado = false;

    public Buffer(String nombre, int capacidad, int inicial) {
        this.nombre = nombre;
        this.capacidad = capacidad;
        this.cantidad = inicial;
    }

    public synchronized void cerrar() {
        cerrado = true;
        notifyAll();
    }

    public synchronized void producir(int n, String quien) throws InterruptedException {
        while (!cerrado && (cantidad + n > capacidad)) {
            Log.log(quien + " espera: " + nombre + " lleno (" + cantidad + "/" + capacidad + ")");
            wait();
        }

        if (cerrado) {
            return;
        }

        cantidad += n;
        Log.log(quien + " produjo " + n + "  " + nombre + "=" + cantidad + "/" + capacidad);
        notifyAll();
    }

    public synchronized void consumir(int n, String quien) throws InterruptedException {
        while (!cerrado && (cantidad < n)) {
            Log.log(quien + " espera: " + nombre + " vacio (" + cantidad + "/" + capacidad + ")");
            wait();
        }

        if (cerrado) {
            return;
        }

        cantidad -= n;
        Log.log(quien + " consumio " + n + "  " + nombre + "=" + cantidad + "/" + capacidad);
        notifyAll();
    }

    public synchronized int getCantidad() {
        return cantidad;
    }

    public int getCapacidad() {
        return capacidad;
    }

    public String getNombre() {
        return nombre;
    }

    public synchronized boolean isCerrado() {
        return cerrado;
    }
}
