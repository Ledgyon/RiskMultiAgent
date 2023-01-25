package plateau;

import plateau.enumerations.NomTerritoireOC;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Territoire {
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

    @Override
    public String toString() {
        return "Territoire{" +
                "nomTerritoire='" + nomTerritoire + '\'' +
                ", territoires_adjacents=" + territoires_adjacents +
                '}';
    }
}
