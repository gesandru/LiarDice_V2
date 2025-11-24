package servidor;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

import objetos.Dado;
import objetos.Jugador;

public class ThreadServer implements Runnable {
    private Socket socket;

    public ThreadServer(Socket s) {
        this.socket = s;
    }

    //Aquí se debería manejar todo el juego
    public void run() {
        try (ObjectOutputStream ow = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream objr = new ObjectInputStream(socket.getInputStream())) {
            ArrayList<Jugador> jugadores = (ArrayList<Jugador>) objr.readObject();
            ArrayList<Dado> todosDados;
            int dadosJugadorAnterior;
            //j es el numero de jugadores con manos vacias, es decir, sin dados
            int i, j, k, l;
            int dado1 = 0, dado2 = 0, dado3 = 0, dado4 = 0, dado5 = 0, dado6 = 0, contador = 0;
            boolean winner = false;
            boolean ronda = false;
            String s = "";
            String[] splitter;
            String apuesta = "0 d1";
            Jugador jugadorAnterior = null;
            while (!winner) {
                i = 0;
                j = 0;
                ronda = false;
                todosDados = new ArrayList<Dado>();
                while (i < jugadores.size()) {
                    jugadores.get(i).Nuevoturno();
                    todosDados.addAll(jugadores.get(i).getMano().getDados());
                    if (jugadores.get(i).getMano().Vacio()) {
                        j++;
                    }
                    i++;
                }
                if (j == jugadores.size() - 1) {
                    ronda = true;
                }
                ow.writeInt(todosDados.size());
                ow.flush();
                //RESET AQUI
                ow.reset();
                ow.writeObject(jugadores);
                ow.flush();

                dadosJugadorAnterior = jugadores.get(jugadores.size() - 1).getDadosEnMano();
                while (!ronda) {
                    i = i % jugadores.size();
                    if (!jugadores.get(i).getMano().Vacio()) {
                        ow.writeInt(dadosJugadorAnterior);
                        ow.flush();
                        s = (String) objr.readObject();

                        if (s.startsWith("M")) {
                            splitter = apuesta.split("d");
                            k = Character.getNumericValue(splitter[0].charAt(0));
                            l = Character.getNumericValue(splitter[1].charAt(0));
                            dado1 = 0;
                            dado2 = 0;
                            dado3 = 0;
                            dado4 = 0;
                            dado5 = 0;
                            dado6 = 0;
                            contador = 0;
                            while (contador < todosDados.size()) {
                                switch (todosDados.get(contador).getNumero()) {
                                    case 1 -> dado1++;
                                    case 2 -> dado2++;
                                    case 3 -> dado3++;
                                    case 4 -> dado4++;
                                    case 5 -> dado5++;
                                    case 6 -> dado6++;
                                }
                                contador++;
                            }
                            switch (l) {
                                case 1:
                                    if (k > dado1) {
                                        jugadorAnterior.QuitarDado();
                                    } else {
                                        jugadores.get(i).QuitarDado();
                                    }
                                    break;
                                case 2:
                                    if (k > dado2) {
                                        jugadorAnterior.QuitarDado();
                                    } else {
                                        jugadores.get(i).QuitarDado();
                                    }
                                    break;
                                case 3:
                                    if (k > dado3) {
                                        jugadorAnterior.QuitarDado();
                                    } else {
                                        jugadores.get(i).QuitarDado();
                                    }
                                    break;
                                case 4:
                                    if (k > dado4) {
                                        jugadorAnterior.QuitarDado();
                                    } else {
                                        jugadores.get(i).QuitarDado();
                                    }
                                    break;
                                case 5:
                                    if (k > dado5) {
                                        jugadorAnterior.QuitarDado();
                                    } else {
                                        jugadores.get(i).QuitarDado();
                                    }
                                    break;
                                case 6:
                                    if (k > dado6) {
                                        jugadorAnterior.QuitarDado();
                                    } else {
                                        jugadores.get(i).QuitarDado();
                                    }
                                    break;
                            }
                            ronda = true;
                            ow.writeBoolean(true);
                            ow.flush();
                        } else {
                            apuesta = s;
                            jugadorAnterior = jugadores.get(i);
                            dadosJugadorAnterior = jugadorAnterior.getDadosEnMano();
                            ow.writeBoolean(false);
                            ow.flush();
                        }
                    }
                    i++;
                }
                //vuelve a comprobar aquí si se ha terminado la partida
                i = 0;
                j = 0;
                while (i < jugadores.size()) {
                    if (jugadores.get(i).getMano().Vacio()) {
                        j++;
                    }
                    i++;
                }
                if (j == jugadores.size() - 1) {
                    winner = true;
                }
                ow.writeBoolean(winner);
                ow.flush();
            }
            String name = "Empty";
            i = 0;
            while (i < jugadores.size()) {
                if (!jugadores.get(i).getMano().Vacio()) {
                    name = jugadores.get(i).getName();
                }
                i++;
            }
            ow.writeObject(name);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}