package servidor;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//Podría añadir que el servidor se divida en 3 servidores, cada uno con su propio pool
//de threads, para manejar mejor cargas
public class Servidor {
	
	// Una lista de servidores replicados 
	private static final List<Integer> replicas = Arrays.asList(55556, 55557, 55558);

	private static int contador = 0;

    public static void main(String[] args) throws ClassNotFoundException {
        ExecutorService pool = Executors.newCachedThreadPool();
        try(ServerSocket ss = new ServerSocket(55555)){
            while(true) {
                try{
                    Socket s = ss.accept();
                    ThreadServer ts = new ThreadServer(s, replicas, contador);
                    pool.execute(ts);
                    contador++;

                }
                catch(Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            pool.shutdown();
        }
    }
}