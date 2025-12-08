package servidor;

import objetos.Dado;
import objetos.Jugador;
import java.io.*;
import java.net.Socket;
import java.util.*;

public class ThreadAdd implements Runnable {
    private final List<Socket> clientes;

    public ThreadAdd(List<Socket> clientes) {
        this.clientes = clientes;
    }

    @Override
    public void run() {
        Map<Socket, BufferedReader> lectores = new HashMap<>();
        Map<Socket, PrintWriter> escritores = new HashMap<>();
        Map<Socket, Jugador> jugadoresMap = new HashMap<>();

        try {
            // Inicializar sockets y pedir nombre
            for (Socket s : clientes) {
                BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
                PrintWriter pw = new PrintWriter(s.getOutputStream(), true);
                lectores.put(s, br);
                escritores.put(s, pw);

                pw.println("Introduce tu nombre:");
                String nombre = br.readLine().trim();
                jugadoresMap.put(s, new Jugador(nombre));
            }

            List<Jugador> jugadores = new ArrayList<>(jugadoresMap.values());
            boolean winner = false;

            while (!winner) {
                // Reset ronda
                String apuesta = "0 d1";
                Jugador jugadorAnterior = null;

                // Tirar dados
                List<Dado> dadosMesa = new ArrayList<>();
                for (Jugador j : jugadores) {
                    j.Nuevoturno();
                    dadosMesa.addAll(j.getMano().getDados());
                }

                boolean rondaTerminada = false;
                int turno = 0;

                while (!rondaTerminada) {
                    turno = turno % jugadores.size();
                    Jugador actual = jugadores.get(turno);
                    Socket sActual = null;
                    for (Map.Entry<Socket, Jugador> e : jugadoresMap.entrySet()) {
                        if (e.getValue() == actual) sActual = e.getKey();
                    }
                    PrintWriter pw = escritores.get(sActual);
                    BufferedReader br = lectores.get(sActual);

                    if (actual.getMano().Vacio()) {
                        turno++;
                        continue;
                    }

                    // Enviar información del turno
                    pw.println("=== NUEVO TURNO ===");
                    pw.println("Tus dados: " + actual.getMano().Mostrar());
                    pw.println("Apuesta anterior: " + apuesta);
                    pw.println("Dados del jugador anterior: " + ((jugadorAnterior == null) ? 0 : jugadorAnterior.getDadosEnMano()));
                    pw.println("1. Apostar");
                    pw.println("2. Llamar mentiroso");

                    String accion;
                    while (true) {
                        pw.println("Elige (1/2) o escribe apuesta (ej: 3 d4):");
                        accion = br.readLine();
                        if (accion == null) throw new IOException("Cliente desconectado");
                        accion = accion.trim();

                        if (accion.equals("2")) { // mentiroso
                            if (jugadorAnterior == null) {
                                pw.println("ERROR: No hay apuesta anterior");
                                continue;
                            }

                            String[] parts = apuesta.split(" d");
                            int kPrev = Integer.parseInt(parts[0]);
                            int facePrev = Integer.parseInt(parts[1]);

                            int contador = 0;
                            for (Dado d : dadosMesa) if (d.getNumero() == facePrev) contador++;

                            if (contador < kPrev) {
                                jugadorAnterior.QuitarDado();
                                pw.println("RESULTADO: La apuesta era falsa. " + jugadorAnterior.getName() + " pierde 1 dado.");
                            } else {
                                actual.QuitarDado();
                                pw.println("RESULTADO: La apuesta era verdadera. " + actual.getName() + " pierde 1 dado.");
                            }

                            rondaTerminada = true;
                            break;

                        } else if (accion.matches("\\d+ d[1-6]")) { // apuesta
                            String[] p = accion.split(" d");
                            int kNew = Integer.parseInt(p[0]);
                            int faceNew = Integer.parseInt(p[1]);

                            String[] prev = apuesta.split(" d");
                            int kPrev = Integer.parseInt(prev[0]);
                            int facePrev = Integer.parseInt(prev[1]);

                            boolean valido = (kNew > kPrev) || (kNew == kPrev && faceNew > facePrev);
                            if (!valido) {
                                pw.println("ERROR:APUESTA_INCORRECTA");
                                continue;
                            }

                            apuesta = accion;
                            jugadorAnterior = actual;
                            break;

                        } else {
                            pw.println("ERROR:COMANDO_INVALIDO");
                        }
                    }

                    turno++;
                }

                // Comprobar ganador
                List<Jugador> conDados = new ArrayList<>();
                for (Jugador j : jugadores) if (!j.getMano().Vacio()) conDados.add(j);
                if (conDados.size() <= 1) {
                    winner = true;
                    String ganador = (conDados.isEmpty()) ? "NINGUNO" : conDados.get(0).getName();
                    for (PrintWriter pw : escritores.values()) {
                        pw.println("GANADOR: " + ganador);
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // Cerrar todos los sockets al terminar la partida
            for (Socket s : clientes) {
                try { s.close(); } catch (IOException ignored) {}
            }
        }
    }
}
