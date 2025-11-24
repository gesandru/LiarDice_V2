package servidor;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//Podría añadir que el servidor se divida en 3 servidores, cada uno con su propio pool
//de threads, para manejar mejor cargas
public class Servidor {
    public static void main(String[] args) throws ClassNotFoundException {
        ExecutorService pool = Executors.newCachedThreadPool();
        try(ServerSocket ss = new ServerSocket(55555)){
            while(true) {
                try{
                    Socket s = ss.accept();
                    ThreadServer ts = new ThreadServer(s);
                    pool.execute(ts);

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