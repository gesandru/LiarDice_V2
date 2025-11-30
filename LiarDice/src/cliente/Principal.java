package cliente;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

/**
 * Cliente que controla a todos los jugadores desde una sola conexión.
 * Protocolo con el servidor según ThreadAdd.java.
 */
public class Principal {

    public static void main(String[] args) {
        final String SERVER = "localhost";
        final int PORT = 55555;

        try (
            Socket socket = new Socket(SERVER, PORT);
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter pw = new PrintWriter(socket.getOutputStream(), true);
            Scanner sc = new Scanner(System.in)
        ) {
            // Enviar nombres de jugadores
            System.out.println("Introduce los nombres de los jugadores separados por coma (ej: pepe,ana,juan):");
            String nombres = sc.nextLine().trim();
            pw.println(nombres);

            boolean partidaActiva = true;

            while (partidaActiva) {
                String linea = br.readLine();
                if (linea == null) break;

                if (linea.startsWith("TOTAL:")) {
                    System.out.println("\n=== NUEVA RONDA ===");
                    System.out.println("Total dados en mesa: " + linea.split(":")[1]);
                    // A continuación vendrán los turnos
                    continue;
                }

                if (linea.startsWith("TURNO:")) {
                    String jugador = linea.split(":", 2)[1];

                    // Leer MANO, APUESTA_ANT, DADOS_ANT (en ese orden, como envía el servidor)
                    String manoLine = br.readLine();
                    String apuestaAntLine = br.readLine();
                    String dadosAntLine = br.readLine();

                    String mano = manoLine.startsWith("MANO:") ? manoLine.split(":",2)[1] : "";
                    String apuestaAnt = apuestaAntLine.startsWith("APUESTA_ANT:") ? apuestaAntLine.split(":",2)[1] : "0 d1";
                    String dadosAnt = dadosAntLine.startsWith("DADOS_ANT:") ? dadosAntLine.split(":",2)[1] : "0";

                    System.out.println("\nTurno de → " + jugador);
                    System.out.println("Tus dados: " + (mano.isEmpty() ? "(sin dados)" : mano));
                    System.out.println("Apuesta anterior: " + apuestaAnt);
                    System.out.println("Dados del jugador anterior: " + dadosAnt);

                    boolean accionAceptada = false;
                    while (!accionAceptada) {
                        System.out.println("1. Apostar");
                        System.out.println("2. Llamar mentiroso");
                        String opc = sc.nextLine().trim();
                        if (!opc.equals("1") && !opc.equals("2")) {
                            System.out.println("Opción inválida. Intenta de nuevo.");
                            continue;
                        }

                        if (opc.equals("2")) {
                            // Llamar mentiroso
                            pw.println("MIENTES");

                            // leer resultado(s) del servidor:
                            String res = br.readLine(); // RESULTADO:...
                            if (res != null && res.startsWith("RESULTADO:")) {
                                System.out.println(res.substring("RESULTADO:".length()));
                            } else if (res != null && res.startsWith("ERROR:")) {
                                System.out.println("Servidor: " + res);
                            }
                            // leer RONDATERMINADA:true
                            String rond = br.readLine();
                            if (rond != null && rond.startsWith("RONDATERMINADA:")) {
                                System.out.println("Servidor: " + rond);
                            }
                            accionAceptada = true; // la ronda acaba o continúa según el servidor
                        } else {
                            // Apostar: pedir datos al usuario y enviar al servidor.
                            System.out.print("Cantidad : ");
                            String ks = sc.nextLine().trim();
                            System.out.print("Cara (1-6): ");
                            String faces = sc.nextLine().trim();

                            // Formar la apuesta como "APUESTA:k dX"
                            String apuestaEnv = "APUESTA:" + ks + " d" + faces;
                            pw.println(apuestaEnv);

                            // Esperar respuesta del servidor: puede ser OK o ERROR:APUESTA_INCORRECTA
                            String respuestaSer = br.readLine();
                            if (respuestaSer == null) { partidaActiva = false; break; }

                            if (respuestaSer.equals("OK")) {
                                System.out.println("Apuesta aceptada: " + ks + " d" + faces);
                                accionAceptada = true;
                            } else if (respuestaSer.equals("ERROR:APUESTA_INCORRECTA")) {
                                System.out.println("ERROR: apuesta incorrecta. Intenta otra apuesta.");
                                // volver a preguntar al mismo jugador (no cambiar turno)
                                // continue loop
                            } else if (respuestaSer.startsWith("ERROR:")) {
                                System.out.println("Servidor: " + respuestaSer);
                                // repetir
                            } else {
                                // respuestas inesperadas, imprimir y repetir
                                System.out.println("Servidor: " + respuestaSer);
                            }
                        }
                    } // while !accionAceptada

                    // Después de la acción, el servidor confirmará si la ronda terminó o si hay que continuar:
                    // - En caso de MIEN TES ya se envió RONDATERMINADA:true por servidor y lo leímos arriba.
                    // - En caso de APUESTA válida, el servidor seguirá la ronda y luego enviará "CONTINUAR" o "WINNER:..."
                    String post = br.readLine();
                    if (post == null) { break; }
                    if (post.startsWith("WINNER:")) {
                        System.out.println("\n=== GANADOR: " + post.split(":",2)[1] + " ===");
                        partidaActiva = false;
                        break;
                    } else if (post.equals("CONTINUAR")) {
                        // seguir
                        // no imprimimos nada especial
                    } else {
                        // puede ser otras líneas; imprimirlas por si acaso
                        System.out.println("Servidor: " + post);
                    }
                }

                // Por seguridad: si llega WINNER en otra línea fuera del flujo
                if (linea.startsWith("WINNER:")) {
                    System.out.println("\n=== GANADOR: " + linea.split(":",2)[1] + " ===");
                    partidaActiva = false;
                    break;
                }
            } // while partidaActiva

            System.out.println("Partida finalizada. Conexión cerrada.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
