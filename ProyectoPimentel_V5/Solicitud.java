public class Solicitud {
    public final Cliente cliente;
    public volatile Boolean respuesta = null;

    public Solicitud(Cliente cliente) {
        this.cliente = cliente;
    }
}
