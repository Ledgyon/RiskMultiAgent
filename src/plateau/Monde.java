package plateau;

import plateau.enumerations.NomContinents;

import java.util.ArrayList;
import java.util.List;

public class Monde {
    private List<Continent> continents;

    public Monde() {

        continents = new ArrayList<>();
        NomContinents[] nomContinents = NomContinents.values();

        for(int i = 0; i < 6; i++) {
            Continent c = new Continent(nomContinents[i].toString());
            this.continents.add(c);
        }
        assignTerritoireAdjacents();
    }


    public void assignTerritoireAdjacents(){
        ArrayList<Territoire> tempListeTerritoire = new ArrayList<>();
        for(int i=0 ; i<6 ; i++){
            switch(i){
                case 0:                               //Continent AMERIQUE_NORD
                    for(int j=0 ; j<9 ;j++){
                        switch (j) {
                            case 0 -> {                   //TERRITOIRE ALASKA
                                tempListeTerritoire.clear();
                                tempListeTerritoire.add(continents.get(0).getTerritoires().get(5));
                                tempListeTerritoire.add(continents.get(0).getTerritoires().get(1));
                                tempListeTerritoire.add(continents.get(4).getTerritoires().get(5));
                                continents.get(i).getTerritoires().get(j).setTerritoires_adjacents(tempListeTerritoire);
                            }
                            case 1 -> {                    //ALBERTA
                                tempListeTerritoire = new ArrayList<>();
                                tempListeTerritoire.add(continents.get(0).getTerritoires().get(0));
                                tempListeTerritoire.add(continents.get(0).getTerritoires().get(5));
                                tempListeTerritoire.add(continents.get(0).getTerritoires().get(6));
                                tempListeTerritoire.add(continents.get(0).getTerritoires().get(8));
                                continents.get(i).getTerritoires().get(j).setTerritoires_adjacents(tempListeTerritoire);
                            }
                        }
                    }
                    break;
                case 1:                               //Continent AMERIQUE_SUD
                    for(int j=0 ; j<4 ;j++){
                        switch (j) {
                            case 0 -> {                   //TERRITOIRE ARGENTINE
                                tempListeTerritoire.clear();
                                tempListeTerritoire.add(continents.get(1).getTerritoires().get(1));
                                tempListeTerritoire.add(continents.get(1).getTerritoires().get(2));
                                continents.get(i).getTerritoires().get(j).setTerritoires_adjacents(tempListeTerritoire);
                            }
                            case 1 -> {                   //TERRITOIRE BRESIL
                                tempListeTerritoire.clear();
                                tempListeTerritoire.add(continents.get(1).getTerritoires().get(0));
                                tempListeTerritoire.add(continents.get(1).getTerritoires().get(2));
                                tempListeTerritoire.add(continents.get(1).getTerritoires().get(3));
                                tempListeTerritoire.add(continents.get(3).getTerritoires().get(4));
                                continents.get(i).getTerritoires().get(j).setTerritoires_adjacents(tempListeTerritoire);
                            }
                            case 2 -> {                   //TERRITOIRE BRESIL
                                tempListeTerritoire.clear();
                                tempListeTerritoire.add(continents.get(1).getTerritoires().get(0));
                                tempListeTerritoire.add(continents.get(1).getTerritoires().get(1));
                                tempListeTerritoire.add(continents.get(1).getTerritoires().get(3));
                                continents.get(i).getTerritoires().get(j).setTerritoires_adjacents(tempListeTerritoire);
                            }
                            case 3 -> {                   //TERRITOIRE BRESIL
                                tempListeTerritoire.clear();
                                tempListeTerritoire.add(continents.get(1).getTerritoires().get(1));
                                tempListeTerritoire.add(continents.get(1).getTerritoires().get(2));
                                tempListeTerritoire.add(continents.get(0).getTerritoires().get(2));
                                continents.get(i).getTerritoires().get(j).setTerritoires_adjacents(tempListeTerritoire);
                            }
                        }
                    }
                    break;
                case 2:                               //Continent EUROPE
                    for(int j=0 ; j<7 ;j++) {
                        switch (j) {
                            case 0 -> {
                                tempListeTerritoire.clear();
                                tempListeTerritoire.add(continents.get(2).getTerritoires().get(1));
                                tempListeTerritoire.add(continents.get(2).getTerritoires().get(2));
                                tempListeTerritoire.add(continents.get(2).getTerritoires().get(3));
                                tempListeTerritoire.add(continents.get(2).getTerritoires().get(6));
                                continents.get(i).getTerritoires().get(j).setTerritoires_adjacents(tempListeTerritoire);
                            }
                            case 1 -> {
                                tempListeTerritoire.clear();
                                tempListeTerritoire.add(continents.get(2).getTerritoires().get(0));
                                tempListeTerritoire.add(continents.get(2).getTerritoires().get(3));
                                tempListeTerritoire.add(continents.get(0).getTerritoires().get(4));
                                continents.get(i).getTerritoires().get(j).setTerritoires_adjacents(tempListeTerritoire);
                            }
                            case 2 -> {
                                tempListeTerritoire.clear();
                                tempListeTerritoire.add(continents.get(2).getTerritoires().get(0));
                                tempListeTerritoire.add(continents.get(2).getTerritoires().get(4));
                                tempListeTerritoire.add(continents.get(2).getTerritoires().get(5));
                                tempListeTerritoire.add(continents.get(2).getTerritoires().get(6));
                                continents.get(i).getTerritoires().get(j).setTerritoires_adjacents(tempListeTerritoire);
                            }
                            case 3 -> {
                                tempListeTerritoire.clear();
                                tempListeTerritoire.add(continents.get(2).getTerritoires().get(0));
                                tempListeTerritoire.add(continents.get(2).getTerritoires().get(1));
                                tempListeTerritoire.add(continents.get(2).getTerritoires().get(5));
                                continents.get(i).getTerritoires().get(j).setTerritoires_adjacents(tempListeTerritoire);
                            }
                            case 4 -> {
                                tempListeTerritoire.clear();
                                tempListeTerritoire.add(continents.get(2).getTerritoires().get(2));
                                tempListeTerritoire.add(continents.get(2).getTerritoires().get(5));
                                tempListeTerritoire.add(continents.get(2).getTerritoires().get(6));
                                tempListeTerritoire.add(continents.get(3).getTerritoires().get(2));
                                continents.get(i).getTerritoires().get(j).setTerritoires_adjacents(tempListeTerritoire);
                            }
                            case 5 -> {
                                tempListeTerritoire.clear();
                                tempListeTerritoire.add(continents.get(2).getTerritoires().get(2));
                                tempListeTerritoire.add(continents.get(2).getTerritoires().get(3));
                                tempListeTerritoire.add(continents.get(2).getTerritoires().get(4));
                                tempListeTerritoire.add(continents.get(4).getTerritoires().get(0));
                                tempListeTerritoire.add(continents.get(4).getTerritoires().get(6));
                                tempListeTerritoire.add(continents.get(4).getTerritoires().get(10));
                                continents.get(i).getTerritoires().get(j).setTerritoires_adjacents(tempListeTerritoire);
                            }
                            case 6 -> {
                                tempListeTerritoire.clear();
                                tempListeTerritoire.add(continents.get(2).getTerritoires().get(0));
                                tempListeTerritoire.add(continents.get(2).getTerritoires().get(2));
                                tempListeTerritoire.add(continents.get(2).getTerritoires().get(4));
                                tempListeTerritoire.add(continents.get(3).getTerritoires().get(4));
                                continents.get(i).getTerritoires().get(j).setTerritoires_adjacents(tempListeTerritoire);
                            }
                        }
                    }
                    break;
                case 3:
                    for(int j=0 ; j<6 ;j++) {
                        switch (j) {
                            case 0 -> {
                                tempListeTerritoire.clear();
                                tempListeTerritoire.add(continents.get(3).getTerritoires().get(1));
                                tempListeTerritoire.add(continents.get(3).getTerritoires().get(4));
                                tempListeTerritoire.add(continents.get(3).getTerritoires().get(5));
                                continents.get(i).getTerritoires().get(j).setTerritoires_adjacents(tempListeTerritoire);
                            }
                            case 1 -> {
                                tempListeTerritoire.clear();
                                tempListeTerritoire.add(continents.get(3).getTerritoires().get(0));
                                tempListeTerritoire.add(continents.get(3).getTerritoires().get(2));
                                tempListeTerritoire.add(continents.get(3).getTerritoires().get(3));
                                tempListeTerritoire.add(continents.get(3).getTerritoires().get(4));
                                tempListeTerritoire.add(continents.get(3).getTerritoires().get(5));
                                tempListeTerritoire.add(continents.get(4).getTerritoires().get(6));
                                continents.get(i).getTerritoires().get(j).setTerritoires_adjacents(tempListeTerritoire);
                            }
                            case 2 -> {
                                tempListeTerritoire.clear();
                                tempListeTerritoire.add(continents.get(3).getTerritoires().get(1));
                                tempListeTerritoire.add(continents.get(3).getTerritoires().get(4));
                                tempListeTerritoire.add(continents.get(2).getTerritoires().get(4));
                                tempListeTerritoire.add(continents.get(4).getTerritoires().get(6));
                                continents.get(i).getTerritoires().get(j).setTerritoires_adjacents(tempListeTerritoire);
                            }
                            case 3 -> {
                                tempListeTerritoire.clear();
                                tempListeTerritoire.add(continents.get(3).getTerritoires().get(1));
                                tempListeTerritoire.add(continents.get(3).getTerritoires().get(5));
                                continents.get(i).getTerritoires().get(j).setTerritoires_adjacents(tempListeTerritoire);
                            }
                            case 4 -> {
                                tempListeTerritoire.clear();
                                tempListeTerritoire.add(continents.get(3).getTerritoires().get(0));
                                tempListeTerritoire.add(continents.get(3).getTerritoires().get(1));
                                tempListeTerritoire.add(continents.get(3).getTerritoires().get(2));
                                tempListeTerritoire.add(continents.get(1).getTerritoires().get(1));
                                tempListeTerritoire.add(continents.get(2).getTerritoires().get(6));
                                continents.get(i).getTerritoires().get(j).setTerritoires_adjacents(tempListeTerritoire);
                            }
                            case 5 -> {
                                tempListeTerritoire.clear();
                                tempListeTerritoire.add(continents.get(3).getTerritoires().get(0));
                                tempListeTerritoire.add(continents.get(3).getTerritoires().get(1));
                                tempListeTerritoire.add(continents.get(3).getTerritoires().get(3));
                                continents.get(i).getTerritoires().get(j).setTerritoires_adjacents(tempListeTerritoire);
                            }
                        }
                    }
                    break;
                case 4:
                    for(int j=0 ; j<12 ; j++){
                        switch (j) {
                            case 0 -> {
                                tempListeTerritoire.clear();
                                tempListeTerritoire.add(continents.get(4).getTerritoires().get(1));
                                tempListeTerritoire.add(continents.get(4).getTerritoires().get(2));
                                tempListeTerritoire.add(continents.get(4).getTerritoires().get(6));
                                tempListeTerritoire.add(continents.get(4).getTerritoires().get(10));
                                tempListeTerritoire.add(continents.get(2).getTerritoires().get(5));
                                continents.get(i).getTerritoires().get(j).setTerritoires_adjacents(tempListeTerritoire);
                            }
                            case 1 -> {
                                tempListeTerritoire.clear();
                                tempListeTerritoire.add(continents.get(4).getTerritoires().get(0));
                                tempListeTerritoire.add(continents.get(4).getTerritoires().get(2));
                                tempListeTerritoire.add(continents.get(4).getTerritoires().get(7));
                                tempListeTerritoire.add(continents.get(4).getTerritoires().get(8));
                                tempListeTerritoire.add(continents.get(4).getTerritoires().get(9));
                                tempListeTerritoire.add(continents.get(4).getTerritoires().get(10));
                                continents.get(i).getTerritoires().get(j).setTerritoires_adjacents(tempListeTerritoire);
                            }
                            case 2 -> {
                                tempListeTerritoire.clear();
                                tempListeTerritoire.add(continents.get(4).getTerritoires().get(0));
                                tempListeTerritoire.add(continents.get(4).getTerritoires().get(1));
                                tempListeTerritoire.add(continents.get(4).getTerritoires().get(6));
                                tempListeTerritoire.add(continents.get(4).getTerritoires().get(8));
                                continents.get(i).getTerritoires().get(j).setTerritoires_adjacents(tempListeTerritoire);
                            }
                            case 3 -> {
                                tempListeTerritoire.clear();
                                tempListeTerritoire.add(continents.get(4).getTerritoires().get(5));
                                tempListeTerritoire.add(continents.get(4).getTerritoires().get(7));
                                tempListeTerritoire.add(continents.get(4).getTerritoires().get(9));
                                tempListeTerritoire.add(continents.get(4).getTerritoires().get(11));
                                continents.get(i).getTerritoires().get(j).setTerritoires_adjacents(tempListeTerritoire);
                            }
                            case 4 -> {
                                tempListeTerritoire.clear();
                                tempListeTerritoire.add(continents.get(4).getTerritoires().get(5));
                                tempListeTerritoire.add(continents.get(4).getTerritoires().get(7));
                                continents.get(i).getTerritoires().get(j).setTerritoires_adjacents(tempListeTerritoire);
                            }
                            case 5 -> {
                                tempListeTerritoire.clear();
                                tempListeTerritoire.add(continents.get(4).getTerritoires().get(3));
                                tempListeTerritoire.add(continents.get(4).getTerritoires().get(4));
                                tempListeTerritoire.add(continents.get(4).getTerritoires().get(7));
                                tempListeTerritoire.add(continents.get(4).getTerritoires().get(11));
                                tempListeTerritoire.add(continents.get(0).getTerritoires().get(0));
                                continents.get(i).getTerritoires().get(j).setTerritoires_adjacents(tempListeTerritoire);
                            }
                            case 6 -> {
                                tempListeTerritoire.clear();
                                tempListeTerritoire.add(continents.get(4).getTerritoires().get(0));
                                tempListeTerritoire.add(continents.get(4).getTerritoires().get(2));
                                tempListeTerritoire.add(continents.get(2).getTerritoires().get(5));
                                tempListeTerritoire.add(continents.get(3).getTerritoires().get(1));
                                tempListeTerritoire.add(continents.get(3).getTerritoires().get(2));
                                continents.get(i).getTerritoires().get(j).setTerritoires_adjacents(tempListeTerritoire);
                            }
                            case 7 -> {
                                tempListeTerritoire.clear();
                                tempListeTerritoire.add(continents.get(4).getTerritoires().get(1));
                                tempListeTerritoire.add(continents.get(4).getTerritoires().get(3));
                                tempListeTerritoire.add(continents.get(4).getTerritoires().get(4));
                                tempListeTerritoire.add(continents.get(4).getTerritoires().get(5));
                                continents.get(i).getTerritoires().get(j).setTerritoires_adjacents(tempListeTerritoire);
                            }
                            case 8 -> {
                                tempListeTerritoire.clear();
                                tempListeTerritoire.add(continents.get(4).getTerritoires().get(1));
                                tempListeTerritoire.add(continents.get(4).getTerritoires().get(2));
                                tempListeTerritoire.add(continents.get(4).getTerritoires().get(9));
                                tempListeTerritoire.add(continents.get(5).getTerritoires().get(1));
                                continents.get(i).getTerritoires().get(j).setTerritoires_adjacents(tempListeTerritoire);
                            }
                            case 9 -> {
                                tempListeTerritoire.clear();
                                tempListeTerritoire.add(continents.get(4).getTerritoires().get(1));
                                tempListeTerritoire.add(continents.get(4).getTerritoires().get(3));
                                tempListeTerritoire.add(continents.get(4).getTerritoires().get(10));
                                tempListeTerritoire.add(continents.get(4).getTerritoires().get(11));
                                continents.get(i).getTerritoires().get(j).setTerritoires_adjacents(tempListeTerritoire);
                            }
                            case 10 -> {
                                tempListeTerritoire.clear();
                                tempListeTerritoire.add(continents.get(4).getTerritoires().get(0));
                                tempListeTerritoire.add(continents.get(4).getTerritoires().get(1));
                                tempListeTerritoire.add(continents.get(4).getTerritoires().get(7));
                                tempListeTerritoire.add(continents.get(4).getTerritoires().get(9));
                                tempListeTerritoire.add(continents.get(2).getTerritoires().get(5));
                                continents.get(i).getTerritoires().get(j).setTerritoires_adjacents(tempListeTerritoire);
                            }
                            case 11 -> {
                                tempListeTerritoire.clear();
                                tempListeTerritoire.add(continents.get(4).getTerritoires().get(3));
                                tempListeTerritoire.add(continents.get(4).getTerritoires().get(5));
                                tempListeTerritoire.add(continents.get(4).getTerritoires().get(9));
                                continents.get(i).getTerritoires().get(j).setTerritoires_adjacents(tempListeTerritoire);
                            }
                        }
                    }
                    break;
                case 5:
                    for(int j=0 ; j<4 ;j++){
                        switch (j) {
                            case 0 -> {                   //TERRITOIRE ARGENTINE
                                tempListeTerritoire.clear();
                                tempListeTerritoire.add(continents.get(5).getTerritoires().get(2));
                                tempListeTerritoire.add(continents.get(5).getTerritoires().get(3));
                                continents.get(i).getTerritoires().get(j).setTerritoires_adjacents(tempListeTerritoire);
                            }
                            case 1 -> {                   //TERRITOIRE BRESIL
                                tempListeTerritoire.clear();
                                tempListeTerritoire.add(continents.get(5).getTerritoires().get(2));
                                tempListeTerritoire.add(continents.get(5).getTerritoires().get(3));
                                tempListeTerritoire.add(continents.get(4).getTerritoires().get(8));
                                continents.get(i).getTerritoires().get(j).setTerritoires_adjacents(tempListeTerritoire);
                            }
                            case 2 -> {                   //TERRITOIRE BRESIL
                                tempListeTerritoire.clear();
                                tempListeTerritoire.add(continents.get(5).getTerritoires().get(0));
                                tempListeTerritoire.add(continents.get(5).getTerritoires().get(1));
                                tempListeTerritoire.add(continents.get(5).getTerritoires().get(3));
                                continents.get(i).getTerritoires().get(j).setTerritoires_adjacents(tempListeTerritoire);
                            }
                            case 3 -> {                   //TERRITOIRE BRESIL
                                tempListeTerritoire.clear();
                                tempListeTerritoire.add(continents.get(5).getTerritoires().get(0));
                                tempListeTerritoire.add(continents.get(5).getTerritoires().get(1));
                                tempListeTerritoire.add(continents.get(5).getTerritoires().get(2));
                                continents.get(i).getTerritoires().get(j).setTerritoires_adjacents(tempListeTerritoire);
                            }
                        }
                    }
                    break;
            }
        }
    }

    public String toString() {
        return "Monde{continents=" + this.continents + "}";
    }
}
