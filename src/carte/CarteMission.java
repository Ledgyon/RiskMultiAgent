package carte;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CarteMission implements Serializable {

    private int nbTerritoire;
    private int nbArmee;
    private List<String> continentAConquerir;
    private String couleur;
    private String typeMission;


    public CarteMission(String cont1, String cont2, String typeMission){                    // Constructeur si la Carte Mission est deux continents à conquérir
        continentAConquerir = new ArrayList<>();
        continentAConquerir.add(cont1);
        continentAConquerir.add(cont2);
        this.typeMission = typeMission;
    }

    public CarteMission(String cont1, String cont2, String autre, String typeMission){      // Constructeur si la Carte Mission est trois continents à conquérir
        continentAConquerir = new ArrayList<>();
        continentAConquerir.add(cont1);
        continentAConquerir.add(cont2);
        continentAConquerir.add(autre);
        this.typeMission = typeMission;
    }

    public CarteMission(int nbTerritoire, String typeMission){                              // Constructeur si la Carte Mission est un certain nombre de Territoires à controller
        this.nbTerritoire = nbTerritoire;
        this.typeMission = typeMission;
    }

    public CarteMission(int nbTerritoire, int nbArmee, String typeMission){                 // Constructeur si la Carte Mission est un certain nombre de Territoires à controller
        this.nbTerritoire = nbTerritoire;                               // et qu'il y a un certain nombre d'armées sur chaque Territoire
        this.nbArmee = nbArmee;
        this.typeMission = typeMission;
    }

    public CarteMission(String couleur, String typeMission){                                // Constructeur si la Carte Mission est un Joueur à éliminer
        this.couleur = couleur;
        nbTerritoire = 24;
        this.typeMission = typeMission;
    }

    public int getNbTerritoire() {
        return nbTerritoire;
    }

    public int getNbArmee() {
        return nbArmee;
    }

    public List<String> getContinentAConquerir() {
        return continentAConquerir;
    }

    public String getCouleur() {
        return couleur;
    }

    public String getTypeMission() {
		return typeMission;
	}

    @Override
    public String toString() {
        String temp = "\n\tCarteMission [";
        if(couleur != null){
            temp += "couleur = " + couleur + ", objectif secondaire, nbTerritoire = " + nbTerritoire;
        } else if (nbTerritoire == 24) {
            temp += "nbTerritoire = " + nbTerritoire;
        } else if (nbTerritoire == 18) {
            temp += "nbTerritoire = " + nbTerritoire + " avec = " + nbArmee + " armees par territoire";
        } else
            temp += "Continent a conquerir = " + continentAConquerir;
        temp+="]";
        return temp;
    }
}
