package carte;

import java.util.ArrayList;
import java.util.List;

public class CarteMission {

    private int nbTerritoire;
    private int nbArmee;
    private List<String> continentAConquerir;
    private String couleur; // A voir ultérieurement


    public CarteMission(String cont1, String cont2){                    // Constructeur si la Carte Mission est deux continents à conquérir
        continentAConquerir = new ArrayList<>();
        continentAConquerir.add(cont1);
        continentAConquerir.add(cont2);
    }

    public CarteMission(String cont1, String cont2, String autre){      // Constructeur si la Carte Mission est trois continents à conquérir
        continentAConquerir = new ArrayList<>();
        continentAConquerir.add(cont1);
        continentAConquerir.add(cont2);
        continentAConquerir.add(autre);
    }

    public CarteMission(int nbTerritoire){                              // Constructeur si la Carte Mission est un certain nombre de Territoires à controller
        this.nbTerritoire = nbTerritoire;
    }

    public CarteMission(int nbTerritoire, int nbArmee){                 // Constructeur si la Carte Mission est un certain nombre de Territoires à controller
        this.nbTerritoire = nbTerritoire;                               // et qu'il y a un certain nombre d'armées sur chaque Territoire
        this.nbArmee = nbArmee;
    }

    public CarteMission(String couleur){                                // Constructeur si la Carte Mission est un Joueur à éliminer
        this.couleur = couleur;
        nbTerritoire = 24;
    }

    public int getNbTerritoire() {
        return nbTerritoire;
    }

    public void setNbTerritoire(int nbTerritoire) {
        this.nbTerritoire = nbTerritoire;
    }

    public int getNbArmee() {
        return nbArmee;
    }

    public void setNbArmee(int nbArmee) {
        this.nbArmee = nbArmee;
    }

    public List<String> getContinentAConquerir() {
        return continentAConquerir;
    }

    public void setContinentAConquerir(List<String> continentAConquerir) {
        this.continentAConquerir = continentAConquerir;
    }

    public String getCouleur() {
        return couleur;
    }

    public void setCouleur(String couleur) {
        this.couleur = couleur;
    }

    @Override
    public String toString() {
        return "\n\tCarteMission{" +
                "nbTerritoire=" + nbTerritoire +
                ", nbArmee=" + nbArmee +
                ", continentAConquerir=" + continentAConquerir +
                ", couleur='" + couleur + '\'' +
                '}';
    }
}
