/*

package servidor;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServidorAdd3 {

	public static void main(String[] args) throws ClassNotFoundException {
        ExecutorService pool = Executors.newCachedThreadPool();
        try(ServerSocket ss = new ServerSocket(55558)){
            while(true) {
                try{
                    Socket s = ss.accept();
                    ThreadAdd ts = new ThreadAdd(s);
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
*/