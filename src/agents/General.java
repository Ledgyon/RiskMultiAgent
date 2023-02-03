package agents;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import carte.CarteMission;
import carte.CartePioche;
import carte.enumerations.Unite;
import jade.core.AID;
import jade.core.AgentServicesTools;
import jade.domain.DFSubscriber;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLMessage;
import plateau.enumerations.NomContinents;
import plateau.enumerations.NomTerritoireAF;
import plateau.enumerations.NomTerritoireAN;
import plateau.enumerations.NomTerritoireAS;
import plateau.enumerations.NomTerritoireASIE;
import plateau.enumerations.NomTerritoireEU;
import plateau.enumerations.NomTerritoireOC;

import javax.swing.*;

public class General extends GuiAgent {
	private List<CartePioche> pioche;
	private List<CarteMission> objectifs;
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

	@Override
	protected void setup(){
		window = new gui.GeneralGui(this);
		window.display();
		window.println("Hello! Agent  " + getLocalName() + " is ready, my address is " + this.getAID().getName());
		window.println(this.toString());
		
		detectJoueurs();
		
		//System.out.println("Liste de joueurs"+joueurs);

		//sendCarteTerritoire();
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
                System.out.println("Liste de joueurs AID"+joueurs);
                window.println(dfd.getName().getLocalName() + " s'est inscrit en tant que joueur : " + model.getAllServices().get(0));
            }
            
            @Override
            public void onDeregister(DFAgentDescription dfd) { // lorsque le joueur est mort
                joueurs.remove(dfd.getName());
                window.println(dfd.getName().getLocalName() + " s'est desinscrit de  : " + model.getAllServices().get(0));
            }
        });
        System.out.println("Liste de joueurs"+joueurs);

    }

	private void sendCarteMission(){
		for(int j=0; j<joueurs.size(); j++){
			ACLMessage message = new ACLMessage(ACLMessage.PROPOSE);
			message.addReceiver(new AID(joueurs.get(j).getLocalName(), AID.ISLOCALNAME));
			try {
				message.setContentObject(objectifs.get(0));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			send(message);
			objectifs.remove(0);
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
	}

	@Override
	protected void onGuiEvent(GuiEvent guiEvent) {
		if (guiEvent.getType() == General.EXIT) {
			doDelete();
		}
		if (guiEvent.getType() == General.INITIALISATION_RISK) {
			sendCarteTerritoire();
			sendCarteMission();
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
		
		/*
		//JOKER
		carteP = new CartePioche("JOKER",Unite.FANTASSIN_CAVALERIE_CANON.toString());
		this.pioche.add(carteP);
		carteP = new CartePioche("JOKER",Unite.FANTASSIN_CAVALERIE_CANON.toString());
		this.pioche.add(carteP);
		*/
		
		Collections.shuffle(this.pioche);
		
	}

	public void constructObjectifs(){
		CarteMission carteM = new CarteMission(NomContinents.AMERIQUE_NORD.toString(),NomContinents.AFRIQUE.toString());
		objectifs.add(carteM);
		carteM = new CarteMission(NomContinents.AMERIQUE_NORD.toString(),NomContinents.OCEANIE.toString());
		objectifs.add(carteM);
		carteM = new CarteMission(NomContinents.ASIE.toString(),NomContinents.AFRIQUE.toString());
		objectifs.add(carteM);
		carteM = new CarteMission(NomContinents.ASIE.toString(),NomContinents.AMERIQUE_SUD.toString());
		objectifs.add(carteM);
		carteM = new CarteMission(NomContinents.EUROPE.toString(),NomContinents.AMERIQUE_SUD.toString(),"Autre");
		objectifs.add(carteM);
		carteM = new CarteMission(NomContinents.EUROPE.toString(),NomContinents.OCEANIE.toString(),"Autre");
		objectifs.add(carteM);
		carteM = new CarteMission(24);
		objectifs.add(carteM);
		carteM = new CarteMission(18,2);
		objectifs.add(carteM);
		carteM = new CarteMission("jaune");
		objectifs.add(carteM);
		carteM = new CarteMission("rouge");
		objectifs.add(carteM);
		carteM = new CarteMission("bleu");
		objectifs.add(carteM);
		carteM = new CarteMission("noir");
		objectifs.add(carteM);
		carteM = new CarteMission("violet");
		objectifs.add(carteM);
		carteM = new CarteMission("vert");
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
