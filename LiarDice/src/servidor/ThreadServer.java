package servidor;

import java.io.PrintWriter;
import java.net.Socket;

public class ThreadServer implements Runnable {

    private final Socket socket;

    public ThreadServer(Socket s) {
        this.socket = s;
    }

    @Override
    public void run() {
        try {
            PrintWriter pw = new PrintWriter(socket.getOutputStream(), true);

            // Balanceo simple (al azar)
            int puerto = 55555 + (int) (Math.random() * 3);

            pw.println("REDIRECT:" + puerto);
            pw.flush();

            socket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
