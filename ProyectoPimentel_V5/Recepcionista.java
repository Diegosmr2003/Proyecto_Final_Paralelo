import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Recepcionista extends Thread {

    public enum EstadoBPMN {
        INICIANDO,
        ESPERANDO_SOLICITUD,
        PROCESANDO_SOLICITUD,
        CONSULTANDO_SERVICIO_REMOTO,
        ENVIANDO_RESPUESTA,
        FINALIZADO
    }

    private final RemoteAuthService remote;
    private final BlockingQueue<Solicitud> cola = new LinkedBlockingQueue<>();
    private volatile boolean seguir = true;
    private volatile EstadoBPMN estadoActual = EstadoBPMN.INICIANDO;

    public Recepcionista(int id, RemoteAuthService remote) {
        super("Recepcionista-" + id);
        this.remote = remote;
    }

    public EstadoBPMN getEstado() { return estadoActual; }
    
    private void setEstado(EstadoBPMN e) { estadoActual = e; }


    public boolean autorizarCliente(Cliente c) throws InterruptedException {

        Solicitud sol = new Solicitud(c);

        cola.put(sol);

        synchronized (sol) {
            while (sol.respuesta == null) {
                sol.wait();
            }
        }

        return sol.respuesta;
    }


    @Override
    public void run() {

        try {
            setEstado(EstadoBPMN.INICIANDO);
            
            while (seguir) {

                setEstado(EstadoBPMN.ESPERANDO_SOLICITUD);
                Solicitud sol = cola.take();

                setEstado(EstadoBPMN.PROCESANDO_SOLICITUD);
                boolean autorizado;

                try {
                    setEstado(EstadoBPMN.CONSULTANDO_SERVICIO_REMOTO);
                    autorizado = remote.solicitarAutorizacion(sol.cliente.getName());
                } catch (Exception e) {
                    Log.log(getName() + " ERROR remoto → denegando por seguridad");
                    autorizado = false;
                }

                Log.log(getName() + " recibe respuesta remota: " +
                        (autorizado ? "AUTORIZADO" : "DENEGADO") +
                        " para " + sol.cliente.getName());

                setEstado(EstadoBPMN.ENVIANDO_RESPUESTA);
                synchronized (sol) {
                    sol.respuesta = autorizado;
                    sol.notify();
                }
            }

        } catch (InterruptedException e) {
        }

        setEstado(EstadoBPMN.FINALIZADO);
        Log.log(getName() + " termina su trabajo.");
    }

    public void detener() {
        seguir = false;
        this.interrupt();
    }
}