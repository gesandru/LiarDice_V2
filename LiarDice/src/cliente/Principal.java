package cliente;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

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
            System.out.println("Introduce los nombres de los jugadores separados por coma (ej: pepe,ana,juan):");
            String nombres = sc.nextLine().trim();
            pw.println(nombres);

            boolean running = true;

            while (running) {
                String line = br.readLine();
                if (line == null) break;

                if (line.startsWith("TOTAL:")) {
                    System.out.println("\n=== NUEVA RONDA ===");
                    System.out.println("Total dados en mesa: " + line.substring(6));
                    continue;
                }
                if (line.startsWith("WINNER:")) {
                    System.out.println("\n=== GANADOR: " + line.substring(7));
                    break;
                }
                if (line.startsWith("RESULTADO:")) {
                    System.out.println(line.substring(10));
                    continue;
                }
                if (line.startsWith("RONDATERMINADA:")) {
                    System.out.println("Servidor: " + line);
                    continue;
                }

                // Si comienza un turno
                if (line.startsWith("TURNO:")) {
                    String jugador = line.substring(6);

                    // leer MANO, APUESTA_ANT, DADOS_ANT (suponemos que el servidor los envía)
                    String manoLine = br.readLine();          // MANO:...
                    String apuestaAntLine = br.readLine();   // APUESTA_ANT:...
                    String dadosAntLine = br.readLine();     // DADOS_ANT:...

                    String mano = manoLine.startsWith("MANO:") ? manoLine.substring(5) : "";
                    String apuestaAnt = apuestaAntLine.startsWith("APUESTA_ANT:") ? apuestaAntLine.substring(12) : "0 d1";
                    String dadosAnt = dadosAntLine.startsWith("DADOS_ANT:") ? dadosAntLine.substring(10) : "0";

                    System.out.println("\nTurno de → " + jugador);
                    System.out.println("Tus dados: " + mano);
                    System.out.println("Apuesta anterior: " + apuestaAnt);
                    System.out.println("Dados del jugador anterior: " + dadosAnt);

                    // ahora ESPERAMOS el prompt para actuar
                    String prompt = br.readLine();
                    if (prompt == null) break;
                    if (!prompt.equals("PIDE_ACCION")) {
                        // si llega otra cosa inesperada, lo imprimimos y seguimos
                        System.out.println("Servidor: " + prompt);
                        continue;
                    }

                    // Pedir acción al usuario
                    boolean accionOk = false;
                    while (!accionOk) {
                        System.out.println("1. Apostar");
                        System.out.println("2. Llamar mentiroso");
                        System.out.print("Elige (1/2): ");
                        String opc = sc.nextLine().trim();
                        if (!opc.equals("1") && !opc.equals("2")) {
                            System.out.println("Opción inválida.");
                            continue;
                        }
                        if (opc.equals("2")) {
                            pw.println("MIENTES");
                            // recibiríamos RESULTADO: y RONDATERMINADA: (si corresponde)
                            String res = br.readLine();
                            if (res != null && res.startsWith("RESULTADO:")) {
                                System.out.println(res.substring("RESULTADO:".length()));
                                String rond = br.readLine();
                                if (rond != null && rond.startsWith("RONDATERMINADA:")) {
                                    System.out.println("Servidor: " + rond);
                                }
                            } else if (res != null && res.startsWith("ERROR:")) {
                                System.out.println("Servidor: " + res);
                            }
                            accionOk = true;
                        } else {
                            // apostar
                            System.out.print("Cantidad (k): ");
                            String ks = sc.nextLine().trim();
                            System.out.print("Cara (1-6): ");
                            String face = sc.nextLine().trim();
                            pw.println("APUESTA:" + ks + " d" + face);

                            // esperar respuesta (OK o ERROR:APUESTA_INCORRECTA)
                            String respuestaSer = br.readLine();
                            if (respuestaSer == null) { running = false; break; }
                            if (respuestaSer.equals("OK")) {
                                System.out.println("Apuesta aceptada.");
                                accionOk = true;
                            } else if (respuestaSer.equals("ERROR:APUESTA_INCORRECTA")) {
                                System.out.println("ERROR: apuesta incorrecta. Intenta otra.");
                                // volver a iterar y pedir otra apuesta al mismo jugador
                            } else {
                                System.out.println("Servidor: " + respuestaSer);
                            }
                        }
                    } // while accionOk

                    continue;
                }

                // cualquier otro mensaje
                System.out.println("Servidor: " + line);
            }

            System.out.println("Partida finalizada. Conexión cerrada.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
