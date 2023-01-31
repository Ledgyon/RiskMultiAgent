package agents;

import java.awt.Color;
import java.util.List;

import carte.CarteMission;
import carte.CartePioche;
import jade.core.AgentServicesTools;
import jade.core.ServiceException;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.messaging.TopicManagementHelper;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import plateau.Territoire;

public class Joueur extends GuiAgent{

    private String couleur;
    private int nombreRegiment;
    private List<CartePioche> main; // cartes que le joueur possede dans sa main (max 5)
    private CarteMission objectif; // objectif du joueur pour remporter la partie
    private List<Territoire> territoires; // territoires possedes par le joueur
    public static final int EXIT = 0;

    private gui.JoueurGui window;
    
    /**
     * topic du joueur demandant les informations du territoire
     */
    private AID topicTerritoire;

    @Override
    protected void setup(){
        window = new gui.JoueurGui(this);
        window.display();
        
        //gestion couleur des joueurs
        switch(window.getNoJoueurGui()){
            case(1)-> {
                window.setColor(Color.YELLOW);
                this.couleur = "jaune";
            }
            case(2)-> {
                window.setColor(Color.RED);
                this.couleur = "rouge";
            }
            case(3)-> {
                window.setColor(Color.BLUE);
                this.couleur = "bleu";
                window.getjTextArea().setForeground(Color.WHITE);
            }
            case(4)-> {
                window.setColor(Color.BLACK);
                this.couleur = "noir";
                window.getjTextArea().setForeground(Color.WHITE);
            }
            case(5)-> {
                window.setColor(Color.MAGENTA);
                this.couleur = "violet";
            }
            case(6)-> {
                window.setColor(Color.GREEN);
                this.couleur = "vert";
            }
        }
        window.println("Hello! Agent  " + getLocalName() + " is ready, my address is " + this.getAID().getName());
        
        //gestion topic manager pour la communication avec l'agent INTERMEDIARE pour avoir les infos plus precise du territoire acquis
        TopicManagementHelper topicHelper = null;
        try {
            topicHelper = (TopicManagementHelper) getHelper(TopicManagementHelper.SERVICE_NAME);
            topicTerritoire = topicHelper.createTopic("INFO TERRITOIRE");
            topicHelper.register(topicTerritoire);
        } catch (ServiceException e) {
            e.printStackTrace();
        }

        //enregistrement de son adresse dans GENERAL
        AgentServicesTools.register(this, "liste joueur", "get AID joueur");
        
        //reception des cartes territoires
        addBehaviour(new CyclicBehaviour(this) {
        	public void action() {
        		ACLMessage msg = receive();
        		if(msg != null) {
        			/*
        			String values[] = msg.getContent().split(",");
        			System.out.println("Msg = " + values[0] + " " + values[1] );
        			*/
        			try {
        				//reception
        				CartePioche temp = (CartePioche)msg.getContentObject();
        				
        				//demande a INTERMEDIAIRE les infos du territoire
        				ACLMessage info_territoire = new ACLMessage(ACLMessage.REQUEST);
        				info_territoire.setContent(temp.getTerritoire());
        				info_territoire.addReceiver(topicTerritoire);
        	            send(info_territoire);
        	            
        	            ACLMessage infoT = receive();
        	            if(infoT == null) block();
        	            
        	            
        	            //affichage
						window.println("Msg = " + msg.getContentObject().getClass());
					} catch (UnreadableException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
        		}
        		else window.println("error");
        		block();
        	}
        });
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
