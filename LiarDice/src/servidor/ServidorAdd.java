package servidor;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServidorAdd {

    private final String nombre;
    private final int puerto;

    public ServidorAdd(String nombre, int puerto) {
        this.nombre = nombre;
        this.puerto = puerto;
    }

    public void start() {
    	// Pool de threads para manejar partidas
        ExecutorService pool = Executors.newFixedThreadPool(10);
        
        try {
            ServerSocket ss = new ServerSocket(puerto);
            System.out.println(nombre + " escuchando en puerto " + puerto);

            

            while (true) {
                Socket cliente = ss.accept();
                pool.submit(new ThreadAdd(cliente));
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        	pool.shutdown();
        }
    }
}
