package objetos;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

public class Mano implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ArrayList<Dado> dados;
    private Random r = new Random();

    //La mano tendrá que cambiarse para cada jugador al principio de cada turno
    // y el servidor deberá tener la información de todos los dados
    public Mano(int i){
        if(i>0){
            //Creo un array con las 6 posibles caras de los dados
            ArrayList<Dado> todosLosDados = new ArrayList<Dado>();
            Dado d = null;
            int n = 1;
            while(n<=6){
                d = new Dado(n);
                todosLosDados.add(d);
                n++;
            }

            TirarDados(i, todosLosDados);
        }
        else{
            this.dados = new ArrayList<Dado>();
        }
    }

    //Aquí toma todos los posibles dados y va metiendo aleatoriamente dados hasta llegar al
    // número de dados que debería tener la mano
    private void TirarDados(int i, ArrayList<Dado> t){
        this.dados = new ArrayList<Dado>();
        int n=0;
        int j = 0;
        Dado aux;
        while(j<i){
            n = r.nextInt(6);
            //Corregido un error que hacía que todos los jugadores usaban los mismos datos
            aux = new Dado(t.get(n).getNumero());
            this.dados.add(aux);
            j++;
        }
    }

    public ArrayList<Dado> getDados(){
        return this.dados;
    }

    //Tiene que haber una mejor forma de hacer esto pero no se me ocurre
    public StringBuilder Mostrar(){
        StringBuilder sb = new StringBuilder();
        int dado1 = 0, dado2 = 0, dado3 = 0, dado4 = 0, dado5 = 0, dado6 = 0, contador = 0;
        while(contador<dados.size()){
            switch (dados.get(contador).getNumero()){
                case 1:
                    dado1++;
                    break;
                case 2:
                    dado2++;
                    break;
                case 3:
                    dado3++;
                    break;
                case 4:
                    dado4++;
                    break;
                case 5:
                    dado5++;
                    break;
                case 6:
                    dado6++;
                    break;
            }
            contador++;
        }
        if(dado1>0){
            sb.append(dado1);
            sb.append(" d1 ");
        }
        if(dado2>0){
            sb.append(dado2);
            sb.append(" d2 ");
        }
        if(dado3>0){
            sb.append(dado3);
            sb.append(" d3 ");
        }
        if(dado4>0){
            sb.append(dado4);
            sb.append(" d4 ");
        }
        if(dado5>0){
            sb.append(dado5);
            sb.append(" d5 ");
        }
        if(dado6>0){
            sb.append(dado6);
            sb.append(" d6 ");
        }
        return sb;
    }

    public boolean Vacio(){
        return this.dados.isEmpty();
    }
}
