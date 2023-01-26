package agents;

import plateau.enumerations.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import carte.CarteMission;
import carte.CartePioche;
import carte.enumerations.Unite;

public class General {
	private List<CartePioche> pioche;
	private List<CarteMission> objectifs;
	
	
	public General() {
		// init de la pioche
		this.pioche = new ArrayList<>();
		constructPioche();
		this.objectifs = new ArrayList<>();
		constructObjectifs();
	}
	
	//fonction de construction de la pioche
	public void constructPioche()
	{
		//ASIE
		CartePioche carteP = new CartePioche(NomTerritoireASIE.AFGHANISTAN.toString(),Unite.FANTASSIN.toString());
		this.pioche.add(carteP);
		carteP = new CartePioche(NomTerritoireASIE.INDE.toString(),Unite.FANTASSIN.toString());
		this.pioche.add(carteP);
		carteP = new CartePioche(NomTerritoireASIE.CHINE.toString(),Unite.CAVALIER.toString());
		this.pioche.add(carteP);
		carteP = new CartePioche(NomTerritoireASIE.JAPON.toString(),Unite.FANTASSIN.toString());
		this.pioche.add(carteP);
		carteP = new CartePioche(NomTerritoireASIE.KAMTCHATKA.toString(),Unite.CAVALIER.toString());
		this.pioche.add(carteP);
		carteP = new CartePioche(NomTerritoireASIE.MONGOLIE.toString(),Unite.CANON.toString());
		this.pioche.add(carteP);
		carteP = new CartePioche(NomTerritoireASIE.MOYEN_ORIENT.toString(),Unite.CANON.toString());
		this.pioche.add(carteP);
		carteP = new CartePioche(NomTerritoireASIE.OURAL.toString(),Unite.CAVALIER.toString());
		this.pioche.add(carteP);
		carteP = new CartePioche(NomTerritoireASIE.SIAM.toString(),Unite.CANON.toString());
		this.pioche.add(carteP);
		carteP = new CartePioche(NomTerritoireASIE.SIBERIE.toString(),Unite.CANON.toString());
		this.pioche.add(carteP);
		carteP = new CartePioche(NomTerritoireASIE.TCHITA.toString(),Unite.FANTASSIN.toString());
		this.pioche.add(carteP);
		carteP = new CartePioche(NomTerritoireASIE.YAKOUTIE.toString(),Unite.CAVALIER.toString());
		this.pioche.add(carteP);
		
		//EUROPE
		carteP = new CartePioche(NomTerritoireEU.EUROPE_NORD.toString(),Unite.CAVALIER.toString());
		this.pioche.add(carteP);
		carteP = new CartePioche(NomTerritoireEU.EUROPE_OCCIDENTALE.toString(),Unite.FANTASSIN.toString());
		this.pioche.add(carteP);
		carteP = new CartePioche(NomTerritoireEU.EUROPE_SUD.toString(),Unite.CAVALIER.toString());
		this.pioche.add(carteP);
		carteP = new CartePioche(NomTerritoireEU.GRANDE_BRETAGNE.toString(),Unite.CAVALIER.toString());
		this.pioche.add(carteP);
		carteP = new CartePioche(NomTerritoireEU.ISLANDE.toString(),Unite.FANTASSIN.toString());
		this.pioche.add(carteP);
		carteP = new CartePioche(NomTerritoireEU.SCANDINAVIE.toString(),Unite.CANON.toString());
		this.pioche.add(carteP);
		carteP = new CartePioche(NomTerritoireEU.UKRAINE.toString(),Unite.CANON.toString());
		this.pioche.add(carteP);

		//AFRIQUE
		carteP = new CartePioche(NomTerritoireAF.AFRIQUE_EST.toString(),Unite.CANON.toString());
		this.pioche.add(carteP);
		carteP = new CartePioche(NomTerritoireAF.AFRIQUE_NORD.toString(),Unite.FANTASSIN.toString());
		this.pioche.add(carteP);
		carteP = new CartePioche(NomTerritoireAF.AFRIQUE_SUD.toString(),Unite.CANON.toString());
		this.pioche.add(carteP);
		carteP = new CartePioche(NomTerritoireAF.CONGO.toString(),Unite.CAVALIER.toString());
		this.pioche.add(carteP);
		carteP = new CartePioche(NomTerritoireAF.EGYPTE.toString(),Unite.FANTASSIN.toString());
		this.pioche.add(carteP);
		carteP = new CartePioche(NomTerritoireAF.MADAGASCAR.toString(),Unite.FANTASSIN.toString());
		this.pioche.add(carteP);
		
		//AMERIQUE DU NORD 
		carteP = new CartePioche(NomTerritoireAN.ALASKA.toString(),Unite.FANTASSIN.toString());
		this.pioche.add(carteP);
		carteP = new CartePioche(NomTerritoireAN.ALBERTA.toString(),Unite.FANTASSIN.toString());
		this.pioche.add(carteP);
		carteP = new CartePioche(NomTerritoireAN.AMERIQUE_CENTRALE.toString(),Unite.CAVALIER.toString());
		this.pioche.add(carteP);
		carteP = new CartePioche(NomTerritoireAN.ETATS_EST.toString(),Unite.CANON.toString());
		this.pioche.add(carteP);
		carteP = new CartePioche(NomTerritoireAN.ETATS_OUEST.toString(),Unite.FANTASSIN.toString());
		this.pioche.add(carteP);
		carteP = new CartePioche(NomTerritoireAN.GROENLAND.toString(),Unite.CAVALIER.toString());
		this.pioche.add(carteP);
		carteP = new CartePioche(NomTerritoireAN.ONTARIO.toString(),Unite.CAVALIER.toString());
		this.pioche.add(carteP);
		carteP = new CartePioche(NomTerritoireAN.QUEBEC.toString(),Unite.CANON.toString());
		this.pioche.add(carteP);
		carteP = new CartePioche(NomTerritoireAN.TERRITOIRE_NORDOUEST.toString(),Unite.CANON.toString());
		this.pioche.add(carteP);
		
		//AMERIQUE DU SUD
		carteP = new CartePioche(NomTerritoireAS.ARGENTINE.toString(),Unite.FANTASSIN.toString());
		this.pioche.add(carteP);
		carteP = new CartePioche(NomTerritoireAS.BRESIL.toString(),Unite.CANON.toString());
		this.pioche.add(carteP);
		carteP = new CartePioche(NomTerritoireAS.PEROU.toString(),Unite.CAVALIER.toString());
		this.pioche.add(carteP);
		carteP = new CartePioche(NomTerritoireAS.VENEZUELA.toString(),Unite.CANON.toString());
		this.pioche.add(carteP);
		
		//OCEANIE
		carteP = new CartePioche(NomTerritoireOC.AUSTRALIE_OCCIDENTALE.toString(),Unite.CANON.toString());
		this.pioche.add(carteP);
		carteP = new CartePioche(NomTerritoireOC.AUSTRALIE_ORIENTALE.toString(),Unite.FANTASSIN.toString());
		this.pioche.add(carteP);
		carteP = new CartePioche(NomTerritoireOC.INDONESIE.toString(),Unite.CAVALIER.toString());
		this.pioche.add(carteP);
		carteP = new CartePioche(NomTerritoireOC.NOUVELLE_GUINEE.toString(),Unite.CAVALIER.toString());
		this.pioche.add(carteP);
		
		//JOKER
		carteP = new CartePioche("JOKER",Unite.FANTASSIN_CAVALERIE_CANON.toString());
		this.pioche.add(carteP);
		carteP = new CartePioche("JOKER",Unite.FANTASSIN_CAVALERIE_CANON.toString());
		this.pioche.add(carteP);
		
		Collections.shuffle(this.pioche);
		
	}

	public void constructObjectifs(){
		CarteMission carteM = new CarteMission(NomContinents.AMERIQUE_NORD.toString(),NomContinents.AFRIQUE.toString());
		objectifs.add(carteM);
		carteM = new CarteMission(NomContinents.AMERIQUE_NORD.toString(),NomContinents.OCEANIE.toString());
		objectifs.add(carteM);
		carteM = new CarteMission(NomContinents.ASIE.toString(),NomContinents.AFRIQUE.toString());
		objectifs.add(carteM);
		carteM = new CarteMission(NomContinents.ASIE.toString(),NomContinents.AMERIQUE_SUD.toString());
		objectifs.add(carteM);
		carteM = new CarteMission(NomContinents.EUROPE.toString(),NomContinents.AMERIQUE_SUD.toString(),"Autre");
		objectifs.add(carteM);
		carteM = new CarteMission(NomContinents.EUROPE.toString(),NomContinents.OCEANIE.toString(),"Autre");
		objectifs.add(carteM);
		carteM = new CarteMission(24);
		objectifs.add(carteM);
		carteM = new CarteMission(18,2);
		objectifs.add(carteM);
		carteM = new CarteMission("jaune");
		objectifs.add(carteM);
		carteM = new CarteMission("rouge");
		objectifs.add(carteM);
		carteM = new CarteMission("bleu");
		objectifs.add(carteM);
		carteM = new CarteMission("noir");
		objectifs.add(carteM);
		carteM = new CarteMission("violet");
		objectifs.add(carteM);
		carteM = new CarteMission("vert");
		objectifs.add(carteM);
	}

	public List<CartePioche> getPioche() {
		return pioche;
	}

	public void setPioche(List<CartePioche> pioche) {
		this.pioche = pioche;
	}

	public List<CarteMission> getObjectifs() {
		return objectifs;
	}

	public void setObjectifs(List<CarteMission> objectifs) {
		this.objectifs = objectifs;
	}

	@Override
	public String toString() {
		return "General [pioche=" + pioche + ", objectifs=" + objectifs + "]";
	}

		
}
