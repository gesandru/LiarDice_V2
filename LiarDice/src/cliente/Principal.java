package cliente;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;

import objetos.Jugador;

//Cambiar esto para que todo sea manejado en el servidor, el cliente solo debe tomar las decisiones
public class Principal {
    public static void main(String[] args) {
        Jugador j1;
        Jugador j2;
        Jugador j3;
        Jugador j4;
        ArrayList<Jugador> jugadores = new ArrayList<Jugador>();
        Scanner sc = new Scanner(System.in);
        int i;
        boolean b = false;
        String s;
        while(!b){
            System.out.println("Introduce el numero de jugadores: 2-4");
            i = sc.nextInt();
            switch (i) {
                case 2:
                    System.out.println("Introduce el nombre del primer jugador: ");
                    s = sc.next();
                    j1 = new Jugador(s);
                    jugadores.add(j1);
                    System.out.println("Introduce el nombre del segundo jugador: ");
                    s = sc.next();
                    j2 = new Jugador(s);
                    jugadores.add(j2);
                    b = true;
                    break;
                case 3:
                    System.out.println("Introduce el nombre del primer jugador: ");
                    s = sc.next();
                    j1 = new Jugador(s);
                    jugadores.add(j1);
                    System.out.println("Introduce el nombre del segundo jugador: ");
                    s = sc.next();
                    j2 = new Jugador(s);
                    jugadores.add(j2);
                    System.out.println("Introduce el nombre del tercer jugador: ");
                    s = sc.next();
                    j3 = new Jugador(s);
                    jugadores.add(j3);
                    b = true;
                    break;
                case 4:
                    System.out.println("Introduce el nombre del primer jugador: ");
                    s = sc.next();
                    j1 = new Jugador(s);
                    jugadores.add(j1);
                    System.out.println("Introduce el nombre del segundo jugador: ");
                    s = sc.next();
                    j2 = new Jugador(s);
                    jugadores.add(j2);
                    System.out.println("Introduce el nombre del tercer jugador: ");
                    s = sc.next();
                    j3 = new Jugador(s);
                    jugadores.add(j3);
                    System.out.println("Introduce el nombre del cuarto jugador: ");
                    s = sc.next();
                    j4 = new Jugador(s);
                    jugadores.add(j4);
                    b = true;
                    break;
                default:
                    System.out.println("No es una opcion valida");
            }
        }
        Game(jugadores);
    }

    private static void Game(ArrayList<Jugador> jugadores) {
        try (Socket s = new Socket("localhost", 55555);
             ObjectInputStream objr = new ObjectInputStream(s.getInputStream());

             ObjectOutputStream ow = new ObjectOutputStream(s.getOutputStream())) {
            ow.writeObject(jugadores);
            ow.flush();
            int i;
            //Esto puede ser la apuesta o llamar al jugador anterior mentiroso
            String apuesta;
            //Esto indica que el juego ha terminado
            boolean winner = false;
            //Esto indica si la ronda se ha terminado
            boolean ronda = false;
            int dadosTotales;
            int dadosJugadorAnterior;
            while (!winner) {
                i = 0;
                dadosTotales = objr.readInt();
                jugadores = (ArrayList<Jugador>) objr.readObject();
                apuesta = "0 d1";
                ronda = false;

                while (!ronda) {
                    i = i % jugadores.size();
                    if(!jugadores.get(i).getMano().Vacio()){
                        dadosJugadorAnterior = objr.readInt();
                        //La apuesta debe ser de la forma x dx
                        // ej: 2 d4 significa creo que hay 2 dados con la cara 4 entre todos los dados.
                        apuesta = jugadores.get(i).Turno(apuesta, dadosTotales, dadosJugadorAnterior);
                        ow.writeObject(apuesta);
                        ow.flush();
                        ronda = objr.readBoolean();
                    }
                    i++;
                }
                winner = objr.readBoolean();
            }
            String name = (String) objr.readObject();
            System.out.println("El ganador es " + name);

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
