package servidor;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServidorAdd1 {

    public static void main(String[] args) {
        ExecutorService pool = Executors.newCachedThreadPool();
        final int PORT = 55556;

        try (ServerSocket ss = new ServerSocket(PORT)) {
            System.out.println("Servidor escuchando en puerto " + PORT);
            while (true) {
                try {
                    Socket s = ss.accept();
                    System.out.println("Cliente conectado desde " + s.getRemoteSocketAddress());
                    ThreadAdd handler = new ThreadAdd(s);
                    pool.execute(handler);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            pool.shutdown();
        }
    }
}
