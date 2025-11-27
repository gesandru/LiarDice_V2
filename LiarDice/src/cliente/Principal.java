package cliente;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Principal {
	
	//Podría cambiar el juego para que cada jugador sea un cliente distinto que se conecta al servidor, 
	//pero habría que cambiar como funciona la redirección al servidor y eso parece complicado

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        try (Socket s = new Socket("localhost", 55555);
             BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
             PrintWriter out = new PrintWriter(s.getOutputStream(), true)) {

            // Enviar nombres iniciales
            System.out.println("Introduce el número de jugadores (2-4): ");
            out.println(sc.nextLine()); // Número

            int jugadores = Integer.parseInt(in.readLine()); // Servidor confirma

            for (int i = 0; i < jugadores; i++) {
                System.out.println("Introduce el nombre del jugador " + (i + 1) + ":");
                out.println(sc.nextLine());
            }

            // Bucle de juego
            while (true) {
                String mensaje = in.readLine();

                if (mensaje.startsWith("TURNO")) {
                    System.out.println("\n>>> " + mensaje);
                }
                else if (mensaje.equals("PEDIR_APUESTA")) {
                    System.out.println("Introduce tu apuesta (ej: 3 d4) o 'M' para mentiroso:");
                    String apuesta = sc.nextLine();
                    out.println(apuesta);
                }
                else if (mensaje.startsWith("INFO")) {
                    System.out.println(mensaje);
                }
                else if (mensaje.startsWith("RONDA_TERMINADA")) {
                    System.out.println(">>> Ronda terminada.");
                }
                else if (mensaje.startsWith("GANADOR")) {
                    System.out.println("¡El ganador es: " + mensaje.split(":")[1] + "!");
                    break;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
