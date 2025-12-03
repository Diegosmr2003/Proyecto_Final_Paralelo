import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class RemoteAuthService {

    private final String endpoint;

    public RemoteAuthService(String endpoint) {
        this.endpoint = endpoint; // https://tu-servicio.run.app/autorizar
    }

    public boolean solicitarAutorizacion(String nombreCliente) throws IOException {

        URL url = new URL(endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");

        // Enviar JSON
        String json = "{ \"cliente\": \"" + nombreCliente + "\" }";

        try (OutputStream os = conn.getOutputStream()) {
            os.write(json.getBytes());
        }

        int status = conn.getResponseCode();
        if (status != 200) {
            throw new IOException("HTTP error: " + status);
        }

        // Leer respuesta JSON
        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
        }

        conn.disconnect();

        // Procesar JSON simple
        String res = response.toString().toLowerCase();

        return res.contains("true");  
    }
}
