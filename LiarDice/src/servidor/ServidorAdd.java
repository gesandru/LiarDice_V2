package servidor;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class ServidorAdd {
    private final String nombreSala;
    private final int maxJugadores;
    private final int puerto;
    private final List<Socket> jugadores = Collections.synchronizedList(new ArrayList<>());
    private final ExecutorService pool = Executors.newCachedThreadPool();

    public ServidorAdd(String nombre, int maxJug, int puerto) {
        this.nombreSala = nombre;
        this.maxJugadores = maxJug;
        this.puerto = puerto;
    }

    public void iniciar() {
        pool.submit(() -> {
            try (ServerSocket serverSocket = new ServerSocket(puerto)) {
                System.out.println(nombreSala + " escuchando en puerto " + puerto);

                while (true) {
                    Socket cliente = serverSocket.accept();
                    jugadores.add(cliente);
                    pool.submit(() -> manejarJugador(cliente));

                    if (jugadores.size() == maxJugadores) {
                        List<Socket> partida = new ArrayList<>(jugadores);
                        jugadores.clear();
                        pool.submit(new ThreadAdd(partida));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void manejarJugador(Socket cliente) {
        try {
            PrintWriter pw = new PrintWriter(cliente.getOutputStream(), true);
            pw.println("Conectado a " + nombreSala + ". Esperando más jugadores...");
            // NO cerrar socket aquí
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public int getPuerto() {
    	return this.puerto;
    }
}
