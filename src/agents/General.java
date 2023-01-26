package agents;

import plateau.enumerations.*;
import java.util.ArrayList;
import java.util.List;

import carte.CarteMission;
import carte.CartePioche;
import carte.enumerations.Unite;

public class General {
	private List<CartePioche> pioche;
	private List<CarteMission> objectifs;
	
	
	public General() {
		this.pioche = new ArrayList<>();
	
		this.objectifs = new ArrayList<>();
	}
	
	public void ConstructPioche()
	{
		CartePioche carte1 = new CartePioche(NomTerritoireASIE.AFGHANISTAN.toString(),Unite.FANTASSIN.toString());
		this.pioche.add(carte1);
		CartePioche carte2 = new CartePioche(NomTerritoireASIE.INDE.toString(),Unite.FANTASSIN.toString());
		this.pioche.add(carte2);
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
