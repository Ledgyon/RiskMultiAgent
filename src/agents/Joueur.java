package agents;

import java.awt.Color;
import java.io.IOException;
import java.sql.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import carte.CarteMission;
import carte.CartePioche;
import gui.JoueurGui;
import jade.core.AID;
import jade.core.AgentServicesTools;
import jade.core.ServiceException;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.ReceiverBehaviour;
import jade.core.messaging.TopicManagementHelper;
import jade.domain.DFSubscriber;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.proto.states.MsgReceiver;
import plateau.Continent;
import plateau.Territoire;

@SuppressWarnings("serial")
public class Joueur extends GuiAgent{

    private String couleur;
    private int nombreRegimentAPlacer;
    private int nombreRegimentMax;
    private List<CartePioche> main; // cartes que le joueur possede dans sa main (max 5)
    private CarteMission objectif; // objectif du joueur pour remporter la partie
    private List<String> territoires_temp; // territoires possedes par le joueur
    private List<Territoire> territoires; // territoires possedes par le joueur
    private List<Continent> continents; // permet de savoir quel continent le joueur a conquis pour l'attribution des renforts et pour les objectifs
    public static final int EXIT = 0;
    public static final int GET_INFO_TERRITOIRE = 1;
    /**
     * topic du joueur demandant les informations du territoire
     */
    AID topicTerritoire;
    /**
     * topic du joueur permettant de faire la repartition des regiments sur les territoires du joueur en debut de partie
     */
    AID topicRepartition;
    /**
     * topic du joueur retournant les informations du territoire
     */
    AID topicTerritoireRetour;

    private gui.JoueurGui window;

    @SuppressWarnings("deprecation")
	@Override
    protected void setup(){
        window = new gui.JoueurGui(this);
        window.display();
        
        this.territoires_temp = new ArrayList<>();
        this.territoires = new ArrayList<>();
        this.main = new ArrayList<>();
        this.nombreRegimentAPlacer = nombreRegimentMax = 20;

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

        //detectIntermediaire();

        //gestion topic manager pour la communication avec l'agent INTERMEDIARE pour avoir les infos plus precise du territoire acquis
        TopicManagementHelper topicHelper;
        try {
            topicHelper =  ( TopicManagementHelper ) getHelper (TopicManagementHelper.SERVICE_NAME) ;
            topicTerritoire = topicHelper.createTopic("INFO TERRITOIRE");
            topicHelper.register(topicTerritoire);
        } catch (ServiceException e) {
            e.printStackTrace();
        }

        //enregistrement de son adresse dans GENERAL
        AgentServicesTools.register(this, "liste joueur", "get AID joueur");

        //reception des cartes territoires et des cartes missions
        addBehaviour(new CyclicBehaviour(this) {
        	public void action() {
        		ACLMessage msg = receive();
        		if(msg != null) {
                    try {
                        if(msg.getContentObject().getClass().getName().equals("carte.CartePioche")) {
                            //reception
                            CartePioche temp = (CartePioche)msg.getContentObject();
                            //window.println(temp.toString());

                            //demande a INTERMEDIAIRE les infos du territoire
                            ACLMessage info_territoire = new ACLMessage(ACLMessage.INFORM);
                            info_territoire.setContent(temp.getTerritoire());
                            info_territoire.addReceiver(topicTerritoire);
                            send(info_territoire);
                            
                            //window.println("pb topic ++" + (Territoire)infoT.getContentObject());
                           /* Territoire tempT = (Territoire)msg.getContentObject();
                        	territoires.add(tempT);
                        	window.println("pb topic" + territoires.toString());*/
                        } else if(msg.getContentObject().getClass().getName().equals("carte.CarteMission")) {
                            // AJout de la carte mission donnÃ© par le General

                            CarteMission temp = (CarteMission) msg.getContentObject();
                            if(temp.getCouleur() != null) {
                                if (temp.getCouleur().equals(couleur)) {
                                    temp = new CarteMission(temp.getNbTerritoire());
                                }
                            }
                            objectif = temp;
                            window.println(objectif.toString());
                        } else /*if(msg.getContentObject().getClass().getName().equals("plateau.Territoire"))*/ {
                        	window.println("pb topic");
                        	Territoire tempT = (Territoire)msg.getContentObject();
                        	territoires.add(tempT);
                        	window.println("pb topic" + territoires.toString());
                        }
                    } catch (UnreadableException e) { // A DEFINIR DE NE RIEN FAIRE SI MESS VIENT D'UN TOPIC, SINON, REMETTRE LE throw new RuntimeException(e);
                        //throw new RuntimeException(e);
                    }

                }
        		else window.println("error territoire");
        		block();
        	}
        });

        topicRepartition = AgentServicesTools.generateTopicAID(this, "REPARTITION REGIMENT");

        //ecoute des messages radio
        addBehaviour(new ReceiverBehaviour(this, -1, MessageTemplate.MatchTopic(topicRepartition), true, (a, m)->{
            window.println("Message recu sur le topic " + topicRepartition.getLocalName() + ". Contenu " + m.getContent()
                    + " emis par :  " + m.getSender().getLocalName());
            assignationRegimentTerritoire();
            nouveauxRenforts();
        }));
        
        //A PARTIR DE MTN "PROPAGATE" NE SERT QUE POUR LE RENVOIE DES INFOS DE TERRITOIRE
        var model = MessageTemplate.MatchPerformative(ACLMessage.PROPAGATE);
        
        //Reception des info du territoire et stockage, fonction ne captant que les messages du model créer precedemment
        addBehaviour(new MsgReceiver(this,model,MsgReceiver.INFINITE,null,null){
        	protected void handleMessage(ACLMessage msg) {
        		if(msg!=null)
        		{
        			Territoire tempT = null;
                	try {
        				tempT = (Territoire)msg.getContentObject();
        			} catch (UnreadableException e) {
        				// TODO Auto-generated catch block
        				e.printStackTrace();
        			}
                    assert tempT != null;
                    window.println("Message recu sur le topic " + topicTerritoire.getLocalName() + ". Contenu " + tempT.toString()
                    + " emis par :  " + msg.getSender().getLocalName());
        			territoires.add(tempT);

                    window.println("Liste de territoire = " + territoires.toString());
        			
        		}
        		reset(model,MsgReceiver.INFINITE,null,null);
        	}
        });

        //addBehaviour(new ContractNetAttaque(this, new ACLMessage(ACLMessage.CFP)));


    }

    // fonction permettant la première assignation de régiments sur les territoires du joueur
    public void assignationRegimentTerritoire(){
        for(Territoire t:territoires) {
            t.setRegimentSurTerritoire(1);
            nombreRegimentAPlacer -= 1;
        }
        Random rand = new Random();
        while(nombreRegimentAPlacer!=0){
            int alea = rand.nextInt(territoires.size());
            territoires.get(alea).addRegimentSurTerritoire(1);
            nombreRegimentAPlacer--;
        }
        window.println(territoires.toString());
    }

    public void nouveauxRenforts(){
        window.println(String.valueOf(nombreRegimentMax));
        boolean b, combinaisonPossible;
        int nbFantassin=0, nbCavalier=0, nbCanon=0;
        int nbAddRegiment = (int) Math.floor(this.territoires.size()/3);
        if(nbAddRegiment<3)
            nbAddRegiment=3;
        nombreRegimentMax += nbAddRegiment;
        if(this.continents!=null)
            for(Continent c:continents)
                nombreRegimentMax += c.getRenfortObtenu();
        Random rand = new Random();
        b=rand.nextBoolean();
        for(CartePioche cp:main){
            switch(cp.getUnite()){
                case("FANTASSIN") -> nbFantassin++;
                case("CAVALIER") -> nbCavalier++;
                case("CANON") -> nbCanon++;
            }
        }
        if(b) {
            if ((nbFantassin > 0)&&(nbCavalier > 0)&&(nbCanon > 0)) {
                nombreRegimentMax+=10;
            }
            if (nbCanon >= 3) {
                nombreRegimentMax+=8;
            }
            if (nbCavalier >= 3) {
                nombreRegimentMax+=6;
            }
            if (nbFantassin >= 3) {
                nombreRegimentMax+=4;
            }
        }
        window.println(String.valueOf(nombreRegimentMax));
    }

    public boolean isDead(){
        return nombreRegimentMax == 0;
    }

    public Joueur getThisByCouleur(String couleur){
        if(this.couleur.equals(couleur))
            return this;
        return null;
    }

    public JoueurGui getWindow() { return window; }

    public String getCouleur() {
        return couleur;
    }

    public void setCouleur(String couleur) {
        this.couleur = couleur;
    }

    @Override
    protected void onGuiEvent(GuiEvent guiEvent) {
        if (guiEvent.getType() == Joueur.EXIT) {
            doDelete();
        }
        /*
        if (guiEvent.getType() == Joueur.GET_INFO_TERRITOIRE) {
        	//demande a INTERMEDIAIRE les infos du territoire
            ACLMessage info_territoire = new ACLMessage(ACLMessage.INFORM);
            info_territoire.setContent("yes");
            info_territoire.addReceiver(topicTerritoire);
            send(info_territoire);
            window.println("ouais le message territoire");

           // ACLMessage infoT = receive();
            //if(infoT == null) block();
		}*/
    }
}
