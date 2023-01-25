package plateau;

import plateau.enumerations.*;

import java.util.ArrayList;
import java.util.List;

public class Continent {
    private String nom;
    private List<Territoire> territoires;
    private int renfortObtenu;

    public Continent(String nom) {
        this.nom = nom;
        assignRenfort(nom);         // Permet d'assigner un nombre de renfort obtenue au début de chaque tour dépendant du continent
        assignTerritoire(nom);      // Permet d'assigner les noms de territoires pour chaque continent
    }

    // Fonction qui permet d'assigner un nombre de renfort obtenue au début de chaque tour dépendant du continent
    public void assignRenfort (String nom){
        switch (nom) {
            case "AMERIQUE_NORD", "EUROPE" -> this.renfortObtenu = 5;
            case "AMERIQUE_SUD", "OCEANIE" -> this.renfortObtenu = 2;
            case "AFRIQUE" -> this.renfortObtenu = 3;
            case "ASIE" -> this.renfortObtenu = 7;
        }
    }

    // Fonction qui permet d'assigner les noms de territoires pour chaque continent
    public void assignTerritoire (String nom){
        switch (nom) {
            case "AMERIQUE_NORD" -> {
                territoires = new ArrayList<>();
                NomTerritoireAN[] nomTerritoireAN = NomTerritoireAN.values();
                for (int i = 0; i < 9; i++) {
                    Territoire t = new Territoire(nomTerritoireAN[i].toString());
                    this.territoires.add(t);
                }
            }
            case "AMERIQUE_SUD" -> {
                territoires = new ArrayList<>();
                NomTerritoireAS[] nomTerritoireAS = NomTerritoireAS.values();
                for (int i = 0; i < 4; i++) {
                    Territoire t = new Territoire(nomTerritoireAS[i].toString());
                    this.territoires.add(t);
                }
            }
            case "AFRIQUE" -> {
                territoires = new ArrayList<>();
                NomTerritoireAF[] nomTerritoireAF = NomTerritoireAF.values();
                for (int i = 0; i < 6; i++) {
                    Territoire t = new Territoire(nomTerritoireAF[i].toString());
                    this.territoires.add(t);
                }
            }
            case "ASIE" -> {
                territoires = new ArrayList<>();
                NomTerritoireASIE[] nomTerritoireASIE = NomTerritoireASIE.values();
                for (int i = 0; i < 12; i++) {
                    Territoire t = new Territoire(nomTerritoireASIE[i].toString());
                    this.territoires.add(t);
                }
            }
            case "EUROPE" -> {
                territoires = new ArrayList<>();
                NomTerritoireEU[] nomTerritoireEU = NomTerritoireEU.values();
                for (int i = 0; i < 7; i++) {
                    Territoire t = new Territoire(nomTerritoireEU[i].toString());
                    this.territoires.add(t);
                }
            }
            case "OCEANIE" -> {
                territoires = new ArrayList<>();
                NomTerritoireOC[] nomTerritoireOC = NomTerritoireOC.values();
                for (int i = 0; i < 4; i++) {
                    Territoire t = new Territoire(nomTerritoireOC[i].toString());
                    this.territoires.add(t);
                }
            }
        }
    }

    public List<Territoire> getTerritoires() {
        return territoires;
    }

    public void setTerritoires(List<Territoire> territoires) {
        this.territoires = territoires;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public int getRenfortObtenu() {
        return renfortObtenu;
    }

    public void setRenfortObtenu(int renfortObtenu) {
        this.renfortObtenu = renfortObtenu;
    }

    @Override
    public String toString() {
        return "\n\tContinent{" +
                "nom='" + nom + '\'' +
                ", renfortObtenu=" + renfortObtenu +
                ", territoires=" + territoires +
                '}';
    }
}
