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
        ArrayList<Territoire> tempListeTerritoire;
        for(int i=0 ; i<6 ; i++){
            switch(i){
                case 0:                               //Continent AMERIQUE_NORD
                    for(int j=0 ; j<9 ;j++){
                        switch(j){
                            case 0:                   //TERRITOIRE ALASKA
                                tempListeTerritoire = new ArrayList<>();
                                tempListeTerritoire.add(continents.get(0).getTerritoires().get(5));
                                tempListeTerritoire.add(continents.get(0).getTerritoires().get(1));
                                tempListeTerritoire.add(continents.get(4).getTerritoires().get(5));
                                continents.get(i).getTerritoires().get(j).setTerritoires_adjacents(tempListeTerritoire);
                                break;
                            case 1:			//ALBERTA
                            	tempListeTerritoire = new ArrayList<>();
                                tempListeTerritoire.add(continents.get(0).getTerritoires().get(0));
                                tempListeTerritoire.add(continents.get(0).getTerritoires().get(5));
                                tempListeTerritoire.add(continents.get(4).getTerritoires().get(6));
                                tempListeTerritoire.add(continents.get(4).getTerritoires().get(8));
                                continents.get(i).getTerritoires().get(j).setTerritoires_adjacents(tempListeTerritoire);
                                break;
                        }
                    }
                    break;
                case 1:                               //Continent AMERIQUE_SUD
                    for(int j=0 ; j<4 ;j++){
                        switch(j){
                            case 0:                   //TERRITOIRE ARGENTINE
                                tempListeTerritoire = new ArrayList<>();
                                tempListeTerritoire.add(continents.get(1).getTerritoires().get(1));
                                tempListeTerritoire.add(continents.get(1).getTerritoires().get(2));
                                continents.get(i).getTerritoires().get(j).setTerritoires_adjacents(tempListeTerritoire);
                                break;
                            case 1:                   //TERRITOIRE BRESIL
                                tempListeTerritoire = new ArrayList<>();
                                tempListeTerritoire.add(continents.get(1).getTerritoires().get(1));
                                tempListeTerritoire.add(continents.get(1).getTerritoires().get(2));
                                continents.get(i).getTerritoires().get(j).setTerritoires_adjacents(tempListeTerritoire);
                                break;
                        }
                    }
            }
        }
    }

    public String toString() {
        return "Monde{continents=" + this.continents + "}";
    }
}
