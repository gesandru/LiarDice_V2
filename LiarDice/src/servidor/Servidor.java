package servidor;

import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Servidor {

    private static final int Puerto = 55555;
    //Pool de 20 threads para manejar redirecciones
    
    
    public static void main(String[] args) throws Exception {

    	ExecutorService pool = Executors.newFixedThreadPool(20);
    	
        //Lanzar salas del juego en puertos distintos
        new Thread(() -> new ServidorAdd("Sala 1", 55556).start()).start();
        new Thread(() -> new ServidorAdd("Sala 2", 55557).start()).start();
        new Thread(() -> new ServidorAdd("Sala 3", 55558).start()).start();

        System.out.println("SERVIDOR PROXY escuchando en puerto " + Puerto);

        ServerSocket ss = new ServerSocket(Puerto);

        
        try {
        while (true) {
            Socket cliente = ss.accept();
            
				pool.submit(new ThreadServer(cliente));
			}
        } catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			pool.shutdown();
		}
    }
}
