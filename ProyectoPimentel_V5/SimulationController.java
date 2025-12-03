import java.awt.*;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import javax.swing.ImageIcon;


public class SimulationController {

    private final Params P;

    public Buffer bufToallas, bufDuchasLibres, bufDuchasSucias;
    public Torniquete torniquete;
    public Maquina[] maquinas;

    ClaseGrupo claseGrupo;
    Instructor instructor;
    Recepcionista recepcionista;

    List<Limpieza> hsLimpieza;
    List<Cliente> hsClientes;

    private Thread hiloSimulacion;
    private volatile boolean corriendo = false;

    private GymCanvas gymCanvas;

    private static final GymCanvas.Rect CUADRANTE_DUCHAS      = new GymCanvas.Rect(0,   360, 0,   360);
    private static final GymCanvas.Rect CUADRANTE_MAQUINAS    = new GymCanvas.Rect(360, 720, 0,   360);
    private static final GymCanvas.Rect CUADRANTE_CLASE       = new GymCanvas.Rect(0,   360, 360, 470);
    private static final GymCanvas.Rect CUADRANTE_RECEPCION   = new GymCanvas.Rect(450, 720, 420, 720);

    private static final int[] ZONA_TORNIQUETE    = {560, 440};
    private static final int[] ZONA_MAQUINAS      = {500, 160};
    private static final int[] ZONA_TOALLAS       = {160, 200};
    private static final int[] ZONA_DUCHAS        = {160, 100};
    private static final int[] ZONA_SALIDA        = {520, 650};
    private static final int[] ZONA_INSTRUCTOR    = {120, 400};
    private static final int[] ZONA_RECEPCIONISTA = {550, 350};
    private static final int[] ZONA_CLASE         = {150, 600};

    public SimulationController(Params p) {
        this.P = p;
    }

    public void setGymCanvas(GymCanvas canvas) {
        this.gymCanvas = canvas;
        Cliente.setCanvas(canvas);
    }

    public boolean isCorriendo() { return corriendo; }
    public Params getParams() { return P; }

    public void iniciar() {
        if (corriendo) return;
        corriendo = true;

        hiloSimulacion = new Thread(() -> {

            try {
                Log.log("== INICIO SIMULACIÓN ==");

                bufToallas      = new Buffer("Toallas",      P.toallasMax,   P.toallasIniciales);
                bufDuchasLibres = new Buffer("DuchasLibres", P.duchasTotales, P.duchasTotales);
                bufDuchasSucias = new Buffer("DuchasSucias", P.duchasTotales, 0);

                torniquete = new Torniquete(P.pasoTorniqueteMs);
                maquinas = new Maquina[]{
                        new Maquina("Caminadora-01", P.usoMaquinaMs),
                        new Maquina("Bicicleta-01",  P.usoMaquinaMs),
                        new Maquina("Eliptica-01",   P.usoMaquinaMs)
                };

                RemoteAuthService remote = new RemoteAuthService(
                        "https://autorizacion-gym-final-d7dcb7c8edgwdreg.westus3-01.azurewebsites.net/api/Autorizacion"
                );
                recepcionista = new Recepcionista(0, remote);
                recepcionista.start();

                if (gymCanvas != null) {
                    gymCanvas.agregarEntidad(
                            "Recepcionista-0",
                            ZONA_RECEPCIONISTA[0],
                            ZONA_RECEPCIONISTA[1],
                            Color.MAGENTA,
                            CUADRANTE_RECEPCION
                    );
                }

                int capacidadClase = 5;
                int duracionClaseMs = 5000;

                claseGrupo = new ClaseGrupo(capacidadClase, duracionClaseMs);
                instructor = new Instructor(claseGrupo, 0);
                instructor.start();

                if (gymCanvas != null) {
                    gymCanvas.agregarEntidad(
                            "Instructor-0",
                            ZONA_INSTRUCTOR[0],
                            ZONA_INSTRUCTOR[1],
                            Color.GREEN,
                            CUADRANTE_CLASE
                    );
                }

                hsLimpieza = new ArrayList<>();

                for (int i = 0; i < P.numLimpieza; i++) {

                    Limpieza l = new Limpieza(
                            bufToallas, bufDuchasLibres, bufDuchasSucias,
                            i, P.toallasPorLote, P.limpiezaDuchaMs, P.pausaLimpiezaMs
                    );

                    hsLimpieza.add(l);
                    l.start();

                    if (gymCanvas != null) {
                        gymCanvas.agregarEntidad(
                                "Limpieza-" + i,
                                ZONA_DUCHAS[0] + (int)(Math.random() * 40),
                                ZONA_DUCHAS[1] + (int)(Math.random() * 40),
                                Color.ORANGE,
                                CUADRANTE_DUCHAS
                        );
                    }
                }

                hsClientes = new ArrayList<>();

                int maxClase = (P.numClientes / capacidadClase) * capacidadClase;

                for (int i = 0; i < P.numClientes; i++) {

                    final int idFinal = i;
                    // Aleatorio: 60% probabilidad de querer clase
                    boolean participa = Math.random() < 0.6;

                    Cliente c = new Cliente(
                            idFinal, recepcionista, torniquete, maquinas,
                            bufToallas, bufDuchasLibres, bufDuchasSucias,
                            P.duchaMs, claseGrupo, participa
                    );

                    hsClientes.add(c);
                    c.start();

                    if (gymCanvas != null) {

                        gymCanvas.agregarEntidad(
                                "Cliente-" + idFinal,
                                ZONA_TORNIQUETE[0],
                                ZONA_TORNIQUETE[1],
                                Color.CYAN,
                                CUADRANTE_RECEPCION
                        );

                        final Cliente ref = c;

                        new Thread(() -> {

                            try {
                                Thread.sleep(P.pasoTorniqueteMs + 100);
                                if (ref.getState() == Thread.State.TERMINATED) return;

                                Cliente.EstadoBPMN estadoAnterior = null;

                                while (ref.getState() != Thread.State.TERMINATED) {
                                    Cliente.EstadoBPMN estadoActual = ref.getEstado();

                                    if (estadoActual != estadoAnterior) {
                                        switch (estadoActual) {
                                            case EN_MAQUINA:
                                                gymCanvas.moverEntidad("Cliente-" + idFinal,
                                                        ZONA_MAQUINAS[0], ZONA_MAQUINAS[1]);
                                                setCuadrante("Cliente-" + idFinal, CUADRANTE_MAQUINAS);
                                                break;

                                            case MAQUINA_TERMINADA:
                                                Image brazo50 = new ImageIcon("Images/Brazo50.png").getImage();
                                                gymCanvas.cambiarImagen("Cliente-" + idFinal, brazo50);
                                                break;

                                            case TOMANDO_TOALLA:
                                                gymCanvas.moverEntidad("Cliente-" + idFinal,
                                                        ZONA_TOALLAS[0], ZONA_TOALLAS[1]);
                                                setCuadrante("Cliente-" + idFinal, CUADRANTE_DUCHAS);
                                                break;

                                            case EN_DUCHA:
                                                gymCanvas.moverEntidad("Cliente-" + idFinal,
                                                        ZONA_DUCHAS[0], ZONA_DUCHAS[1]);
                                                setCuadrante("Cliente-" + idFinal, CUADRANTE_DUCHAS);
                                                break;

                                            case ESPERANDO_CLASE:
                                            case EN_CLASE:
                                                gymCanvas.moverEntidad("Cliente-" + idFinal,
                                                        ZONA_CLASE[0], ZONA_CLASE[1]);
                                                setCuadrante("Cliente-" + idFinal, CUADRANTE_CLASE);
                                                break;

                                            case SALIENDO:
                                                gymCanvas.moverEntidad("Cliente-" + idFinal,
                                                        ZONA_SALIDA[0], ZONA_SALIDA[1]);
                                                setCuadrante("Cliente-" + idFinal, CUADRANTE_RECEPCION);
                                                break;
                                        }
                                        estadoAnterior = estadoActual;
                                    }

                                    Thread.sleep(100); 
                                }

                            } catch (InterruptedException ignored) {}

                        }, "Animacion-Cliente-" + idFinal).start();
                    }

                    Thread.sleep(P.interLlegadaMs);
                }
                
                for (Cliente c : hsClientes) c.join();

                claseGrupo.noHabraMasClientes();

                bufToallas.cerrar();
                bufDuchasLibres.cerrar();
                bufDuchasSucias.cerrar();

                for (Limpieza l : hsLimpieza) l.parar();
                for (Limpieza l : hsLimpieza) {
                    try { l.join(); } catch (InterruptedException ignored) {}
                }

                instructor.detener();
                instructor.join();

                Log.log("FIN → Toallas      = " + bufToallas.getCantidad()      + "/" + bufToallas.getCapacidad());
                Log.log("FIN → DuchasLibres = " + bufDuchasLibres.getCantidad() + "/" + bufDuchasLibres.getCapacidad());
                Log.log("FIN → DuchasSucias = " + bufDuchasSucias.getCantidad() + "/" + bufDuchasSucias.getCapacidad());
                Log.log("== FIN SIMULACIÓN ==");
                

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                corriendo = false;
            }

        }, "SimController");

        hiloSimulacion.start();
    }

    public void detener() {
        try {
            if (bufToallas != null) bufToallas.cerrar();
            if (bufDuchasLibres != null) bufDuchasLibres.cerrar();
            if (bufDuchasSucias != null) bufDuchasSucias.cerrar();

            if(recepcionista != null) recepcionista.detener();

            if (claseGrupo != null) claseGrupo.cancelar();
            if (instructor != null) instructor.detener();

            if (hsLimpieza != null)
                for (Limpieza l : hsLimpieza) l.parar();

            if (hiloSimulacion != null)
                hiloSimulacion.interrupt();

        } catch (Exception ignored) {}
    }

    public synchronized List<Thread> snapshotAgentes() {
        List<Thread> out = new ArrayList<>();

        if (hsClientes != null) out.addAll(hsClientes);
        if (hsLimpieza != null) out.addAll(hsLimpieza);
        if (instructor != null) out.add(instructor);
        if (recepcionista != null) out.add(recepcionista);

        return out;
    }

    public synchronized Map<Cliente.EstadoBPMN, Integer> snapshotEstadosCliente() {
        Map<Cliente.EstadoBPMN, Integer> mapa = new EnumMap<>(Cliente.EstadoBPMN.class);

        if (hsClientes != null) {
            for (Cliente c : hsClientes) {
                if (c.getState() == Thread.State.TERMINATED) continue;
                Cliente.EstadoBPMN e = c.getEstado();
                if (e == null) continue;
                mapa.put(e, mapa.getOrDefault(e, 0) + 1);
            }
        }

        return mapa;
    }

    public synchronized Map<Limpieza.EstadoBPMN, Integer> snapshotEstadosLimpieza() {
        Map<Limpieza.EstadoBPMN, Integer> mapa = new EnumMap<>(Limpieza.EstadoBPMN.class);

        if (hsLimpieza != null) {
            for (Limpieza l : hsLimpieza) {
                if (l.getState() == Thread.State.TERMINATED) continue;
                Limpieza.EstadoBPMN e = l.getEstado();
                if (e == null) continue;
                mapa.put(e, mapa.getOrDefault(e, 0) + 1);
            }
        }

        return mapa;
    }

    public synchronized Map<Instructor.EstadoBPMN, Integer> snapshotEstadosInstructor() {
        Map<Instructor.EstadoBPMN, Integer> mapa = new EnumMap<>(Instructor.EstadoBPMN.class);

        if (instructor != null && instructor.getState() != Thread.State.TERMINATED) {
            Instructor.EstadoBPMN e = instructor.getEstado();
            if (e != null) {
                mapa.put(e, 1);
            }
        }

        return mapa;
    }

    public synchronized Map<Recepcionista.EstadoBPMN, Integer> snapshotEstadosRecepcionista() {
        Map<Recepcionista.EstadoBPMN, Integer> mapa = new EnumMap<>(Recepcionista.EstadoBPMN.class);

        if (recepcionista != null && recepcionista.getState() != Thread.State.TERMINATED) {
            Recepcionista.EstadoBPMN e = recepcionista.getEstado();
            if (e != null) {
                mapa.put(e, 1);
            }
        }

        return mapa;
    }

    private void setCuadrante(String nombre, GymCanvas.Rect cuadrante) {
        if (gymCanvas != null)
            gymCanvas.setCuadranteEntidad(nombre, cuadrante);
    }
}