package servidor;

import java.io.*;
import java.net.Socket;
import java.util.List;

public class ThreadServer implements Runnable {
    private Socket cliente;
    List<Integer> replicas;
    int contador;

    public ThreadServer(Socket s, List<Integer> r, int c) {
        this.cliente = s;
        this.replicas = r;
        this.contador = c;
    }

    //Indicamos a qué servidor mandarlo, por ahora se va mandando secuencialmente, 
    //posible mejorarlo con un algorítmo pero es suficiente.
    @Override
    public void run() {
        Socket backend = null;

        try {
            // vamos mandando a cada servidor un cliente
            int port = replicas.get(contador % replicas.size());
            System.out.println("Thread " + contador + ", replica port " + port);

            backend = new Socket("localhost", port);
            contador++;

            // Conectamos al cliente con uno de los servidores para el juego
            Thread t1 = proxy(cliente.getInputStream(), backend.getOutputStream());
            Thread t2 = proxy(backend.getInputStream(), cliente.getOutputStream());

            // esperamos a que termine
            t1.join();
            t2.join();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try { cliente.close(); } catch (Exception ignored) {}
            try { if (backend != null) backend.close(); } catch (Exception ignored) {}
        }
    }

    private Thread proxy(InputStream in, OutputStream out) {
        Thread t = new Thread(() -> {
            try {
                byte[] buf = new byte[4096];
                int len;
                while ((len = in.read(buf)) != -1) {
                    out.write(buf, 0, len);
                    out.flush();
                }
            } catch (IOException ignored) {}
        });

        t.start();
        return t;
    }
}