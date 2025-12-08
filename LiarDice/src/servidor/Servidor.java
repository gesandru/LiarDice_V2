package servidor;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Servidor {
    public static void main(String[] args) {

        ServidorAdd sala2 = new ServidorAdd("Sala 2 jugadores", 2, 55556);
        ServidorAdd sala3 = new ServidorAdd("Sala 3 jugadores", 3, 55557);
        ServidorAdd sala4 = new ServidorAdd("Sala 4 jugadores", 4, 55558);

        sala2.iniciar();
        sala3.iniciar();
        sala4.iniciar();

        ExecutorService pool = Executors.newCachedThreadPool();

        try (ServerSocket ss = new ServerSocket(55555)) {
            System.out.println("Proxy escuchando en 55555");

            while (true) {
                Socket c = ss.accept();
                pool.submit(new ThreadServer(c, sala2, sala3, sala4));
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            pool.shutdown();
        }
    }
}
