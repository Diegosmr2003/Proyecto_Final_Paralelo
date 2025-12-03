public class Torniquete {
    private int pasoMs;

    public Torniquete(int pasoMs) {
        this.pasoMs = pasoMs;
    }

    public synchronized void entrar(String cliente) throws InterruptedException {
        Log.log(cliente + " pasa por torniquete...");
        Thread.sleep(pasoMs);
        Log.log(cliente + " entro al gym");
    }
}
