package agents;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import carte.CarteMission;
import carte.CartePioche;
import carte.enumerations.TypeMission;
import carte.enumerations.Unite;
import jade.core.AID;
import jade.core.AgentServicesTools;
import jade.domain.DFSubscriber;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.proto.states.MsgReceiver;
import plateau.enumerations.NomContinents;
import plateau.enumerations.NomTerritoireAF;
import plateau.enumerations.NomTerritoireAN;
import plateau.enumerations.NomTerritoireAS;
import plateau.enumerations.NomTerritoireASIE;
import plateau.enumerations.NomTerritoireEU;
import plateau.enumerations.NomTerritoireOC;

public class General extends GuiAgent {
	private List<CartePioche> pioche;
	private List<CartePioche> defaussePioche;
	private List<CarteMission> objectifs;
	private List<String> strategies;
	private gui.GeneralGui window;
	
	/**
     * code pour ajout de livre par la gui
     */
    public static final int EXIT = 0;
    /**
     * code pour achat de livre par la gui
     */
    public static final int INITIALISATION_RISK = 1;
	
	/**
     * liste des joueurs
     */
    private ArrayList<AID> joueurs;
    

	@SuppressWarnings({ "deprecation", "serial" })
	@Override
	protected void setup(){
		window = new gui.GeneralGui(this);
		window.display();
		window.println("Hello! Agent  " + getLocalName() + " is ready, my address is " + this.getAID().getName());
		window.println(this.toString());
		detectJoueurs();
		
		this.strategies = new ArrayList<>();
		strategies.addAll(Arrays.asList("attaque","aleatoire","defense","passive","equilibre","revanche"));
		//melange des strategies
		Collections.shuffle(strategies);

		AgentServicesTools.register(this, "general", "link");
		
		var model0 = MessageTemplate.MatchConversationId("return carte");

		addBehaviour(new MsgReceiver(this,model0,MsgReceiver.INFINITE,null,null) {
			 protected void handleMessage(ACLMessage msg) {
				 if (msg != null) {
					 try {
						 CartePioche temp = (CartePioche)msg.getContentObject();
						 defaussePioche.add(temp);
					 } catch (UnreadableException e) {
						 //throw new RuntimeException(e);
					 }
				 } else window.println("error cartes");
				 block();
				 reset(model0,MsgReceiver.INFINITE,null,null);
			 }
		});
		
		var model1 = MessageTemplate.MatchConversationId("demande carte pioche");

		addBehaviour(new MsgReceiver(this,model1,MsgReceiver.INFINITE,null,null) {
			 protected void handleMessage(ACLMessage msg) {
				 if (msg != null) {
					 
					 if(pioche.isEmpty())
	     				{
							Collections.shuffle(defaussePioche);
						 	pioche.addAll(defaussePioche);
						 	defaussePioche = new ArrayList<>();
	     				}
					 
					ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
     				message.setConversationId("envoie carte pioche");
     				message.addReceiver(new AID(msg.getSender().getLocalName(), AID.ISLOCALNAME));
     				try {
						message.setContentObject(pioche.get(0));
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
     				
     				pioche.remove(0);
     				     				
     				send(message);
				 }
				 reset(model1,MsgReceiver.INFINITE,null,null);
			 }
		});
	}
	
	/**
     * ecoute des evenement de type enregistrement en tant que joueur, pour avoir acces a leurs adresses
     */
    private void detectJoueurs() {
        var model = AgentServicesTools.createAgentDescription("liste joueur", "get AID joueur");
        this.joueurs = new ArrayList<>();

        //souscription au service des pages jaunes pour recevoir une alerte en cas mouvement sur le service travel agency'seller
        addBehaviour(new DFSubscriber(this, model) {
            @Override
            public void onRegister(DFAgentDescription dfd) { //au debut
                joueurs.add(dfd.getName());
                //System.out.println("Liste de joueurs AID"+joueurs);
                window.println(dfd.getName().getLocalName() + " s'est inscrit en tant que joueur : " + model.getAllServices().get(0));
            }
            
            @Override
            public void onDeregister(DFAgentDescription dfd) { // lorsque le joueur est mort
                joueurs.remove(dfd.getName());
                window.println(dfd.getName().getLocalName() + " s'est desinscrit de  : " + model.getAllServices().get(0));
            }
        });
        //System.out.println("Liste de joueurs"+joueurs);

    }

	private void sendCarteMissionEtStrat(){
		for(int j=0; j<joueurs.size(); j++){
			ACLMessage message = new ACLMessage(ACLMessage.PROPOSE);
			message.setConversationId("init jeu");
			message.addReceiver(new AID(joueurs.get(j).getLocalName(), AID.ISLOCALNAME));
			try {
				message.setContentObject(objectifs.get(0));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			message.setEncoding(strategies.get(0));
			send(message);
			objectifs.remove(0);
			strategies.remove(0);
		}
	}
    
    private void sendCarteTerritoire()
    {
    	int i = 0;
		int i_carte = 0;
		while(i_carte < pioche.size()) // alors on envoie une carte a chaque joueurs jusqu'a ce que la liste "pioche" soit vide
		{
			//System.out.println("yes");
			ACLMessage message = new ACLMessage(ACLMessage.PROPOSE);
			message.setConversationId("init jeu");
			message.addReceiver(new AID(joueurs.get(i).getLocalName(), AID.ISLOCALNAME));
			try {
				//System.out.println("On attend les actions");
				Thread.sleep(1);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			try {
				message.setContentObject(pioche.get(i_carte));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			send(message);

			window.println("Carte "+pioche.get(i_carte));
			window.println("MSG "+message.toString());

			i_carte++;
			//boucle pour gerer l'envoie aux joueurs
			if (i != (joueurs.size() - 1)) {
				i++;
			} else i = 0;
		}
		
    }
	
	public General() {
		// init de la pioche
		this.pioche = new ArrayList<>();
		constructPioche();
		this.objectifs = new ArrayList<>();
		constructObjectifs();
		this.defaussePioche = new ArrayList<>();
	}

	@Override
	protected void onGuiEvent(GuiEvent guiEvent) {
		if (guiEvent.getType() == General.EXIT) {
			doDelete();
		}
		if (guiEvent.getType() == General.INITIALISATION_RISK) {
			sendCarteTerritoire();
			sendCarteMissionEtStrat();
			addJokerShuffle();
		}
	}

	public void addJokerShuffle(){
		//JOKER
		CartePioche carteP = new CartePioche("JOKER",Unite.FANTASSIN_CAVALERIE_CANON.toString());
		this.pioche.add(carteP);
		carteP = new CartePioche("JOKER",Unite.FANTASSIN_CAVALERIE_CANON.toString());
		this.pioche.add(carteP);


		Collections.shuffle(this.pioche);

		window.println(pioche.toString());
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
		
		Collections.shuffle(this.pioche);
		
	}

	public void constructObjectifs(){
		CarteMission carteM = new CarteMission(NomContinents.AMERIQUE_NORD.toString(),NomContinents.AFRIQUE.toString(),TypeMission.CONTINENTS.toString());
		objectifs.add(carteM);
		carteM = new CarteMission(NomContinents.AMERIQUE_NORD.toString(),NomContinents.OCEANIE.toString(),TypeMission.CONTINENTS.toString());
		objectifs.add(carteM);
		carteM = new CarteMission(NomContinents.ASIE.toString(),NomContinents.AFRIQUE.toString(),TypeMission.CONTINENTS.toString());
		objectifs.add(carteM);
		carteM = new CarteMission(NomContinents.ASIE.toString(),NomContinents.AMERIQUE_SUD.toString(),TypeMission.CONTINENTS.toString());
		objectifs.add(carteM);
		carteM = new CarteMission(NomContinents.EUROPE.toString(),NomContinents.AMERIQUE_SUD.toString(),"Autre",TypeMission.CONTINENTS.toString());
		objectifs.add(carteM);
		carteM = new CarteMission(NomContinents.EUROPE.toString(),NomContinents.OCEANIE.toString(),"Autre",TypeMission.CONTINENTS.toString());
		objectifs.add(carteM);
		carteM = new CarteMission(24,TypeMission.TERRITOIRES.toString());
		objectifs.add(carteM);
		carteM = new CarteMission(18,2,TypeMission.TERRITOIRES_ET_ARMEES_MIN.toString());
		objectifs.add(carteM);
		carteM = new CarteMission("jaune",TypeMission.COULEUR.toString());
		objectifs.add(carteM);
		carteM = new CarteMission("rouge",TypeMission.COULEUR.toString());
		objectifs.add(carteM);
		carteM = new CarteMission("bleu",TypeMission.COULEUR.toString());
		objectifs.add(carteM);
		carteM = new CarteMission("noir",TypeMission.COULEUR.toString());
		objectifs.add(carteM);
		carteM = new CarteMission("violet",TypeMission.COULEUR.toString());
		objectifs.add(carteM);
		carteM = new CarteMission("vert",TypeMission.COULEUR.toString());
		objectifs.add(carteM);

		Collections.shuffle(this.objectifs);
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
		return "pioche=\n" + pioche + "\nobjectifs=\n" + objectifs;
	}

		
}
