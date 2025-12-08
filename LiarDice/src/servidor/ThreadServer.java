package servidor;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * ThreadServer CORRECTO: solo lee el tipo de partida y entrega el socket a la sala.
 * No cierra el socket ni crea readers/writers que consuman extra del stream.
 */
public class ThreadServer implements Runnable {
    private final Socket cliente;
    private final ServidorAdd sala2;
    private final ServidorAdd sala3;
    private final ServidorAdd sala4;

    public ThreadServer(Socket cliente, ServidorAdd s2, ServidorAdd s3, ServidorAdd s4) {
        this.cliente = cliente;
        this.sala2 = s2;
        this.sala3 = s3;
        this.sala4 = s4;
    }

    @Override
    public void run() {
        try {
            // SOLO leer el tipo de partida
            BufferedReader br = new BufferedReader(new InputStreamReader(cliente.getInputStream()));
            PrintWriter pw = new PrintWriter(cliente.getOutputStream(), true);

            pw.println("Elige tipo de partida (2/3/4):");

            String tipo;
            while ((tipo = br.readLine()) != null) {
                tipo = tipo.trim();
                if (tipo.equals("2") || tipo.equals("3") || tipo.equals("4")) break;
                pw.println("Tipo inválido. Elige 2, 3 o 4:");
            }

            if (tipo == null) {
                // cliente se desconectó antes de elegir
                try { cliente.close(); } catch (Exception ignored) {}
                return;
            }

            ServidorAdd salaDestino = switch (tipo) {
                case "2" -> sala2;
                case "3" -> sala3;
                case "4" -> sala4;
                default -> sala2;
            };

            // DEBUG
            System.out.println("DEBUG: jugador conectado, tipo=" + tipo + " socket=" + cliente);

            // ENTREGAR el mismo socket a la sala — NO cerramos ni leemos más
            salaDestino.agregarJugador(cliente);

        } catch (Exception e) {
            e.printStackTrace();
            try { cliente.close(); } catch (Exception ignored) {}
        }
    }
}
