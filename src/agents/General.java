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
		ConstructPioche();
		this.objectifs = new ArrayList<>();
	}
	
	//fonction de construction de la pioche
	public void ConstructPioche()
	{
		//ASIE
		CartePioche carte = new CartePioche(NomTerritoireASIE.AFGHANISTAN.toString(),Unite.FANTASSIN.toString());
		this.pioche.add(carte);
		carte = new CartePioche(NomTerritoireASIE.INDE.toString(),Unite.FANTASSIN.toString());
		this.pioche.add(carte);
		carte = new CartePioche(NomTerritoireASIE.CHINE.toString(),Unite.CAVALIER.toString());
		this.pioche.add(carte);
		carte = new CartePioche(NomTerritoireASIE.JAPON.toString(),Unite.FANTASSIN.toString());
		this.pioche.add(carte);
		carte = new CartePioche(NomTerritoireASIE.KAMTCHATKA.toString(),Unite.CAVALIER.toString());
		this.pioche.add(carte);
		carte = new CartePioche(NomTerritoireASIE.MONGOLIE.toString(),Unite.CANON.toString());
		this.pioche.add(carte);
		carte = new CartePioche(NomTerritoireASIE.MOYEN_ORIENT.toString(),Unite.CANON.toString());
		this.pioche.add(carte);
		carte = new CartePioche(NomTerritoireASIE.OURAL.toString(),Unite.CAVALIER.toString());
		this.pioche.add(carte);
		carte = new CartePioche(NomTerritoireASIE.SIAM.toString(),Unite.CANON.toString());
		this.pioche.add(carte);
		carte = new CartePioche(NomTerritoireASIE.SIBERIE.toString(),Unite.CANON.toString());
		this.pioche.add(carte);
		carte = new CartePioche(NomTerritoireASIE.TCHITA.toString(),Unite.FANTASSIN.toString());
		this.pioche.add(carte);
		carte = new CartePioche(NomTerritoireASIE.YAKOUTIE.toString(),Unite.CAVALIER.toString());
		this.pioche.add(carte);
		
		//EUROPE
		carte = new CartePioche(NomTerritoireEU.EUROPE_NORD.toString(),Unite.CAVALIER.toString());
		this.pioche.add(carte);
		carte = new CartePioche(NomTerritoireEU.EUROPE_OCCIDENTALE.toString(),Unite.FANTASSIN.toString());
		this.pioche.add(carte);
		carte = new CartePioche(NomTerritoireEU.EUROPE_SUD.toString(),Unite.CAVALIER.toString());
		this.pioche.add(carte);
		carte = new CartePioche(NomTerritoireEU.GRANDE_BRETAGNE.toString(),Unite.CAVALIER.toString());
		this.pioche.add(carte);
		carte = new CartePioche(NomTerritoireEU.ISLANDE.toString(),Unite.FANTASSIN.toString());
		this.pioche.add(carte);
		carte = new CartePioche(NomTerritoireEU.SCANDINAVIE.toString(),Unite.CANON.toString());
		this.pioche.add(carte);
		carte = new CartePioche(NomTerritoireEU.UKRAINE.toString(),Unite.CANON.toString());
		this.pioche.add(carte);

		//AFRIQUE
		carte = new CartePioche(NomTerritoireAF.AFRIQUE_EST.toString(),Unite.CANON.toString());
		this.pioche.add(carte);
		carte = new CartePioche(NomTerritoireAF.AFRIQUE_NORD.toString(),Unite.FANTASSIN.toString());
		this.pioche.add(carte);
		carte = new CartePioche(NomTerritoireAF.AFRIQUE_SUD.toString(),Unite.CANON.toString());
		this.pioche.add(carte);
		carte = new CartePioche(NomTerritoireAF.CONGO.toString(),Unite.CAVALIER.toString());
		this.pioche.add(carte);
		carte = new CartePioche(NomTerritoireAF.EGYPTE.toString(),Unite.FANTASSIN.toString());
		this.pioche.add(carte);
		carte = new CartePioche(NomTerritoireAF.MADAGASCAR.toString(),Unite.FANTASSIN.toString());
		this.pioche.add(carte);
		
		//AMERIQUE DU NORD 
		carte = new CartePioche(NomTerritoireAN.ALASKA.toString(),Unite.FANTASSIN.toString());
		this.pioche.add(carte);
		carte = new CartePioche(NomTerritoireAN.ALBERTA.toString(),Unite.FANTASSIN.toString());
		this.pioche.add(carte);
		carte = new CartePioche(NomTerritoireAN.AMERIQUE_CENTRALE.toString(),Unite.CAVALIER.toString());
		this.pioche.add(carte);
		carte = new CartePioche(NomTerritoireAN.ETATS_EST.toString(),Unite.CANON.toString());
		this.pioche.add(carte);
		carte = new CartePioche(NomTerritoireAN.ETATS_OUEST.toString(),Unite.FANTASSIN.toString());
		this.pioche.add(carte);
		carte = new CartePioche(NomTerritoireAN.GROENLAND.toString(),Unite.CAVALIER.toString());
		this.pioche.add(carte);
		carte = new CartePioche(NomTerritoireAN.ONTARIO.toString(),Unite.CAVALIER.toString());
		this.pioche.add(carte);
		carte = new CartePioche(NomTerritoireAN.QUEBEC.toString(),Unite.CANON.toString());
		this.pioche.add(carte);
		carte = new CartePioche(NomTerritoireAN.TERRITOIRE_NORDOUEST.toString(),Unite.CANON.toString());
		this.pioche.add(carte);
		
		//AMERIQUE DU SUD
		carte = new CartePioche(NomTerritoireAS.ARGENTINE.toString(),Unite.FANTASSIN.toString());
		this.pioche.add(carte);
		carte = new CartePioche(NomTerritoireAS.BRESIL.toString(),Unite.CANON.toString());
		this.pioche.add(carte);
		carte = new CartePioche(NomTerritoireAS.PEROU.toString(),Unite.CAVALIER.toString());
		this.pioche.add(carte);
		carte = new CartePioche(NomTerritoireAS.VENEZUELA.toString(),Unite.CANON.toString());
		this.pioche.add(carte);
		
		//OCEANIE
		carte = new CartePioche(NomTerritoireOC.AUSTRALIE_OCCIDENTALE.toString(),Unite.CANON.toString());
		this.pioche.add(carte);
		carte = new CartePioche(NomTerritoireOC.AUSTRALIE_ORIENTALE.toString(),Unite.FANTASSIN.toString());
		this.pioche.add(carte);
		carte = new CartePioche(NomTerritoireOC.INDONESIE.toString(),Unite.CAVALIER.toString());
		this.pioche.add(carte);
		carte = new CartePioche(NomTerritoireOC.NOUVELLE_GUINEE.toString(),Unite.CAVALIER.toString());
		this.pioche.add(carte);
		
		//JOKER
		carte = new CartePioche("JOKER",Unite.FANTASSIN_CAVALERIE_CANON.toString());
		this.pioche.add(carte);
		carte = new CartePioche("JOKER",Unite.FANTASSIN_CAVALERIE_CANON.toString());
		this.pioche.add(carte);
		
		Collections.shuffle(this.pioche);;
		
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
