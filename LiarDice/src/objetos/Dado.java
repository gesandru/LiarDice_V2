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


    @Override
    public String toString() {
        return "Dado{numero=" + this.numero + '}';
    }



    public void mostrar(){
        System.out.println("d"+this.numero);}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Dado)) return false;
        Dado d = (Dado) o;
        return d.numero == this.numero;
    }
}
