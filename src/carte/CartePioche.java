package carte;

public class CartePioche {
	private String territoire;
	private String unite;
	
	public CartePioche(String territoire, String unite) {
		this.territoire = territoire;
		this.unite = unite;
	}

	public String getTerritoire() {
		return territoire;
	}

	public void setTerritoire(String territoire) {
		this.territoire = territoire;
	}

	public String getUnite() {
		return unite;
	}

	public void setUnite(String unite) {
		this.unite = unite;
	}

	@Override
	public String toString() {
		return "CartePioche [territoire=" + territoire + ", unite=" + unite + "]";
	}
}
