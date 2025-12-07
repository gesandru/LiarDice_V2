package servidor;

import objetos.Dado;
import objetos.Jugador;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ThreadAdd implements Runnable {

    private final Socket socket;

    public ThreadAdd(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter pw = new PrintWriter(socket.getOutputStream(), true)
        ) {
            // Leer nombres: "pepe,ana,juan"
            String lineaJugadores = br.readLine();
            if (lineaJugadores == null || lineaJugadores.trim().isEmpty()) {
                pw.println("ERROR: no se recibieron nombres");
                return;
            }
            String[] nombres = lineaJugadores.split(",");
            ArrayList<Jugador> jugadores = new ArrayList<>();
            for (String nombre : nombres) jugadores.add(new Jugador(nombre.trim()));

            boolean overallWinner = false;

            while (!overallWinner) {
                // ---------------------------
                // INICIO DE RONDA: resetear apuesta y jugadorAnterior
                // ---------------------------
                String apuesta = "0 d1";          // <<-- reseteo al inicio de cada ronda
                Jugador jugadorAnterior = null;  // <<-- nadie apostó todavía en la nueva ronda

                // nueva ronda: todos tiran
                ArrayList<Dado> dadosEnMesa = new ArrayList<>();
                int sinDados = 0;
                for (Jugador j : jugadores) {
                    j.Nuevoturno();
                    dadosEnMesa.addAll(j.getMano().getDados());
                    if (j.getMano().Vacio()) sinDados++;
                }

                int totalDados = dadosEnMesa.size();
                pw.println("TOTAL:" + totalDados);

                if (sinDados == jugadores.size() - 1) {
                    // finaliza si sólo queda 1 jugador con dados
                    String posible = "Empty";
                    for (Jugador j : jugadores) if (!j.getMano().Vacio()) posible = j.getName();
                    pw.println("WINNER:" + posible);
                    break;
                }

                boolean rondaTerminada = false;
                int turno = 0;

                while (!rondaTerminada) {
                    turno = turno % jugadores.size();
                    Jugador actual = jugadores.get(turno);

                    if (!actual.getMano().Vacio()) {
                        // enviar contexto
                        pw.println("TURNO:" + actual.getName());
                        pw.println("MANO:" + actual.getMano().Mostrar().toString());
                        pw.println("APUESTA_ANT:" + apuesta); // ahora será "0 d1" al inicio de la ronda
                        int dadosAnt = (jugadorAnterior == null) ? 0 : jugadorAnterior.getDadosEnMano();
                        pw.println("DADOS_ANT:" + dadosAnt);

                        // IMPORTANTE: prompt explícito que sincroniza
                        pw.println("PIDE_ACCION");

                        // leer acción del cliente
                        String respuesta = br.readLine();
                        if (respuesta == null) {
                            pw.println("ERROR: cliente desconectado");
                            return;
                        }
                        respuesta = respuesta.trim();

                        // branch MIENTES
                        if (respuesta.equalsIgnoreCase("MIENTES")) {
                            if (!apuesta.matches("\\d+ d[1-6]")) {
                                // apuesta inválida previa -> penalizar acusador por seguridad
                                actual.QuitarDado();
                                pw.println("RESULTADO: Apuesta previa inválida, acusador penalizado.");
                            } else {
                                String[] parts = apuesta.split(" d");
                                int kPrev = Integer.parseInt(parts[0]);
                                int facePrev = Integer.parseInt(parts[1]);

                                int contador = 0;
                                for (Dado d : dadosEnMesa) if (d.getNumero() == facePrev) contador++;

                                if (contador < kPrev) {
                                    if (jugadorAnterior != null && jugadorAnterior.getDadosEnMano() > 0) {
                                        jugadorAnterior.QuitarDado();
                                        pw.println("RESULTADO: La apuesta era falsa. " + jugadorAnterior.getName() + " pierde 1 dado.");
                                    } else {
                                        actual.QuitarDado();
                                        pw.println("RESULTADO: La apuesta era falsa. (penalizado acusador) " + actual.getName() + " pierde 1 dado.");
                                    }
                                } else {
                                    actual.QuitarDado();
                                    pw.println("RESULTADO: La apuesta era verdadera. " + actual.getName() + " pierde 1 dado.");
                                }
                            }
                            rondaTerminada = true;
                            pw.println("RONDATERMINADA:true");
                        }
                        // branch APUESTA
                        else if (respuesta.startsWith("APUESTA:")) {
                            String apuestaRec = respuesta.substring("APUESTA:".length()).trim(); // "3 d4"
                            if (!apuestaRec.matches("\\d+ d[1-6]")) {
                                pw.println("ERROR:APUESTA_INCORRECTA");
                                // repetir petición al mismo jugador
                                continue;
                            }

                            String[] p = apuestaRec.split(" d");
                            int kNew = Integer.parseInt(p[0]);
                            int faceNew = Integer.parseInt(p[1]);

                            // parse apuesta previa
                            int kPrev = 0;
                            int facePrev = 1;
                            if (apuesta.matches("\\d+ d[1-6]")) {
                                String[] pa = apuesta.split(" d");
                                kPrev = Integer.parseInt(pa[0]);
                                facePrev = Integer.parseInt(pa[1]);
                            }

                            boolean valido = false;
                            if (kNew <= 0 || faceNew < 1 || faceNew > 6 || kNew > totalDados) {
                                valido = false;
                            } else if (kNew > kPrev) {
                                valido = true;
                            } else if (kNew == kPrev && faceNew > facePrev) {
                                valido = true;
                            }

                            if (!valido) {
                                pw.println("ERROR:APUESTA_INCORRECTA");
                                continue; // repetir misma petición
                            }

                            // apuesta aceptada
                            apuesta = apuestaRec;
                            jugadorAnterior = actual;
                            pw.println("OK");
                            // no cerramos la ronda; el bucle continuará y pasará al siguiente jugador
                        }
                        // branch comando inválido
                        else {
                            pw.println("ERROR:COMANDO_INVALIDO");
                            continue;
                        }
                    } // if actual tiene dados

                    turno++;
                } // while !rondaTerminada

                // al terminar la ronda, se vuelve al inicio del while principal y se generará
                // una nueva ronda (apuesta reseteada allí)
                // antes de comenzar la siguiente iteración se comprobará si hay ganador global:
                int jugadoresConDados = 0;
                String posibleWinner = "Empty";
                for (Jugador j : jugadores) {
                    if (!j.getMano().Vacio()) {
                        jugadoresConDados++;
                        posibleWinner = j.getName();
                    }
                }
                if (jugadoresConDados <= 1) {
                    overallWinner = true;
                    pw.println("WINNER:" + posibleWinner);
                    break;
                } else {
                    pw.println("CONTINUAR");
                }
            } // while !overallWinner

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try { socket.close(); } catch (IOException ignored) {}
        }
    }
}
