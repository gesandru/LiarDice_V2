package objetos;

import java.io.Serializable;
import java.util.Scanner;

public class Jugador implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final String name;
    private int dadosEnMano;
    private Mano mano;
    private Scanner scanner;

    public Jugador(String s){
        this.name=s;
        this.mano = new Mano(5);
        this.dadosEnMano = 5;
    }

    //Cada jugador toma su turno, recibe la ultima apuesta y el numero total de dados y decide si hacer otra
    //que debe ser mayor que la anterior en cantidad o en número
    //o llamar mentiroso al anterior jugador
    //El jugador puede aumentar la cara pero no decrementar la cantidad
    //El jugador deberá escribir la apuesta con el formato "(numero) d(cara)"
    //Obviamente el jugador con el primer turno no puede llamar a nadie mentiroso
    public String Turno(String ultimaApuesta, int dadosTotales, int dadosJugadorAnterior){
        if(!(this.dadosEnMano==0)) {
            this.scanner = new Scanner(System.in).useDelimiter("\n");
            boolean b = true;
            int i, k, l, n, m;
            String s = "";
            String[] sa;
            String[] sa2;
            StringBuilder sb = new StringBuilder("\n");
            sb.append(this.name);
            StringBuilder sbError = new StringBuilder("Esa no es una opcion");
            sbError.append("\n1. Apostar\n");
            sbError.append("2. Llamar mentiroso");
            StringBuilder apuestaIncorrecta = new StringBuilder("Esa no es una apuesta valida");
            apuestaIncorrecta.append("\n1. Apostar\n");
            apuestaIncorrecta.append("2. Llamar mentiroso");
            sb.append("\n");
            sb.append("La ultima apuesta fue ");
            sb.append(ultimaApuesta);
            sb.append("\nEl numero total de dados en la mesa es ");
            sb.append(dadosTotales);
            sb.append("\nEl numero de dados del jugador anterior es ");
            sb.append(dadosJugadorAnterior);
            sb.append("\nTus dados son: \n");
            sb.append(this.mano.Mostrar());
            sb.append("\n1. Apostar\n");
            sb.append("2. Llamar mentiroso");
            System.out.println(sb);
            while (b){
            	String opcion = scanner.next();
            	//Hay que evitar que tomen opciones no válidas
            	if (!opcion.matches("[12]")) {
            	    System.out.println(sbError);
            	    continue;
            	}
            	i = Integer.parseInt(opcion);
                switch (i) {
                    case 1:
                        s = scanner.next();
                        sa = s.split("d");
                        //Comprueba que la apuesta sea válida
                        if (sa.length != 2 || !sa[0].matches("\\d+") || !sa[1].matches("\\d+")) {
                            System.out.println(apuestaIncorrecta);
                            continue;
                        }
                        //Comprueba que la apuesta previa sea válida
                        if (!ultimaApuesta.matches("\\d+d\\d+")) {
                            System.out.println("La apuesta anterior no es válida: " + ultimaApuesta);
                            return "ERROR"; // o alguna acción apropiada
                        }
                        sa2 = ultimaApuesta.split("d");
                        //Cambiado la conversión por una más simple
                        k = Integer.parseInt(sa[0]);
                        l = Integer.parseInt(sa[1]);
                        n = Integer.parseInt(sa2[0]);
                        m = Integer.parseInt(sa2[1]);
                        if(k>0){
                            if(k==n){
                                if(l>m){
                                    b = false;
                                }
                            }
                            else if(k>n){
                                if(k<=dadosTotales){
                                    if(l>=m){
                                        b = false;
                                    }
                                }
                            }
                        }
                        if(b){
                            System.out.println(apuestaIncorrecta);
                        }
                        break;
                    case 2:
                        s = "MIENTES!";
                        b = false;
                        break;
                    default:
                        System.out.println(sbError);
                }
            }
            return s;
        }
        return "No me quedan dados";
    }

    public void Nuevoturno(){
        this.mano = new Mano(this.dadosEnMano);
    }

    public String getName(){return this.name;}

    public int getDadosEnMano(){
        return this.dadosEnMano;
    }

    public void QuitarDado(){
        this.dadosEnMano = this.dadosEnMano-1;
    }

    public Mano getMano() {
        return this.mano;
    }
}
