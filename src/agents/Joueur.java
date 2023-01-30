package agents;

import java.awt.*;
import java.util.List;
import java.util.Objects;

import carte.CarteMission;
import carte.CartePioche;
import gui.JoueurGui;
import jade.core.AgentServicesTools;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPANames;
import jade.gui.AgentWindowed;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import jade.gui.SimpleWindow4Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import plateau.Territoire;

public class Joueur extends GuiAgent{

    private String couleur;
    private int nombreRegiment;
    private List<CartePioche> main; // cartes que le joueur possede dans sa main (max 5)
    private CarteMission objectif; // objectif du joueur pour remporter la partie
    private List<Territoire> territoires; // territoires possedes par le joueur
    public static final int EXIT = 0;

    private gui.JoueurGui window;

    @Override
    protected void setup(){
        final Object[] args = getArguments(); // Recuperation des arguments
        window = new gui.JoueurGui(this);
        window.display();
        switch(window.getNoJoueurGui()){
            case(1)-> window.setColor(Color.YELLOW);
            case(2)-> window.setColor(Color.RED);
            case(3)-> window.setColor(Color.BLUE);
            case(4)-> window.setColor(Color.BLACK);
            case(5)-> window.setColor(Color.MAGENTA);
            case(6)-> window.setColor(Color.GREEN);
        }
        window.println("Hello! Agent  " + getLocalName() + " is ready, my address is " + this.getAID().getName());

    }

    public boolean isDead(){
        return nombreRegiment == 0;
    }

    public Joueur getThisByCouleur(String couleur){
        if(this.couleur.equals(couleur))
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

    @Override
    protected void onGuiEvent(GuiEvent guiEvent) {
        if (guiEvent.getType() == Joueur.EXIT) {
            doDelete();
        }
    }
}
