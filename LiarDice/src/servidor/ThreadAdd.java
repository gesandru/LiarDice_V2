package servidor;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

import objetos.Dado;
import objetos.Jugador;

/**
 * ThreadAdd: servidor que gestiona todos los jugadores desde una sola conexión cliente.
 * Protocolo (textual):
 * - Cliente envía nombres separados por coma en la primera línea: "pepe,ana,juan"
 * - Servidor por cada ronda envía:
 *     TOTAL:<int>
 *   y por cada turno:
 *     TURNO:<nombre>
 *     MANO:<string>
 *     APUESTA_ANT:<string>   (ej: "1 d3")
 *     DADOS_ANT:<int>
 * - Cliente responde:
 *     APUESTA:<k dL>   (ej: "APUESTA:3 d4")
 *   o
 *     MIENTES
 * - Si la apuesta es inválida, servidor responde:
 *     ERROR:APUESTA_INCORRECTA
 *   y vuelve a pedir acción al mismo jugador.
 * - Si la apuesta es válida, servidor responde:
 *     OK
 * - Si el cliente ha dicho MIENTES, servidor evalúa y responde:
 *     RESULTADO:...
 *     RONDATERMINADA:true
 * - Al final del juego servidor envía:
 *     WINNER:<nombre>
 */
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
            // 1) Leer nombres (una línea: "pepe,ana,juan")
            String lineaJugadores = br.readLine();
            if (lineaJugadores == null || lineaJugadores.trim().isEmpty()) {
                pw.println("ERROR: no se recibieron nombres");
                return;
            }
            String[] nombres = lineaJugadores.split(",");

            ArrayList<Jugador> jugadores = new ArrayList<>();
            for (String nombre : nombres) {
                jugadores.add(new Jugador(nombre.trim()));
            }

            String apuesta = "0 d1";  // apuesta inicial
            Jugador jugadorAnterior = null;
            boolean winner = false;

            while (!winner) {
                // Preparar nueva ronda: cada jugador lanza sus dados
                ArrayList<Dado> dadosEnMesa = new ArrayList<>();
                int sinDados = 0;
                for (Jugador j : jugadores) {
                    j.Nuevoturno();
                    dadosEnMesa.addAll(j.getMano().getDados());
                    if (j.getMano().Vacio()) sinDados++;
                }

                // Enviar total de dados en mesa
                int totalDados = dadosEnMesa.size();
                pw.println("TOTAL:" + totalDados);

                // Si solo queda 1 jugador con dados, finaliza la partida
                if (sinDados == jugadores.size() - 1) {
                    winner = true;
                    // se informará abajo
                }

                boolean rondaTerminada = false;
                int turno = 0;

                while (!rondaTerminada) {
                    turno = turno % jugadores.size();
                    Jugador actual = jugadores.get(turno);

                    if (!actual.getMano().Vacio()) {
                        String manoString = actual.getMano().Mostrar().toString(); // "2 d4 2 d6"
                        int dadosAnterior = (jugadorAnterior == null) ? 0 : jugadorAnterior.getDadosEnMano();

                        // Enviar contexto al cliente
                        pw.println("TURNO:" + actual.getName());
                        pw.println("MANO:" + manoString);
                        pw.println("APUESTA_ANT:" + apuesta);
                        pw.println("DADOS_ANT:" + dadosAnterior);

                        // Esperar acción del cliente (APUESTA:... o MIENTES)
                        String respuesta = br.readLine();
                        if (respuesta == null) {
                            // Cliente desconectado
                            pw.println("ERROR: cliente desconectado");
                            return;
                        }
                        respuesta = respuesta.trim();

                        // Si el cliente acusa "MIENTES"
                        if (respuesta.equalsIgnoreCase("MIENTES")) {
                            // validar apuesta anterior existe
                            if (!apuesta.matches("\\d+ d[1-6]")) {
                                // apuesta previa mal formada -> no se puede comprobar, penalizamos al acusador por seguridad
                                actual.QuitarDado();
                                pw.println("RESULTADO: Apuesta previa inválida, acusador penalizado.");
                            } else {
                                // parsear apuesta previa
                                String[] parts = apuesta.split(" d");
                                int kPrev = Integer.parseInt(parts[0]);
                                int facePrev = Integer.parseInt(parts[1]);

                                // contar caras en mesa
                                int contador = 0;
                                for (Dado d : dadosEnMesa)
                                    if (d.getNumero() == facePrev) contador++;

                                if (contador < kPrev) {
                                    // apuesta falsa: jugadorAnterior pierde 1 dado
                                    if (jugadorAnterior != null && jugadorAnterior.getDadosEnMano() > 0) {
                                        jugadorAnterior.QuitarDado();
                                        pw.println("RESULTADO: La apuesta era falsa. " + jugadorAnterior.getName() + " pierde 1 dado.");
                                    } else {
                                        // fallback: si no hay jugador anterior con dados, penalizar acusador
                                        actual.QuitarDado();
                                        pw.println("RESULTADO: La apuesta era falsa. (penalizado acusador) " + actual.getName() + " pierde 1 dado.");
                                    }
                                } else {
                                    // apuesta verdadera: acusador pierde 1 dado
                                    actual.QuitarDado();
                                    pw.println("RESULTADO: La apuesta era verdadera. " + actual.getName() + " pierde 1 dado.");
                                }
                            }
                            // cerrar la ronda
                            rondaTerminada = true;
                            pw.println("RONDATERMINADA:true");
                        } else if (respuesta.startsWith("APUESTA:")) {
                            String apuestaRecibida = respuesta.substring("APUESTA:".length()).trim(); // "3 d4"

                            // Validar formato
                            if (!apuestaRecibida.matches("\\d+ d[1-6]")) {
                                pw.println("ERROR:APUESTA_INCORRECTA");
                                // no avanzar turno; repetir para el mismo jugador
                                continue;
                            }

                            // parsear números
                            String[] p = apuestaRecibida.split(" d");
                            int kNew = Integer.parseInt(p[0]);
                            int faceNew = Integer.parseInt(p[1]);

                            // parsear apuesta anterior
                            int kPrev = 0;
                            int facePrev = 1;
                            if (apuesta.matches("\\d+ d[1-6]")) {
                                String[] pa = apuesta.split(" d");
                                kPrev = Integer.parseInt(pa[0]);
                                facePrev = Integer.parseInt(pa[1]);
                            } else {
                                // si apuesta anterior no está en formato esperado (salvo inicial "0 d1"), tomar valores por defecto
                                kPrev = 0;
                                facePrev = 1;
                            }

                            // Regla de validez:
                            // - kNew > kPrev  (y kNew <= totalDados)
                            // OR
                            // - kNew == kPrev AND faceNew > facePrev
                            boolean valido = false;
                            if (kNew <= 0 || faceNew < 1 || faceNew > 6) {
                                valido = false;
                            } else if (kNew > totalDados) {
                                // no se puede apostar más dados que los que hay en mesa
                                valido = false;
                            } else if (kNew > kPrev) {
                                valido = true;
                            } else if (kNew == kPrev && faceNew > facePrev) {
                                valido = true;
                            } else {
                                valido = false;
                            }

                            if (!valido) {
                                pw.println("ERROR:APUESTA_INCORRECTA");
                                // no cambiar jugadorAnterior ni avanzar el turno, repetir petición del mismo jugador
                                continue;
                            }

                            // apuesta válida: actualizar estado
                            apuesta = apuestaRecibida;
                            jugadorAnterior = actual;
                            pw.println("OK");
                            // continuar al siguiente jugador (no cerrar ronda)
                        } else {
                            // comando desconocido: pedir de nuevo
                            pw.println("ERROR:COMANDO_INVALIDO");
                            continue;
                        }
                    } // if jugador tiene dados
                    turno++;
                } // while !rondaTerminada

                // comprobar ganador
                int sinDadosAhora = 0;
                String posibleWinner = "Empty";
                for (Jugador j : jugadores) {
                    if (j.getMano().Vacio()) sinDadosAhora++;
                    else posibleWinner = j.getName();
                }
                if (sinDadosAhora == jugadores.size() - 1) {
                    winner = true;
                    pw.println("WINNER:" + posibleWinner);
                } else {
                    pw.println("CONTINUAR");
                }

            } // while !winner

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try { socket.close(); } catch (IOException ignored) {}
        }
    }
}
