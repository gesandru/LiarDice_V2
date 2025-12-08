package servidor;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class ServidorAdd {

    private final String nombre;
    private final int max;
    private final int puerto;

    private final List<Socket> jugadores = Collections.synchronizedList(new ArrayList<>());
    private final ExecutorService pool = Executors.newCachedThreadPool();

    public ServidorAdd(String n, int m, int p) {
        nombre = n;
        max = m;
        puerto = p;
    }

    public int getPuerto() {
        return puerto;
    }

    public void iniciar() {
        new Thread(() -> {
            try (ServerSocket ss = new ServerSocket(puerto)) {
                System.out.println(nombre + " escuchando en puerto " + puerto);

                while (true) {
                    Socket cliente = ss.accept();
                    agregarJugador(cliente);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    //Aquí se van añadiendo los jugadores hasta que hay suficientes para empezar la partida
    public synchronized void agregarJugador(Socket cliente) {
        try {
            new PrintWriter(cliente.getOutputStream(), true)
                    .println("Conectado a " + nombre + ". Esperando más jugadores...");
        } catch (IOException ignored) {}

        jugadores.add(cliente);

        if (jugadores.size() == max) {
            List<Socket> partida = new ArrayList<>(jugadores);
            jugadores.clear();

            pool.submit(new ThreadAdd(partida, max));
        }
    }
}
