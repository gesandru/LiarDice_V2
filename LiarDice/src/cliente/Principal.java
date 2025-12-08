package cliente;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Principal {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        try (
                Socket proxy = new Socket("localhost", 55555);
                BufferedReader br = new BufferedReader(new InputStreamReader(proxy.getInputStream()));
                PrintWriter pw = new PrintWriter(proxy.getOutputStream(), true)
        ) {

            // Pregunta tipo de partida
            System.out.println(br.readLine());
            String tipo = sc.nextLine();
            pw.println(tipo);

            // Recibir puerto de sala
            String respuesta = br.readLine(); // ej: OK:55557
            int puertoSala = Integer.parseInt(respuesta.split(":")[1].trim());

            // Conectarse a la sala
            Socket sala = new Socket("localhost", puertoSala);
            BufferedReader brSala = new BufferedReader(new InputStreamReader(sala.getInputStream()));
            PrintWriter pwSala = new PrintWriter(sala.getOutputStream(), true);

            // Hilo de lectura de mensajes
            Thread lector = new Thread(() -> {
                try {
                    String line;
                    while ((line = brSala.readLine()) != null) {
                        System.out.println(line);
                    }
                    System.out.println("La partida ha terminado.");
                } catch (IOException ignored) {}
                finally {
                    try { sala.close(); } catch (IOException ignored) {}
                }
            });
            lector.start();

            // Enviar datos del jugador y jugadas
            while (!sala.isClosed()) {
                if (!sc.hasNextLine()) break;
                pwSala.println(sc.nextLine());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
