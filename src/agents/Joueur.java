package agents;

public class Joueur {

    private String couleur;
    private int nombreRegiment;

    public boolean isDead(){
        return nombreRegiment == 0;
    }

    public Joueur getThisByCouleur(String couleur){
        if(this.couleur==couleur)
            return this;
        return null;
    }

    public String getCouleur() {
        return couleur;
    }

    public void setCouleur(String couleur) {
        this.couleur = couleur;
    }

    public int getNombreRegiment() {
        return nombreRegiment;
    }

    public void setNombreRegiment(int nombreRegiment) {
        this.nombreRegiment = nombreRegiment;
    }

}
