package servidor;

import java.io.*;
import java.net.Socket;

public class ThreadServer implements Runnable {

    private final Socket cliente;
    private final ServidorAdd s2, s3, s4;

    public ThreadServer(Socket c, ServidorAdd a, ServidorAdd b, ServidorAdd d) {
        this.cliente = c;
        this.s2 = a;
        this.s3 = b;
        this.s4 = d;
    }

    @Override
    public void run() {
        try (
                BufferedReader br = new BufferedReader(new InputStreamReader(cliente.getInputStream()));
                PrintWriter pw = new PrintWriter(cliente.getOutputStream(), true)
        ) {

            pw.println("Elige tipo de partida (2/3/4):");

            String tipo;
            while (true) {
                tipo = br.readLine();
                if (tipo == null) return;
                if (tipo.equals("2") || tipo.equals("3") || tipo.equals("4")) break;
                pw.println("Tipo inválido. Elige 2, 3 o 4:");
            }

            ServidorAdd destino = tipo.equals("2") ? s2 : (tipo.equals("3") ? s3 : s4);

            pw.println("OK:" + destino.getPuerto());

        } catch (IOException ignored) {
        } finally {
            try { cliente.close(); } catch (IOException ignored) {}
        }
    }
}
