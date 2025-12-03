public class Cliente extends Thread {

    public enum EstadoBPMN {
        LLEGANDO,
        EN_RECEPCION,
        ACCESO_DENEGADO,
        TORNIQUETE,
        EN_GIMNASIO,
        EN_MAQUINA,
        MAQUINA_TERMINADA,
        TOMANDO_TOALLA,
        TOMANDO_DUCHA,
        EN_DUCHA,
        DUCHA_TERMINADA,
        ESPERANDO_CLASE,
        EN_CLASE,
        CLASE_TERMINADA,
        SALIENDO
    }

    private final int id;
    private final Recepcionista recepcionista;
    private final Torniquete torniquete;
    private final Maquina[] maquinas;
    private final Buffer toallas;
    private final Buffer duchasLibres;
    private final Buffer duchasSucias;
    private final int duchaMs;

    private final ClaseGrupo claseGrupo;
    
    private final boolean quiereUsarMaquina;
    private final boolean quiereDucha;
    private final boolean quiereClase;
    private final int vecesUsarMaquina; 

    private final java.util.Random rng = new java.util.Random();

    private volatile EstadoBPMN estadoActual = null;

    private boolean claseTomada = false;

    private static GymCanvas gymCanvasRef = null;

    public static void setCanvas(GymCanvas canvas) {
        gymCanvasRef = canvas;
    }

    public Cliente(int id,
                   Recepcionista recepcionista,
                   Torniquete torniquete,
                   Maquina[] maquinas,
                   Buffer to,
                   Buffer duchasLibres,
                   Buffer duchasSucias,
                   int duchaMs,
                   ClaseGrupo claseGrupo,
                   boolean participaEnClase) {

        super("Cliente-" + id);

        this.id = id;
        this.recepcionista = recepcionista;
        this.torniquete = torniquete;
        this.maquinas = maquinas;
        this.toallas = to;
        this.duchasLibres = duchasLibres;
        this.duchasSucias = duchasSucias;
        this.duchaMs = duchaMs;
        this.claseGrupo = claseGrupo;

        this.estadoActual = EstadoBPMN.LLEGANDO;

        // ===== GENERAR PREFERENCIAS ALEATORIAS =====
        // 80% de probabilidad de querer usar máquina
        this.quiereUsarMaquina = rng.nextDouble() < 0.8;
        
        // 70% de probabilidad de querer ducharse
        this.quiereDucha = rng.nextDouble() < 0.7;
        
        // Usar el parametro de participación en clase (puede ser aleatorio o definido)
        this.quiereClase = participaEnClase;
        
        // Si quiere usar maquina, decide cuántas veces (1, 2 o 3)
        this.vecesUsarMaquina = quiereUsarMaquina ? (rng.nextInt(3) + 1) : 0;

        Log.log(getName() + " PREFERENCIAS → Máquina:" + (quiereUsarMaquina ? vecesUsarMaquina + "x" : "NO") + 
                ", Ducha:" + (quiereDucha ? "SÍ" : "NO") + 
                ", Clase:" + (quiereClase ? "SÍ" : "NO"));
    }

    public EstadoBPMN getEstado() { return estadoActual; }

    private void setEstado(EstadoBPMN e) { estadoActual = e; }

    private void removerDelCanvas() {
        if (gymCanvasRef != null)
            gymCanvasRef.removerEntidad(getName());
    }

    @Override
    public void run() {
        try {

            setEstado(EstadoBPMN.LLEGANDO);

            // ===== RECEPCIÓN =====
            setEstado(EstadoBPMN.EN_RECEPCION);
            boolean autorizado = recepcionista.autorizarCliente(this);
            if (!autorizado) {
                setEstado(EstadoBPMN.ACCESO_DENEGADO);
                Log.log(getName() + " se retira: acceso denegado en recepción");
                removerDelCanvas();
                return;
            }

            // ===== TORNIQUETE =====
            setEstado(EstadoBPMN.TORNIQUETE);
            torniquete.entrar(getName());

            setEstado(EstadoBPMN.EN_GIMNASIO);

            if (quiereUsarMaquina) {
                for (int i = 0; i < vecesUsarMaquina; i++) {
                    setEstado(EstadoBPMN.EN_MAQUINA);
                    Maquina m = maquinas[rng.nextInt(maquinas.length)];
                    m.usar(getName());
                    setEstado(EstadoBPMN.MAQUINA_TERMINADA);
                    
                    if (i < vecesUsarMaquina - 1) {
                        Thread.sleep(200);
                    }
                }
            } else {
                Log.log(getName() + " decide NO usar máquinas y pasa directo");
            }

            if (quiereDucha) {
                setEstado(EstadoBPMN.TOMANDO_TOALLA);
                toallas.consumir(1, getName());

                setEstado(EstadoBPMN.TOMANDO_DUCHA);
                duchasLibres.consumir(1, getName());

                setEstado(EstadoBPMN.EN_DUCHA);
                Thread.sleep(duchaMs);

                setEstado(EstadoBPMN.DUCHA_TERMINADA);
                duchasSucias.producir(1, getName());
            } else {
                Log.log(getName() + " decide NO ducharse");
            }

            if (claseGrupo != null && quiereClase && !claseTomada) {

                setEstado(EstadoBPMN.ESPERANDO_CLASE);
                boolean entro = claseGrupo.unirseYEsperarInicio(getName());

                if (entro) {
                    setEstado(EstadoBPMN.EN_CLASE);
                    claseGrupo.esperarFinClase(getName());
                    setEstado(EstadoBPMN.CLASE_TERMINADA);

                    claseTomada = true;
                }
            } else if (!quiereClase) {
                Log.log(getName() + " decide NO tomar clase grupal");
            }

            setEstado(EstadoBPMN.SALIENDO);
            Log.log(getName() + " sale del gimnasio");
            removerDelCanvas();

        } catch (InterruptedException e) {
            removerDelCanvas();
        }
    }
}