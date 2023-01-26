package agents;

import java.util.List;

import carte.CarteMission;
import carte.CartePioche;

public class General {
	private List<CartePioche> pioche;
	private List<CarteMission> objectifs;
	
	public General(List<CartePioche> pioche, List<CarteMission> objectifs) {
		super();
		this.pioche = pioche;
		this.objectifs = objectifs;
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
