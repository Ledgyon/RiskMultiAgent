package agents;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import carte.CarteMission;
import carte.CartePioche;
import gui.JoueurGui;
import jade.core.AID;
import jade.core.AgentServicesTools;
import jade.core.ServiceException;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.messaging.TopicManagementHelper;
import jade.domain.DFSubscriber;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.proto.states.MsgReceiver;
import plateau.Territoire;

@SuppressWarnings("serial")
public class Joueur extends GuiAgent{

    private String couleur;
    private int nombreRegiment;
    private List<CartePioche> main; // cartes que le joueur possede dans sa main (max 5)
    private CarteMission objectif; // objectif du joueur pour remporter la partie
    private List<String> territoires_temp; // territoires possedes par le joueur
    private List<Territoire> territoires; // territoires possedes par le joueur
    private List<String> continents; // permet de savoir quel continent le joueur a conquis pour l'attribution des renforts et pour les objectifs
    public static final int EXIT = 0;
    public static final int GET_INFO_TERRITOIRE = 1;
    private AID intermediaire;
    /**
     * topic du joueur demandant les informations du territoire
     */
    AID topicTerritoire;
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
        /*
         * Probleme ou le TopicManagementHelper est considere comme inactif donc impossiblite de creer un topic
         * et donc la conversation entre le joueur et l'intermediaire ne se fera jamais
         */
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

    private void detectIntermediaire() {
        var model = AgentServicesTools.createAgentDescription("intermediaire", "link");

        //souscription au service des pages jaunes pour recevoir une alerte en cas mouvement sur le service travel agency'seller
        addBehaviour(new DFSubscriber(this, model) {
            @Override
            public void onRegister(DFAgentDescription dfd) {
                intermediaire=dfd.getName();
                window.println(dfd.getName().getLocalName() + " s'est inscrit en tant qu'intermediaire : " + model.getAllServices().get(0));
            }

            @Override
            public void onDeregister(DFAgentDescription dfd) {
                if(dfd.getName().equals(intermediaire)) {
                    intermediaire=null;
                    window.println(dfd.getName().getLocalName() + " s'est desinscrit de  : " + model.getAllServices().get(0));
                }
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

    public JoueurGui getWindow() { return window; }

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

    public AID getIntermediaire() {
        return intermediaire;
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
