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
                            case 1 -> {                    //TERRITOIRE ALBERTA
                                tempListeTerritoire = new ArrayList<>();
                                tempListeTerritoire.add(continents.get(0).getTerritoires().get(0));
                                tempListeTerritoire.add(continents.get(0).getTerritoires().get(5));
                                tempListeTerritoire.add(continents.get(0).getTerritoires().get(6));
                                tempListeTerritoire.add(continents.get(0).getTerritoires().get(8));
                                continents.get(i).getTerritoires().get(j).setTerritoires_adjacents(tempListeTerritoire);
                            }
                            case 2 -> {                    //TERRITOIRE AMERIQUE_CENTRALE
                                tempListeTerritoire = new ArrayList<>();
                                tempListeTerritoire.add(continents.get(1).getTerritoires().get(3));
                                tempListeTerritoire.add(continents.get(0).getTerritoires().get(3));
                                tempListeTerritoire.add(continents.get(0).getTerritoires().get(8));
                                continents.get(i).getTerritoires().get(j).setTerritoires_adjacents(tempListeTerritoire);
                            }
                            case 3 -> {                    //TERRITOIRE ETATS_EST
                                tempListeTerritoire = new ArrayList<>();
                                tempListeTerritoire.add(continents.get(0).getTerritoires().get(2));
                                tempListeTerritoire.add(continents.get(0).getTerritoires().get(6));
                                tempListeTerritoire.add(continents.get(0).getTerritoires().get(7));
                                tempListeTerritoire.add(continents.get(0).getTerritoires().get(8));
                                continents.get(i).getTerritoires().get(j).setTerritoires_adjacents(tempListeTerritoire);
                            }
                            case 4 -> {                    //TERRITOIRE GROENLAND
                                tempListeTerritoire = new ArrayList<>();
                                tempListeTerritoire.add(continents.get(0).getTerritoires().get(5));
                                tempListeTerritoire.add(continents.get(0).getTerritoires().get(6));
                                tempListeTerritoire.add(continents.get(0).getTerritoires().get(7));
                                tempListeTerritoire.add(continents.get(2).getTerritoires().get(1));
                                continents.get(i).getTerritoires().get(j).setTerritoires_adjacents(tempListeTerritoire);
                            }
                            case 5 -> {                    //TERRITOIRE TERRITOIRES_NORD_OUEST
                                tempListeTerritoire = new ArrayList<>();
                                tempListeTerritoire.add(continents.get(0).getTerritoires().get(0));
                                tempListeTerritoire.add(continents.get(0).getTerritoires().get(1));
                                tempListeTerritoire.add(continents.get(0).getTerritoires().get(4));
                                tempListeTerritoire.add(continents.get(0).getTerritoires().get(6));
                                tempListeTerritoire.add(continents.get(0).getTerritoires().get(7));
                                continents.get(i).getTerritoires().get(j).setTerritoires_adjacents(tempListeTerritoire);
                            }
                            case 6 -> {                    //TERRITOIRE ONTARIO
                                tempListeTerritoire = new ArrayList<>();
                                tempListeTerritoire.add(continents.get(0).getTerritoires().get(1));
                                tempListeTerritoire.add(continents.get(0).getTerritoires().get(3));
                                tempListeTerritoire.add(continents.get(0).getTerritoires().get(4));
                                tempListeTerritoire.add(continents.get(0).getTerritoires().get(5));
                                tempListeTerritoire.add(continents.get(0).getTerritoires().get(7));
                                tempListeTerritoire.add(continents.get(0).getTerritoires().get(8));
                                continents.get(i).getTerritoires().get(j).setTerritoires_adjacents(tempListeTerritoire);
                            }
                            case 7 -> {                    //TERRITOIRE QUEBEC
                                tempListeTerritoire = new ArrayList<>();
                                tempListeTerritoire.add(continents.get(0).getTerritoires().get(3));
                                tempListeTerritoire.add(continents.get(0).getTerritoires().get(4));
                                tempListeTerritoire.add(continents.get(0).getTerritoires().get(6));
                                continents.get(i).getTerritoires().get(j).setTerritoires_adjacents(tempListeTerritoire);
                            }
                            case 8 -> {                    //TERRITOIRE ETATS_OUEST
                                tempListeTerritoire = new ArrayList<>();
                                tempListeTerritoire.add(continents.get(0).getTerritoires().get(1));
                                tempListeTerritoire.add(continents.get(0).getTerritoires().get(2));
                                tempListeTerritoire.add(continents.get(0).getTerritoires().get(3));
                                tempListeTerritoire.add(continents.get(0).getTerritoires().get(6));
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
                            case 2 -> {                   //TERRITOIRE PEROU
                                tempListeTerritoire.clear();
                                tempListeTerritoire.add(continents.get(1).getTerritoires().get(0));
                                tempListeTerritoire.add(continents.get(1).getTerritoires().get(1));
                                tempListeTerritoire.add(continents.get(1).getTerritoires().get(3));
                                continents.get(i).getTerritoires().get(j).setTerritoires_adjacents(tempListeTerritoire);
                            }
                            case 3 -> {                   //TERRITOIRE VENEZUELA
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
                            case 0 -> {                   //TERRITOIRE GRANDE_BRETAGNE
                                tempListeTerritoire.clear();
                                tempListeTerritoire.add(continents.get(2).getTerritoires().get(1));
                                tempListeTerritoire.add(continents.get(2).getTerritoires().get(2));
                                tempListeTerritoire.add(continents.get(2).getTerritoires().get(3));
                                tempListeTerritoire.add(continents.get(2).getTerritoires().get(6));
                                continents.get(i).getTerritoires().get(j).setTerritoires_adjacents(tempListeTerritoire);
                            }
                            case 1 -> {                   //TERRITOIRE ISLANDE
                                tempListeTerritoire.clear();
                                tempListeTerritoire.add(continents.get(2).getTerritoires().get(0));
                                tempListeTerritoire.add(continents.get(2).getTerritoires().get(3));
                                tempListeTerritoire.add(continents.get(0).getTerritoires().get(4));
                                continents.get(i).getTerritoires().get(j).setTerritoires_adjacents(tempListeTerritoire);
                            }
                            case 2 -> {                   //TERRITOIRE EUROPE_NORD
                                tempListeTerritoire.clear();
                                tempListeTerritoire.add(continents.get(2).getTerritoires().get(0));
                                tempListeTerritoire.add(continents.get(2).getTerritoires().get(4));
                                tempListeTerritoire.add(continents.get(2).getTerritoires().get(5));
                                tempListeTerritoire.add(continents.get(2).getTerritoires().get(6));
                                continents.get(i).getTerritoires().get(j).setTerritoires_adjacents(tempListeTerritoire);
                            }
                            case 3 -> {                   //TERRITOIRE SCANDINAVIE
                                tempListeTerritoire.clear();
                                tempListeTerritoire.add(continents.get(2).getTerritoires().get(0));
                                tempListeTerritoire.add(continents.get(2).getTerritoires().get(1));
                                tempListeTerritoire.add(continents.get(2).getTerritoires().get(5));
                                continents.get(i).getTerritoires().get(j).setTerritoires_adjacents(tempListeTerritoire);
                            }
                            case 4 -> {                   //TERRITOIRE EUROPE_SUD
                                tempListeTerritoire.clear();
                                tempListeTerritoire.add(continents.get(2).getTerritoires().get(2));
                                tempListeTerritoire.add(continents.get(2).getTerritoires().get(5));
                                tempListeTerritoire.add(continents.get(2).getTerritoires().get(6));
                                tempListeTerritoire.add(continents.get(3).getTerritoires().get(2));
                                continents.get(i).getTerritoires().get(j).setTerritoires_adjacents(tempListeTerritoire);
                            }
                            case 5 -> {                   //TERRITOIRE UKRAINE
                                tempListeTerritoire.clear();
                                tempListeTerritoire.add(continents.get(2).getTerritoires().get(2));
                                tempListeTerritoire.add(continents.get(2).getTerritoires().get(3));
                                tempListeTerritoire.add(continents.get(2).getTerritoires().get(4));
                                tempListeTerritoire.add(continents.get(4).getTerritoires().get(0));
                                tempListeTerritoire.add(continents.get(4).getTerritoires().get(6));
                                tempListeTerritoire.add(continents.get(4).getTerritoires().get(10));
                                continents.get(i).getTerritoires().get(j).setTerritoires_adjacents(tempListeTerritoire);
                            }
                            case 6 -> {                   //TERRITOIRE EUROPE_OCCIDENTALE
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
                case 3:                               //Continent Afrique
                    for(int j=0 ; j<6 ;j++) {
                        switch (j) {
                            case 0 -> {                   //TERRITOIRE CONGO
                                tempListeTerritoire.clear();
                                tempListeTerritoire.add(continents.get(3).getTerritoires().get(1));
                                tempListeTerritoire.add(continents.get(3).getTerritoires().get(4));
                                tempListeTerritoire.add(continents.get(3).getTerritoires().get(5));
                                continents.get(i).getTerritoires().get(j).setTerritoires_adjacents(tempListeTerritoire);
                            }
                            case 1 -> {                   //TERRITOIRE AFRIQUE_EST
                                tempListeTerritoire.clear();
                                tempListeTerritoire.add(continents.get(3).getTerritoires().get(0));
                                tempListeTerritoire.add(continents.get(3).getTerritoires().get(2));
                                tempListeTerritoire.add(continents.get(3).getTerritoires().get(3));
                                tempListeTerritoire.add(continents.get(3).getTerritoires().get(4));
                                tempListeTerritoire.add(continents.get(3).getTerritoires().get(5));
                                tempListeTerritoire.add(continents.get(4).getTerritoires().get(6));
                                continents.get(i).getTerritoires().get(j).setTerritoires_adjacents(tempListeTerritoire);
                            }
                            case 2 -> {                   //TERRITOIRE EGYPTE
                                tempListeTerritoire.clear();
                                tempListeTerritoire.add(continents.get(3).getTerritoires().get(1));
                                tempListeTerritoire.add(continents.get(3).getTerritoires().get(4));
                                tempListeTerritoire.add(continents.get(2).getTerritoires().get(4));
                                tempListeTerritoire.add(continents.get(4).getTerritoires().get(6));
                                continents.get(i).getTerritoires().get(j).setTerritoires_adjacents(tempListeTerritoire);
                            }
                            case 3 -> {                   //TERRITOIRE MADAGASCAR
                                tempListeTerritoire.clear();
                                tempListeTerritoire.add(continents.get(3).getTerritoires().get(1));
                                tempListeTerritoire.add(continents.get(3).getTerritoires().get(5));
                                continents.get(i).getTerritoires().get(j).setTerritoires_adjacents(tempListeTerritoire);
                            }
                            case 4 -> {                   //TERRITOIRE AFRIQUE_NORD
                                tempListeTerritoire.clear();
                                tempListeTerritoire.add(continents.get(3).getTerritoires().get(0));
                                tempListeTerritoire.add(continents.get(3).getTerritoires().get(1));
                                tempListeTerritoire.add(continents.get(3).getTerritoires().get(2));
                                tempListeTerritoire.add(continents.get(1).getTerritoires().get(1));
                                tempListeTerritoire.add(continents.get(2).getTerritoires().get(6));
                                continents.get(i).getTerritoires().get(j).setTerritoires_adjacents(tempListeTerritoire);
                            }
                            case 5 -> {                   //TERRITOIRE AFRIQUE_SUD
                                tempListeTerritoire.clear();
                                tempListeTerritoire.add(continents.get(3).getTerritoires().get(0));
                                tempListeTerritoire.add(continents.get(3).getTerritoires().get(1));
                                tempListeTerritoire.add(continents.get(3).getTerritoires().get(3));
                                continents.get(i).getTerritoires().get(j).setTerritoires_adjacents(tempListeTerritoire);
                            }
                        }
                    }
                    break;
                case 4:                               //Continent Asie
                    for(int j=0 ; j<12 ; j++){
                        switch (j) {
                            case 0 -> {                   //TERRITOIRE AFGHANISTAN
                                tempListeTerritoire.clear();
                                tempListeTerritoire.add(continents.get(4).getTerritoires().get(1));
                                tempListeTerritoire.add(continents.get(4).getTerritoires().get(2));
                                tempListeTerritoire.add(continents.get(4).getTerritoires().get(6));
                                tempListeTerritoire.add(continents.get(4).getTerritoires().get(10));
                                tempListeTerritoire.add(continents.get(2).getTerritoires().get(5));
                                continents.get(i).getTerritoires().get(j).setTerritoires_adjacents(tempListeTerritoire);
                            }
                            case 1 -> {                   //TERRITOIRE CHINE
                                tempListeTerritoire.clear();
                                tempListeTerritoire.add(continents.get(4).getTerritoires().get(0));
                                tempListeTerritoire.add(continents.get(4).getTerritoires().get(2));
                                tempListeTerritoire.add(continents.get(4).getTerritoires().get(7));
                                tempListeTerritoire.add(continents.get(4).getTerritoires().get(8));
                                tempListeTerritoire.add(continents.get(4).getTerritoires().get(9));
                                tempListeTerritoire.add(continents.get(4).getTerritoires().get(10));
                                continents.get(i).getTerritoires().get(j).setTerritoires_adjacents(tempListeTerritoire);
                            }
                            case 2 -> {                   //TERRITOIRE INDE
                                tempListeTerritoire.clear();
                                tempListeTerritoire.add(continents.get(4).getTerritoires().get(0));
                                tempListeTerritoire.add(continents.get(4).getTerritoires().get(1));
                                tempListeTerritoire.add(continents.get(4).getTerritoires().get(6));
                                tempListeTerritoire.add(continents.get(4).getTerritoires().get(8));
                                continents.get(i).getTerritoires().get(j).setTerritoires_adjacents(tempListeTerritoire);
                            }
                            case 3 -> {                   //TERRITOIRE TCHITA
                                tempListeTerritoire.clear();
                                tempListeTerritoire.add(continents.get(4).getTerritoires().get(5));
                                tempListeTerritoire.add(continents.get(4).getTerritoires().get(7));
                                tempListeTerritoire.add(continents.get(4).getTerritoires().get(9));
                                tempListeTerritoire.add(continents.get(4).getTerritoires().get(11));
                                continents.get(i).getTerritoires().get(j).setTerritoires_adjacents(tempListeTerritoire);
                            }
                            case 4 -> {                   //TERRITOIRE JAPON
                                tempListeTerritoire.clear();
                                tempListeTerritoire.add(continents.get(4).getTerritoires().get(5));
                                tempListeTerritoire.add(continents.get(4).getTerritoires().get(7));
                                continents.get(i).getTerritoires().get(j).setTerritoires_adjacents(tempListeTerritoire);
                            }
                            case 5 -> {                   //TERRITOIRE KAMTCHATKA
                                tempListeTerritoire.clear();
                                tempListeTerritoire.add(continents.get(4).getTerritoires().get(3));
                                tempListeTerritoire.add(continents.get(4).getTerritoires().get(4));
                                tempListeTerritoire.add(continents.get(4).getTerritoires().get(7));
                                tempListeTerritoire.add(continents.get(4).getTerritoires().get(11));
                                tempListeTerritoire.add(continents.get(0).getTerritoires().get(0));
                                continents.get(i).getTerritoires().get(j).setTerritoires_adjacents(tempListeTerritoire);
                            }
                            case 6 -> {                   //TERRITOIRE MOYEN_ORIENT
                                tempListeTerritoire.clear();
                                tempListeTerritoire.add(continents.get(4).getTerritoires().get(0));
                                tempListeTerritoire.add(continents.get(4).getTerritoires().get(2));
                                tempListeTerritoire.add(continents.get(2).getTerritoires().get(5));
                                tempListeTerritoire.add(continents.get(3).getTerritoires().get(1));
                                tempListeTerritoire.add(continents.get(3).getTerritoires().get(2));
                                continents.get(i).getTerritoires().get(j).setTerritoires_adjacents(tempListeTerritoire);
                            }
                            case 7 -> {                   //TERRITOIRE MONGOLIE
                                tempListeTerritoire.clear();
                                tempListeTerritoire.add(continents.get(4).getTerritoires().get(1));
                                tempListeTerritoire.add(continents.get(4).getTerritoires().get(3));
                                tempListeTerritoire.add(continents.get(4).getTerritoires().get(4));
                                tempListeTerritoire.add(continents.get(4).getTerritoires().get(5));
                                continents.get(i).getTerritoires().get(j).setTerritoires_adjacents(tempListeTerritoire);
                            }
                            case 8 -> {                   //TERRITOIRE SIAM
                                tempListeTerritoire.clear();
                                tempListeTerritoire.add(continents.get(4).getTerritoires().get(1));
                                tempListeTerritoire.add(continents.get(4).getTerritoires().get(2));
                                tempListeTerritoire.add(continents.get(4).getTerritoires().get(9));
                                tempListeTerritoire.add(continents.get(5).getTerritoires().get(1));
                                continents.get(i).getTerritoires().get(j).setTerritoires_adjacents(tempListeTerritoire);
                            }
                            case 9 -> {                   //TERRITOIRE SIBERIE
                                tempListeTerritoire.clear();
                                tempListeTerritoire.add(continents.get(4).getTerritoires().get(1));
                                tempListeTerritoire.add(continents.get(4).getTerritoires().get(3));
                                tempListeTerritoire.add(continents.get(4).getTerritoires().get(10));
                                tempListeTerritoire.add(continents.get(4).getTerritoires().get(11));
                                continents.get(i).getTerritoires().get(j).setTerritoires_adjacents(tempListeTerritoire);
                            }
                            case 10 -> {                   //TERRITOIRE OURAL
                                tempListeTerritoire.clear();
                                tempListeTerritoire.add(continents.get(4).getTerritoires().get(0));
                                tempListeTerritoire.add(continents.get(4).getTerritoires().get(1));
                                tempListeTerritoire.add(continents.get(4).getTerritoires().get(7));
                                tempListeTerritoire.add(continents.get(4).getTerritoires().get(9));
                                tempListeTerritoire.add(continents.get(2).getTerritoires().get(5));
                                continents.get(i).getTerritoires().get(j).setTerritoires_adjacents(tempListeTerritoire);
                            }
                            case 11 -> {                   //TERRITOIRE YAKOUTIE
                                tempListeTerritoire.clear();
                                tempListeTerritoire.add(continents.get(4).getTerritoires().get(3));
                                tempListeTerritoire.add(continents.get(4).getTerritoires().get(5));
                                tempListeTerritoire.add(continents.get(4).getTerritoires().get(9));
                                continents.get(i).getTerritoires().get(j).setTerritoires_adjacents(tempListeTerritoire);
                            }
                        }
                    }
                    break;
                case 5:                               //Continent Oceanie
                    for(int j=0 ; j<4 ;j++){
                        switch (j) {
                            case 0 -> {                   //TERRITOIRE AUSTRALIE_ORIENTALE
                                tempListeTerritoire.clear();
                                tempListeTerritoire.add(continents.get(5).getTerritoires().get(2));
                                tempListeTerritoire.add(continents.get(5).getTerritoires().get(3));
                                continents.get(i).getTerritoires().get(j).setTerritoires_adjacents(tempListeTerritoire);
                            }
                            case 1 -> {                   //TERRITOIRE INDONESIE
                                tempListeTerritoire.clear();
                                tempListeTerritoire.add(continents.get(5).getTerritoires().get(2));
                                tempListeTerritoire.add(continents.get(5).getTerritoires().get(3));
                                tempListeTerritoire.add(continents.get(4).getTerritoires().get(8));
                                continents.get(i).getTerritoires().get(j).setTerritoires_adjacents(tempListeTerritoire);
                            }
                            case 2 -> {                   //TERRITOIRE NOUVELLE_GUINEE
                                tempListeTerritoire.clear();
                                tempListeTerritoire.add(continents.get(5).getTerritoires().get(0));
                                tempListeTerritoire.add(continents.get(5).getTerritoires().get(1));
                                tempListeTerritoire.add(continents.get(5).getTerritoires().get(3));
                                continents.get(i).getTerritoires().get(j).setTerritoires_adjacents(tempListeTerritoire);
                            }
                            case 3 -> {                   //TERRITOIRE AUSTRALIE_OCCIDENTALE
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
        return "Monde\n\tContinents = " + this.continents + "}";
    }
}
