package servidor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

import objetos.Dado;
import objetos.Jugador;

/**
 * ThreadAdd: maneja un juego completo con un único cliente que controla
 * las decisiones de todos los jugadores (modo A).
 *
 * Comunicación por texto:
 * - El cliente primero manda: número de jugadores (línea)
 * - Servidor responde con el mismo número (confirmación)
 * - Cliente envía N nombres (N líneas)
 * - Servidor inicia la partida usando mensajes tipo:
 *     INFO:...
 *     TURNO:<nombre>
 *     PEDIR_APUESTA
 *     RONDA_TERMINADA
 *     GANADOR:<nombre>
 *
 * Cliente responde a PEDIR_APUESTA con:
 *    M   (mentiroso)
 *  o k dL  (ej: "3 d4")
 */
public class ThreadAdd implements Runnable {

    private final Socket socket;

    public ThreadAdd(Socket s) {
        this.socket = s;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            // 1) Recibir número de jugadores y confirmar
            String line = in.readLine();
            if (line == null) return;
            int numJugadores;
            try {
                numJugadores = Integer.parseInt(line.trim());
            } catch (NumberFormatException nfe) {
                out.println("INFO:ERROR: número de jugadores inválido");
                return;
            }
            // Confirmación
            out.println(String.valueOf(numJugadores));

            // 2) Leer nombres y crear jugadores
            ArrayList<Jugador> jugadores = new ArrayList<>();
            for (int i = 0; i < numJugadores; i++) {
                String nombre = in.readLine();
                if (nombre == null) {
                    out.println("INFO:ERROR: falta nombre de jugador");
                    return;
                }
                Jugador j = new Jugador(nombre.trim());
                jugadores.add(j);
            }

            out.println("INFO: Jugadores registrados. Comienza la partida.");

            // Variables del juego
            boolean winner = false;
            boolean ronda = false;
            String apuesta = "0 d1"; // apuesta inicial
            Jugador jugadorAnterior = jugadores.get(jugadores.size() - 1); // como en la versión original
            int dadosJugadorAnterior = jugadorAnterior.getDadosEnMano();

            // Bucle principal de la partida
            while (!winner) {
                // Preparar nueva ronda: lanzar/renovar dados internamente
                ArrayList<Dado> todosDados = new ArrayList<>();
                int manosVacias = 0;
                for (Jugador j : jugadores) {
                    j.Nuevoturno(); // suponiendo que esto rellena la mano
                    todosDados.addAll(j.getMano().getDados());
                    if (j.getMano().Vacio()) manosVacias++;
                }

                // Si sólo queda 1 jugador con dados, la ronda/patada puede finalizar
                if (manosVacias == jugadores.size() - 1) {
                    // Hay una ronda "forzada" en la que no se jugará (se puede detectar después)
                    ronda = true;
                } else {
                    ronda = false;
                }

                // Enviar información al cliente
                out.println("INFO: DADOS_TOTALES:" + todosDados.size());

                // Reset apuesta inicial por si correspondiera
                apuesta = "0 d1";
                jugadorAnterior = jugadores.get(jugadores.size() - 1);
                dadosJugadorAnterior = jugadorAnterior.getDadosEnMano();

                // Bucle de la ronda
                while (!ronda) {
                    for (int i = 0; i < jugadores.size() && !ronda; i++) {
                        Jugador turno = jugadores.get(i);
                        if (turno.getMano().Vacio()) {
                            // Saltar si no tiene dados
                            continue;
                        }

                        // Informar turno y dar contexto
                        out.println("TURNO:" + turno.getName());
                        out.println("INFO:APUESTA_ACTUAL:" + apuesta);
                        out.println("INFO:DADOS_JUGADOR_ANTERIOR:" + dadosJugadorAnterior);
                        out.println("INFO:DADOS_TOTALES:" + todosDados.size());
                        out.println("PEDIR_APUESTA");

                        // Leer respuesta del cliente (apuesta o M)
                        String respuesta = in.readLine();
                        if (respuesta == null) {
                            out.println("INFO:ERROR: cliente desconectado");
                            return;
                        }
                        respuesta = respuesta.trim();

                        if (respuesta.equalsIgnoreCase("M")) {
                            // Se acusa de mentiroso: comprobar la apuesta actual
                            // Parsear apuesta actual (formato "k dL")
                            int k = parseKFromApuesta(apuesta);
                            int l = parseLFromApuesta(apuesta);

                            // Contar caras
                            int[] cont = new int[7]; // índices 1..6
                            for (Dado d : todosDados) {
                                int num = d.getNumero();
                                if (num >= 1 && num <= 6) cont[num]++;
                            }

                            boolean apuestaCumple = false;
                            if (l >= 1 && l <= 6) {
                                apuestaCumple = (k <= cont[l]);
                            }

                            if (!apuestaCumple) {
                                // La apuesta era falsa → el jugador que apostó (jugadorAnterior) pierde un dado
                                if (jugadorAnterior != null && !jugadorAnterior.getMano().Vacio()) {
                                    jugadorAnterior.QuitarDado();
                                    out.println("INFO:RESULTADO: La apuesta era falsa. " + jugadorAnterior.getName() + " pierde 1 dado.");
                                } else {
                                    // fallback: si jugadorAnterior no tiene dados, penalizar al acusador
                                    turno.QuitarDado();
                                    out.println("INFO:RESULTADO: La apuesta era falsa. (penalizado acusador) " + turno.getName() + " pierde 1 dado.");
                                }
                            } else {
                                // La apuesta era verdadera → el acusador (turno) pierde un dado
                                turno.QuitarDado();
                                out.println("INFO:RESULTADO: La apuesta era verdadera. " + turno.getName() + " pierde 1 dado.");
                            }

                            // Indicar fin de ronda
                            ronda = true;
                            out.println("RONDA_TERMINADA");
                            // Actualizar dadosJugadorAnterior (no será usado hasta que haya nueva apuesta)
                            dadosJugadorAnterior = turno.getDadosEnMano();
                            break;
                        } else {
                            // Es una apuesta nueva: guardarla y pasar al siguiente jugador
                            // validar formato básico: algo como "3 d4" o "10 d6"
                            if (!isValidApuestaFormat(respuesta)) {
                                out.println("INFO:ERROR: formato de apuesta inválido. Debes usar 'k dL' (ej: 3 d4).");
                                // pedir de nuevo al mismo jugador (decrementar i para repetir)
                                i--;
                                continue;
                            }
                            apuesta = respuesta;
                            jugadorAnterior = turno;
                            dadosJugadorAnterior = jugadorAnterior.getDadosEnMano();
                            out.println("INFO:APUESTA_RECIBIDA:" + apuesta);
                            // continuar al siguiente jugador
                        }
                    } // for jugadores
                } // while !ronda

                // Después de la ronda, comprobar si hay un ganador
                int jugadoresConDados = 0;
                String posibleGanador = "Empty";
                for (Jugador j : jugadores) {
                    if (!j.getMano().Vacio()) {
                        jugadoresConDados++;
                        posibleGanador = j.getName();
                    }
                }
                if (jugadoresConDados <= 1) {
                    winner = true;
                    out.println("GANADOR:" + posibleGanador);
                    out.println("INFO: Partida finalizada. Ganador -> " + posibleGanador);
                    break;
                } else {
                    // Continuar con la siguiente ronda
                    out.println("INFO: Comienza nueva ronda.");
                }
            } // while !winner

        } catch (IOException | ClassCastException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

    // Helpers para parsear "k dL" (ej "3 d4")
    private int parseKFromApuesta(String apuesta) {
        if (apuesta == null) return 0;
        apuesta = apuesta.replaceAll("\\s+", ""); // "3d4"
        try {
            int pos = apuesta.indexOf('d');
            if (pos > 0) {
                return Integer.parseInt(apuesta.substring(0, pos));
            }
        } catch (Exception ignored) {}
        return 0;
    }

    private int parseLFromApuesta(String apuesta) {
        if (apuesta == null) return 1;
        apuesta = apuesta.replaceAll("\\s+", "");
        try {
            int pos = apuesta.indexOf('d');
            if (pos >= 0 && pos < apuesta.length() - 1) {
                return Integer.parseInt(apuesta.substring(pos + 1));
            }
        } catch (Exception ignored) {}
        return 1;
    }

    private boolean isValidApuestaFormat(String s) {
        if (s == null) return false;
        s = s.trim();
        // acepta formatos como "3 d4", "10 d6", "3d4"
        String compact = s.replaceAll("\\s+", "");
        return compact.matches("\\d+d[1-6]");
    }
}
