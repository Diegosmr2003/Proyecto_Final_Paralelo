public class Params {
    // Cantidades
    public final int numClientes;
    public final int numLimpieza;
    public final int toallasMax;
    public final int duchasTotales;
    public final int toallasIniciales;

    // Tiempos
    public final int pasoTorniqueteMs;
    public final int usoMaquinaMs;
    public final int duchaMs;
    public final int limpiezaDuchaMs;
    public final int pausaLimpiezaMs;
    public final int interLlegadaMs;

    // Producción
    public final int toallasPorLote;

    public Params(int numClientes, int numLimpieza,
                  int toallasMax, int duchasTotales, int toallasIniciales,
                  int pasoTorniqueteMs, int usoMaquinaMs, int duchaMs,
                  int limpiezaDuchaMs, int pausaLimpiezaMs, int interLlegadaMs,
                  int toallasPorLote) {

        this.numClientes = numClientes;
        this.numLimpieza = numLimpieza;
        this.toallasMax = toallasMax;
        this.duchasTotales = duchasTotales;
        this.toallasIniciales = Math.min(toallasIniciales, toallasMax);

        this.pasoTorniqueteMs = pasoTorniqueteMs;
        this.usoMaquinaMs = usoMaquinaMs;
        this.duchaMs = duchaMs;
        this.limpiezaDuchaMs = limpiezaDuchaMs;
        this.pausaLimpiezaMs = pausaLimpiezaMs;
        this.interLlegadaMs = interLlegadaMs;

        this.toallasPorLote = toallasPorLote;
    }
}
