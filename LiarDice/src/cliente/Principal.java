package cliente;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

/*
 * Cambios principales del juego respecto a la versión 1:
 * Cambio de la lógica del juego para que todo se trate dentro del thread del servidor,
 * en la versión anterior era un lío con ObjectOutputStream y creación de jugadores y trato de jugadores dentro del cliente.
 * En la nueva versión el cliente solo toma decisiones y para eso solo necesitamos un reader y un writer.
 * 
 * Cambio de la clase Servidor a que sea un proxy inverso, indicandole al cliente donde
 * están los servidores donde está la lógica del juego.
 * 
 * Cambio de la implementación para que cada cliente represente un solo jugador, no la partida completa.
 * Por lo que tuve que cambiar la lógica de todo.
 */

/* Funcionamiento general del programa:
 * Primero se lanza Servidor que se queda esperando a que se conecten clientes.
 * Cada conexión de Principal representa un jugador.
 * Al conectarse un jugador le indíca qué tipo de partida quiere jugar (cúantos jugadores quiere que haya en la partida)
 * El Servidor le indica al jugador a qué puerto de ServidorAdd debe conectarse
 * Al conectarse al ServidorAdd se queda esperando y cuando hay suficientes jugadores conectados crea una sala
 * y empieza el juego.
 * 
 * Problemas: 
 * Una vez terminada la partida hay que desconectarse manualmente. El Servidor muestra errores pero se cierra la conexión
 * pero sigue funcionando perfectamente.
 * 
 * Los cierres de algunas cosas son raros pero es necesario hacerlo así o no funciona. Aún así, todo debería cerrarse 
 * correctamente al cerrar el socket del cliente (de nuevo, el Servidor muestra errores pero sigue funcionando perfectamente).
 */
public class Principal {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        try (
                Socket proxy = new Socket("localhost", 55555);
                BufferedReader br = new BufferedReader(new InputStreamReader(proxy.getInputStream()));
                PrintWriter pw = new PrintWriter(proxy.getOutputStream(), true)
        ) {

            // Pregunta tipo de partida
            System.out.println(br.readLine());
            String tipo = sc.nextLine();
            pw.println(tipo);

            // Recibir puerto de sala
            String respuesta = br.readLine(); // ej: OK:55557
            int puertoSala = Integer.parseInt(respuesta.split(":")[1].trim());

            // Conectarse a la sala
            Socket sala = new Socket("localhost", puertoSala);
            BufferedReader brSala = new BufferedReader(new InputStreamReader(sala.getInputStream()));
            PrintWriter pwSala = new PrintWriter(sala.getOutputStream(), true);

            // Hilo de lectura de mensajes
            Thread lector = new Thread(() -> {
                try {
                    String line;
                    while ((line = brSala.readLine()) != null) {
                        System.out.println(line);
                    }
                    System.out.println("La partida ha terminado.");
                } catch (IOException ignored) {}
                finally {
                    try { sala.close(); } catch (IOException ignored) {}
                }
            });
            lector.start();

            // Enviar datos del jugador y jugadas
            while (!sala.isClosed()) {
                if (!sc.hasNextLine()) break;
                pwSala.println(sc.nextLine());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
