package objetos;

import java.io.Serializable;

public class Dado implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int numero;

    public Dado(int n){
        this.numero=n;
    }

    public int getNumero() {
        return this.numero;
    }


    public String toString() {
        return "Carta{" +
                ", numero=" + this.numero +
                '}';}


    public void mostrar(){
        System.out.println("d"+this.numero);}

    public boolean equals(Dado d){
        if(d.getNumero()==this.getNumero()){
            return true;
        }
        else{
            return false;
        }
    }
}
