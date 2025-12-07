package cliente;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Principal {

    public static void main(String[] args) throws Exception {

        Scanner sc = new Scanner(System.in);

        Socket sock = new Socket("localhost", 55555);
        BufferedReader br = new BufferedReader(new InputStreamReader(sock.getInputStream()));
        PrintWriter pw = new PrintWriter(sock.getOutputStream(), true);

        String linea = br.readLine();
        if (!linea.startsWith("REDIRECT:")) {
            System.out.println("ERROR: respuesta inesperada del proxy");
            return;
        }

        int puertoSala = Integer.parseInt(linea.split(":")[1]);
        sock.close();

        System.out.println("Conectando a sala en puerto " + puertoSala);

        sock = new Socket("localhost", puertoSala);
        br = new BufferedReader(new InputStreamReader(sock.getInputStream()));
        pw = new PrintWriter(sock.getOutputStream(), true);

        System.out.println("Introduce los nombres de los jugadores separados por coma:");
        pw.println(sc.nextLine());

        boolean jugando = true;

        while (jugando) {
            String msg = br.readLine();
            if (msg == null) break;

            if (msg.startsWith("TURNO:")) {
                System.out.println("\nTurno de → " + msg.substring(6));
            } else if (msg.startsWith("MANO:")) {
                System.out.println("Tus dados: " + msg.substring(5));
            } else if (msg.startsWith("APUESTA_ANT:")) {
                System.out.println("Apuesta anterior: " + msg.substring(12));
            } else if (msg.startsWith("DADOS_ANT:")) {
                System.out.println("Dados del jugador anterior: " + msg.substring(10));
            } else if (msg.equals("PIDE_ACCION")) {
                System.out.println("1. Apostar");
                System.out.println("2. Llamar mentiroso");
                System.out.print("Elige (1/2): ");
                int op = Integer.parseInt(sc.nextLine().trim());

                if (op == 1) {
                    System.out.print("Cantidad (k): ");
                    int k = Integer.parseInt(sc.nextLine());

                    System.out.print("Cara (1-6): ");
                    int cara = Integer.parseInt(sc.nextLine());

                    pw.println("APUESTA:" + k + " d" + cara);
                } else {
                    pw.println("MIENTES");
                }

            } else if (msg.startsWith("ERROR:")) {
                System.out.println(msg);
            } else if (msg.startsWith("OK")) {
                System.out.println("Apuesta aceptada.");
            } else if (msg.startsWith("RESULTADO:")) {
                System.out.println(msg.substring(10));
            } else if (msg.startsWith("RONDATERMINADA")) {
                System.out.println("Ronda terminada.");
            } else if (msg.startsWith("TOTAL:")) {
                System.out.println("\n=== NUEVA RONDA ===");
                System.out.println("Total dados en mesa: " + msg.substring(6));
            } else if (msg.startsWith("WINNER:")) {
                System.out.println("\nGANADOR: " + msg.substring(7));
                jugando = false;
            } else if (msg.equals("CONTINUAR")) {
                continue;
            }
        }

        sock.close();
    }
}
