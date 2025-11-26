package servidor;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class ServidorAdd1 {


	public static void main(String[] args) throws ClassNotFoundException {
        ExecutorService pool = Executors.newCachedThreadPool();
        try(ServerSocket ss = new ServerSocket(55556)){
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

