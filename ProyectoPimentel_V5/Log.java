import java.util.function.Consumer;
import javax.swing.SwingUtilities;

public class Log {
        private static Consumer<String> salida = null;

    public static synchronized void setSalida(Consumer<String> consumer) {
        salida = consumer;
    }

    public static void log(String msg) {
        if (salida != null) {
            SwingUtilities.invokeLater(() -> salida.accept(msg));
        } else {
            System.out.println(msg);
        }
    }
}