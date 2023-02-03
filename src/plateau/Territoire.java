package plateau;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Territoire implements Serializable {
    private String nomTerritoire;
    private List<Territoire> territoires_adjacents;
    private int regimentSurTerritoire;

    public Territoire(String nomTerritoire) {
        this.nomTerritoire = nomTerritoire;
        territoires_adjacents = new ArrayList<>();
    }
    public void setTerritoires_adjacents(List<Territoire> territoires_adjacents) {
        this.territoires_adjacents.addAll(territoires_adjacents);
    }

    public String getNomTerritoire() {
        return nomTerritoire;
    }

    public void setNomTerritoire(String nomTerritoire) {
        this.nomTerritoire = nomTerritoire;
    }

    public List<Territoire> getTerritoires_adjacents() {
        return territoires_adjacents;
    }

    public int getRegimentSurTerritoire() {
        return regimentSurTerritoire;
    }

    public void setRegimentSurTerritoire(int regimentSurTerritoire) {
        this.regimentSurTerritoire = regimentSurTerritoire;
    }

    @Override
    public String toString() {
        return "\n\t\tTerritoire{" +
                "nomTerritoire = '" + nomTerritoire + '\'' +
                toStringTerritoireAdjacents() +
                "regimentSurTerritoire = " + regimentSurTerritoire + '}';
    }

    public String toStringTerritoireAdjacents(){
        StringBuilder renvoie= new StringBuilder(", territoireAdjacent = [ ");
        for(Territoire ter:territoires_adjacents){
            renvoie.append(ter.getNomTerritoire());
            if(!territoires_adjacents.get(territoires_adjacents.size()-1).getNomTerritoire().equals(ter.getNomTerritoire()))
                renvoie.append(", ");
        }
        renvoie.append(" ]");
        return renvoie.toString();
    }

    public void addRegimentSurTerritoire(int i) {
        this.regimentSurTerritoire+=i;
    }
}
