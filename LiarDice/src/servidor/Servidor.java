package servidor;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Servidor {

    public static void main(String[] args) {

        // Salas del servidor
        ServidorAdd sala2 = new ServidorAdd("Sala de 2 jugadores", 2, 55556);
        ServidorAdd sala3 = new ServidorAdd("Sala de 3 jugadores", 3, 55557);
        ServidorAdd sala4 = new ServidorAdd("Sala de 4 jugadores", 4, 55558);

        sala2.iniciar();
        sala3.iniciar();
        sala4.iniciar();

        int puertoProxy = 55555;
        ExecutorService pool = Executors.newCachedThreadPool();

        try (ServerSocket serverSocket = new ServerSocket(puertoProxy)) {
            System.out.println("Servidor proxy escuchando en puerto " + puertoProxy);

            while (true) {
                Socket cliente = serverSocket.accept();
                pool.submit(new ThreadServer(cliente, sala2, sala3, sala4));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
