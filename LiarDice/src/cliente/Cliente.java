//Clase innecesaria

/*package cliente;

import java.io.*;
import java.net.Socket;

public class Cliente {

    private Socket socket;
    private BufferedReader br;
    private PrintWriter pw;

    public void conectar() throws Exception {
        socket = new Socket("localhost", 55555);
        br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        pw = new PrintWriter(socket.getOutputStream(), true);
    }

    public void enviar(String msg) {
        pw.println(msg);
    }

    public String recibir() throws Exception {
        return br.readLine();
    }

    public BufferedReader getBR() {
        return br;
    }

    public PrintWriter getPW() {
        return pw;
    }
}
*/