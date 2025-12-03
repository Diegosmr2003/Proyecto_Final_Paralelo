public class Limpieza extends Thread {

    public enum EstadoBPMN {
        INICIANDO,
        ESPERANDO_DUCHAS_SUCIAS,
        LIMPIANDO_DUCHA,
        PRODUCIENDO_DUCHA_LIBRE,
        PRODUCIENDO_TOALLAS,
        PAUSANDO,
        FINALIZADO
    }

    private final Buffer toallas;
    private final Buffer duchasLibres;
    private final Buffer duchasSucias;
    private final int toallasPorLote;
    private final int limpiezaDuchaMs;
    private final int pausaLimpiezaMs;

    private volatile boolean seguir = true;
    private volatile EstadoBPMN estadoActual = EstadoBPMN.INICIANDO;

    public Limpieza(Buffer toallas,
                    Buffer duchasLibres,
                    Buffer duchasSucias,
                    int id,
                    int toallasPorLote,
                    int limpiezaDuchaMs,
                    int pausaLimpiezaMs) {
        super("Limpieza-" + id);
        this.toallas = toallas;
        this.duchasLibres = duchasLibres;
        this.duchasSucias = duchasSucias;
        this.toallasPorLote = toallasPorLote;
        this.limpiezaDuchaMs = limpiezaDuchaMs;
        this.pausaLimpiezaMs = pausaLimpiezaMs;
    }

    public EstadoBPMN getEstado() { return estadoActual; }
    
    private void setEstado(EstadoBPMN e) { estadoActual = e; }

    public void parar() {
        seguir = false;
        interrupt();
    }

    @Override
    public void run() {
        try {
            setEstado(EstadoBPMN.INICIANDO);
            
            while (seguir) {

                if (toallas.isCerrado() || duchasLibres.isCerrado() || duchasSucias.isCerrado()) {
                    break;
                }

                boolean hizoAlgo = false;

                if (duchasSucias.getCantidad() > 0) {
                    try {
                        setEstado(EstadoBPMN.ESPERANDO_DUCHAS_SUCIAS);
                        duchasSucias.consumir(1, getName());
                        
                        setEstado(EstadoBPMN.LIMPIANDO_DUCHA);
                        Thread.sleep(limpiezaDuchaMs);

                        setEstado(EstadoBPMN.PRODUCIENDO_DUCHA_LIBRE);
                        duchasLibres.producir(1, getName());
                        hizoAlgo = true;
                    } catch (InterruptedException ex) {
                        break;
                    }
                }

                if (toallas.getCapacidad() - toallas.getCantidad() >= toallasPorLote) {
                    try {
                        setEstado(EstadoBPMN.PRODUCIENDO_TOALLAS);
                        toallas.producir(toallasPorLote, getName());
                        hizoAlgo = true;
                    } catch (InterruptedException ex) {
                        break;
                    }
                }
                
                if (!hizoAlgo || pausaLimpiezaMs > 0) {
                    setEstado(EstadoBPMN.PAUSANDO);
                    Thread.sleep(pausaLimpiezaMs > 0 ? pausaLimpiezaMs : 50);
                }
            }
        } catch (InterruptedException e) {
            Log.log(getName() + " interrumpido.");
        }

        setEstado(EstadoBPMN.FINALIZADO);
        Log.log(getName() + " termina correctamente.");
    }
}