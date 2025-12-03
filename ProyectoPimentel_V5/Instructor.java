public class Instructor extends Thread {
    
    public enum EstadoBPMN {
        INICIANDO,
        ESPERANDO_ALUMNOS,
        IMPARTIENDO_CLASE,
        FINALIZANDO_CLASE,
        FINALIZADO
    }
    
    private final ClaseGrupo clase;
    private volatile EstadoBPMN estadoActual = EstadoBPMN.INICIANDO;

    public Instructor(ClaseGrupo clase, int id) {
        super("Instructor-" + id);
        this.clase = clase;
    }

    public EstadoBPMN getEstado() { return estadoActual; }
    
    private void setEstado(EstadoBPMN e) { estadoActual = e; }

    @Override
    public void run() {
        try {
            setEstado(EstadoBPMN.INICIANDO);
            cicloInstructor();
        } catch (InterruptedException e) {
            Log.log(getName() + " interrumpido");
        }
        setEstado(EstadoBPMN.FINALIZADO);
    }
    
    private void cicloInstructor() throws InterruptedException {
        while (true) {

            synchronized (clase) {
                setEstado(EstadoBPMN.ESPERANDO_ALUMNOS);
                
                long limiteEspera = System.currentTimeMillis() + 5000;

                while (clase.getEsperando() < clase.getCapacidad() && 
                       !clase.isCerrar() && 
                       !clase.isNoHabraMasClientes()) {

                    long tiempoRestante = limiteEspera - System.currentTimeMillis();

                    if (tiempoRestante <= 0) {
                        Log.log(getName() + " inicia la clase por timeout con " + 
                                clase.getEsperando() + " clientes.");
                        break;
                    }

                    Log.log(getName() + " espera a que se llene la clase (" +
                            clase.getEsperando() + "/" + clase.getCapacidad() + 
                            "), quedan " + tiempoRestante + "ms");

                    clase.wait(tiempoRestante);
                }

                if (clase.isCerrar() || clase.isNoHabraMasClientes()) {
                    Log.log(getName() + " cancela sus clases");
                    return;
                }

                if (clase.getEsperando() == 0) {
                    Log.log(getName() + " no inicia clase (no hay alumnos).");
                    continue;
                }

                clase.setClaseEnCurso(true);
                Log.log(getName() + " inicia la clase con " + clase.getEsperando() + " clientes");
                clase.notifyAll();
            }

            setEstado(EstadoBPMN.IMPARTIENDO_CLASE);
            Thread.sleep(clase.getDuracionMs());

            synchronized (clase) {
                setEstado(EstadoBPMN.FINALIZANDO_CLASE);
                clase.setClaseEnCurso(false);
                clase.setEsperando(0);
                Log.log(getName() + " termina la clase");
                clase.notifyAll();
            }
        }
    }

    public void detener() {
        clase.cancelar();
        interrupt();
    }
}