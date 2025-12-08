package servidor;

import objetos.Dado;
import objetos.Jugador;

import java.io.*;
import java.net.Socket;
import java.util.*;

public class ThreadAdd implements Runnable {

    private final List<Socket> clientes;
    private final int maxJug;

    public ThreadAdd(List<Socket> sockets, int maxJugadores) {
        this.clientes = sockets;
        this.maxJug = maxJugadores;
    }

    @Override
    public void run() {
        Map<Socket, BufferedReader> lectores = new HashMap<>();
        Map<Socket, PrintWriter> escritores = new HashMap<>();
        Map<Socket, Jugador> jugadoresMap = new HashMap<>();

        try {
            // Inicializar jugadores
            for (Socket s : clientes) {
                BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
                PrintWriter pw = new PrintWriter(s.getOutputStream(), true);

                lectores.put(s, br);
                escritores.put(s, pw);

                pw.println("Introduce tu nombre:");
                String nombre = br.readLine();
                jugadoresMap.put(s, new Jugador(nombre));
            }

            List<Jugador> jugadores = new ArrayList<>(jugadoresMap.values());

            boolean winner = false;

            while (!winner) {
                String apuesta = "0 d1";
                Jugador jugadorAnterior = null;

                List<Dado> mesa = new ArrayList<>();
                for (Jugador j : jugadores) {
                    j.Nuevoturno();
                    mesa.addAll(j.getMano().getDados());
                }

                boolean finRonda = false;
                int turno = 0;

                while (!finRonda) {
                    turno %= jugadores.size();
                    Jugador actual = jugadores.get(turno);

                    if (actual.getMano().Vacio()) {
                        turno++;
                        continue;
                    }

                    Socket sActual = jugadoresMap.entrySet().stream()
                            .filter(e -> e.getValue() == actual)
                            .findFirst().get().getKey();

                    PrintWriter pw = escritores.get(sActual);
                    BufferedReader br = lectores.get(sActual);

                    pw.println("=== TURNO ===");
                    pw.println("Tus dados: " + actual.getMano().Mostrar());
                    pw.println("Apuesta actual: " + apuesta);
                    pw.println("Opciones: (n dn))Hacer apuesta (Ej: 3 d4)  2) Mentiroso");

                    String acc = br.readLine();
                    if (acc == null) return;

                    acc = acc.trim();

                    // Mentiroso
                    if (acc.equals("2")) {
                        int kPrev = Integer.parseInt(apuesta.split(" d")[0]);
                        int caraPrev = Integer.parseInt(apuesta.split(" d")[1]);

                        int cont = 0;
                        for (Dado d : mesa)
                            if (d.getNumero() == caraPrev) cont++;

                        if (cont < kPrev) {
                            jugadorAnterior.QuitarDado();
                            broadcast(escritores, jugadorAnterior.getName() + " pierde un dado.");
                        } else {
                            actual.QuitarDado();
                            broadcast(escritores, actual.getName() + " pierde un dado.");
                        }

                        finRonda = true;
                        continue;
                    }

                    // Apostar
                    if (acc.matches("\\d+ d[1-6]")) {
                        int kNew = Integer.parseInt(acc.split(" d")[0]);
                        int caraNew = Integer.parseInt(acc.split(" d")[1]);

                        int kPrev = Integer.parseInt(apuesta.split(" d")[0]);
                        int caraPrev = Integer.parseInt(apuesta.split(" d")[1]);

                        boolean valido = (kNew > kPrev) || (kNew == kPrev && caraNew > caraPrev);

                        if (!valido) {
                            pw.println("Apuesta inválida.");
                            continue;
                        }

                        apuesta = acc;
                        jugadorAnterior = actual;
                        turno++;
                        continue;
                    }

                    pw.println("Entrada inválida.");
                }

                // Revisar jugadores vivos
                List<Jugador> vivos = new ArrayList<>();
                for (Jugador j : jugadores)
                    if (!j.getMano().Vacio())
                        vivos.add(j);

                if (vivos.size() == 1) {
                    broadcast(escritores, "FIN DE PARTIDA! GANADOR: " + vivos.get(0).getName());
                    winner = true;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            for (Socket s : clientes) {
                try { s.close(); } catch (IOException ignored) {}
            }
        }
    }

    private void broadcast(Map<Socket, PrintWriter> escritores, String msg) {
        for (PrintWriter w : escritores.values()) {
            w.println(msg);
        }
    }
}
