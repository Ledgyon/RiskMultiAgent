package agents;

import java.awt.Color;
import java.io.IOException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import java.util.*;


import carte.CarteMission;
import carte.CartePioche;
import gui.JoueurGui;
import jade.core.AID;
import jade.core.AgentServicesTools;
import jade.core.ServiceException;
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
    
    private List<Territoire> territoires; // territoires possedes par le joueur
    private List<Continent> continents; // permet de savoir quel continent le joueur a conquis pour l'attribution des renforts et pour les objectifs
    public static final int EXIT = 0;
    public static final int GET_INFO_TERRITOIRE = 1;
    
    private AID intermediaire;
    private AID general;
    /**
     * topic du joueur demandant les informations du territoire
     */
    AID topicTerritoire;
    /**
     * topic du joueur demandant de mettre à jour les régiments sur le plateau
     */
    AID topicRegimentTeritoire;
    /**
     * topic du joueur permettant de faire la repartition des regiments sur les territoires du joueur en debut de partie
     */
    AID topicRepartition;
    /**
     * topic du joueur permettant de mettre à jour les continents possedes
     */
    AID topicUpdateContinent;
    /*
     * OUAIS
     */
    AID topicAutorisationUpdateRegimentTerritoireAdjacent;
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
        
        this.territoires = new ArrayList<>();
        this.continents = new ArrayList<>();
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

        detectIntermediaire();
        detectGeneral();

        //gestion topic manager pour la communication avec l'agent INTERMEDIARE pour avoir les infos plus precise du territoire acquis
        TopicManagementHelper topicHelper;
        try {
            topicHelper =  ( TopicManagementHelper ) getHelper (TopicManagementHelper.SERVICE_NAME) ;
            topicRegimentTeritoire = topicHelper.createTopic("INFO REGIMENT TERRITOIRE");
            topicTerritoire = topicHelper.createTopic("INFO TERRITOIRE");
            topicUpdateContinent = topicHelper.createTopic("UPDATE CONTINENT");
            topicHelper.register(topicTerritoire);
            topicHelper.register(topicRegimentTeritoire);
            topicHelper.register(topicUpdateContinent);
        } catch (ServiceException e) {
            e.printStackTrace();
        }

        //enregistrement de son adresse dans GENERAL et INTERMEDIAIRE
        AgentServicesTools.register(this, "liste joueur", "get AID joueur");

      //A PARTIR DE MTN "PROPAGATE" NE SERT QUE POUR LE RENVOIE DES INFOS DE TERRITOIRE
        //var model0 = MessageTemplate.MatchPerformative(ACLMessage.PROPOSE);
        var model0 = MessageTemplate.MatchConversationId("init jeu");
        
        //Reception des info du territoire et stockage, fonction ne captant que les messages du model créer precedemment
        
        //reception des cartes territoires et des cartes missions
        addBehaviour(new MsgReceiver(this,model0,MsgReceiver.INFINITE,null,null){
        	protected void handleMessage(ACLMessage msg) {
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
                        } 
                        
                        /*
                         * OBSOLETE MAIS ON LAISSE POUR L'INSTANT
                         */
                        /*
                        else if(msg.getContentObject().getClass().getName().equals("plateau.Territoire")) { 
                        	window.println("pb topic");
                        	Territoire tempT = (Territoire)msg.getContentObject();
                        	territoires.add(tempT);
                        	window.println("pb topic" + territoires.toString());
                        }*/
                    } catch (UnreadableException e) { // A DEFINIR DE NE RIEN FAIRE SI MESS VIENT D'UN TOPIC, SINON, REMETTRE LE throw new RuntimeException(e);
                        //throw new RuntimeException(e);
                    }

                }
        		else window.println("error territoire");
        		block();
        		reset(model0,MsgReceiver.INFINITE,null,null);
        	}
        });

        topicRepartition = AgentServicesTools.generateTopicAID(this, "REPARTITION REGIMENT");

        //ecoute des messages radio
        addBehaviour(new ReceiverBehaviour(this, -1, MessageTemplate.MatchTopic(topicRepartition), true, (a, m)->{
            window.println("Message recu sur le topic " + topicRepartition.getLocalName() + ". Contenu " + m.getContent()
                    + " emis par :  " + m.getSender().getLocalName());
            
            //RENFORT
            assignationRegimentTerritoire();
            nouveauxRenforts();
            updateContinents();
            window.println(territoires.toString());
            
            
        }));
        
        topicAutorisationUpdateRegimentTerritoireAdjacent = AgentServicesTools.generateTopicAID(this, "Update Regiment Territoire Adjacent");

        //ecoute des messages radio
        addBehaviour(new ReceiverBehaviour(this, -1, MessageTemplate.MatchTopic(topicAutorisationUpdateRegimentTerritoireAdjacent), true, (a, m)->{
            window.println("Message recu sur le topic " + topicAutorisationUpdateRegimentTerritoireAdjacent.getLocalName() + ". Contenu " + m.getContent()
                    + " emis par :  " + m.getSender().getLocalName());
            
            //COMBAT
            infoRegimentTerritoireAdjacent();
            window.println("\n\nTerritoires adjacents regiments update :");
            window.println(territoires.toString());

            //MANOEUVRE
            manoeuvreRegiment();
        }));
        
        //A PARTIR DE MTN "PROPAGATE" NE SERT QUE POUR LE RENVOIE DES INFOS DE TERRITOIRE
        //var model1 = MessageTemplate.MatchPerformative(ACLMessage.PROPAGATE);
        
        //init du model
        var model1 = MessageTemplate.MatchConversationId("send infos territoire");
        
        //Reception des info du territoire et stockage, fonction ne captant que les messages du model créer precedemment
        addBehaviour(new MsgReceiver(this,model1,MsgReceiver.INFINITE,null,null){
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
                    window.println("Message recu sur le model " + model1.toString() + ". Contenu " + tempT.toString()
                    + " emis par :  " + msg.getSender().getLocalName());
        			territoires.add(tempT);

                    window.println("Liste de territoire = " + territoires.toString());
        			
        		}
        		reset(model1,MsgReceiver.INFINITE,null,null);
        	}
        });
        
        //init du model
        var model2 = MessageTemplate.MatchConversationId("retour update regiment territoire adjacent");
        
        //Reception des info du territoire et stockage, fonction ne captant que les messages du model créer precedemment
        addBehaviour(new MsgReceiver(this,model2,MsgReceiver.INFINITE,null,null){
        	protected void handleMessage(ACLMessage msg) {
        		if(msg!=null)
        		{
        			var infos = msg.getContent().split(",");
                    
                    window.println("Message recu sur le model " + model2.toString() + ". Contenu " + msg.getContent().toString()
                    + " emis par :  " + msg.getSender().getLocalName());
                    
                    // Affectation du nombre de regiment
                    territoires.get(Integer.parseInt(infos[1])).getTerritoires_adjacents().get(Integer.parseInt(infos[2])).setRegimentSurTerritoire(Integer.parseInt(infos[3]));
        			
                    //si dernier update, alors affichage
                    if(Integer.parseInt(infos[1]) == territoires.size()-1 && Integer.parseInt(infos[2]) == territoires.get(Integer.parseInt(infos[1])).getTerritoires_adjacents().size()-1)
                    {
                    	window.println("\n\nTerritoires adjacents regiments update :");
                        window.println(territoires.toString());
                        autorisationPhaseCombat();
                    }
        		}
        		reset(model2,MsgReceiver.INFINITE,null,null);
        	}
        });

        var model3 = MessageTemplate.MatchConversationId("send update continent");


        //addBehaviour(new ContractNetAttaque(this, new ACLMessage(ACLMessage.CFP)));
        addBehaviour(new MsgReceiver(this,model3,MsgReceiver.INFINITE,null,null){
            protected void handleMessage(ACLMessage msg) {
                if(msg!=null)
                {
                    List<Continent> continentList = new ArrayList<>();
                    try {
                        continentList = (List<Continent>) msg.getContentObject();
                    } catch (UnreadableException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    continents.addAll(continentList);
                }
                reset(model3,MsgReceiver.INFINITE,null,null);
            }
        });
        
      //init du model
        var model4 = MessageTemplate.MatchConversationId("debut phase combat");
        
        //Reception de la notification du debut de la phase de combat
        addBehaviour(new MsgReceiver(this,model4,MsgReceiver.INFINITE,null,null){
        	protected void handleMessage(ACLMessage msg) {
        		if(msg!=null)
        		{
                    window.println("\nMessage recu sur le model " + model4.toString() + " emis par :  " + msg.getSender().getLocalName());
                    phaseCombatJoueur();
        			
        		}
        		reset(model4,MsgReceiver.INFINITE,null,null);
        	}
        });

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
        for(Territoire ter:territoires){
            updateRegimentTerritoire(ter);
        }
    }

    public void updateRegimentTerritoire(Territoire ter){
        ACLMessage infoRegimentTerritoire = new ACLMessage(ACLMessage.INFORM);
        try {
            infoRegimentTerritoire.setContentObject(ter);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        infoRegimentTerritoire.addReceiver(topicRegimentTeritoire);
        send(infoRegimentTerritoire);
    }

    // Permet de rajouter d'obtenir de nouveaux renforts
    public void nouveauxRenforts(){
        window.println(String.valueOf(nombreRegimentMax));
        int nbAddRegiment = this.territoires.size()/3;
        if(nbAddRegiment<3)
            nbAddRegiment=3;
        nombreRegimentMax += nbAddRegiment;
        if(this.continents!=null)
            for(Continent c:continents)
                nombreRegimentMax += c.getRenfortObtenu();
        if(main.size() >= 3)
            nouveauxRenfortsMain();
        window.println(String.valueOf(nombreRegimentMax));
    }

    // Permet de rajouter d'obtenir de nouveaux renforts par rapport aux cartes se trouvant dans la main du joueur
    private void nouveauxRenfortsMain(){
        int nbFantassin=0, nbCavalier=0, nbCanon=0;
        Map<Integer,String> fantassins = new HashMap<>();
        Map<Integer,String> cavaliers = new HashMap<>();
        Map<Integer,String> canons = new HashMap<>();
        Random rand = new Random();
        boolean joker=false;
        boolean echange=rand.nextBoolean();
        for(int i=0; i< main.size(); i++){
            switch(main.get(i).getUnite()){
                case("FANTASSIN") -> {
                    nbFantassin++;
                    fantassins.put(i,main.get(i).getTerritoire());
                }
                case("CAVALIER") -> {
                    nbCavalier++;
                    cavaliers.put(i,main.get(i).getTerritoire());
                }
                case("CANON") -> {
                    nbCanon++;
                    canons.put(i,main.get(i).getTerritoire());
                }
                case("FANTASSIN_CAVALERIE_CANON") -> {
                    nbCanon++;
                    fantassins.put(i,main.get(i).getTerritoire());
                    nbCavalier++;
                    cavaliers.put(i,main.get(i).getTerritoire());
                    nbFantassin++;
                    canons.put(i,main.get(i).getTerritoire());
                    joker=true;
                }
            }
        }
        if(echange) {
            if ((nbFantassin > 0) && (nbCavalier > 0) && (nbCanon > 0) && !joker){
                for(Integer j:fantassins.keySet()){
                    returnCarteGeneral(j);
                    for(Territoire ter:territoires)
                        if(ter.getNomTerritoire().equals(fantassins.get(j)))
                            nombreRegimentMax+=2;
                    fantassins.remove(j);
                    break;
                }
                for(Integer k:cavaliers.keySet()){
                    returnCarteGeneral(k);
                    for(Territoire ter:territoires)
                        if(ter.getNomTerritoire().equals(cavaliers.get(k)))
                            nombreRegimentMax+=2;
                    cavaliers.remove(k);
                    break;
                }
                for(Integer t:canons.keySet()){
                    returnCarteGeneral(t);
                    for(Territoire ter:territoires)
                        if(ter.getNomTerritoire().equals(canons.get(t)))
                            nombreRegimentMax+=2;
                    canons.remove(t);
                    break;
                }
                nombreRegimentMax+=10;
                nbFantassin--;
                nbCanon--;
                nbCavalier--;
            }
            if(joker && ((nbFantassin > 1) && (nbCanon > 1)) || ((nbFantassin > 1) && (nbCavalier > 1)) || ((nbCavalier > 1) && (nbCanon > 1))){
                if(nbFantassin == 1){
                    for(Integer j:fantassins.keySet()){
                        returnCarteGeneral(j);
                        fantassins.remove(j);
                        break;
                    }
                    for(Integer k:cavaliers.keySet()){
                        returnCarteGeneral(k);
                        for(Territoire ter:territoires)
                            if(ter.getNomTerritoire().equals(cavaliers.get(k)))
                                nombreRegimentMax+=2;
                        cavaliers.remove(k);
                        break;
                    }
                    for(Integer t:canons.keySet()){
                        returnCarteGeneral(t);
                        for(Territoire ter:territoires)
                            if(ter.getNomTerritoire().equals(canons.get(t)))
                                nombreRegimentMax+=2;
                        canons.remove(t);
                        break;
                    }
                }
                if(nbCanon == 1){
                    for(Integer j:canons.keySet()){
                        returnCarteGeneral(j);
                        canons.remove(j);
                        break;
                    }
                    for(Integer k:cavaliers.keySet()){
                        returnCarteGeneral(k);
                        for(Territoire ter:territoires)
                            if(ter.getNomTerritoire().equals(cavaliers.get(k)))
                                nombreRegimentMax+=2;
                        cavaliers.remove(k);
                        break;
                    }
                    for(Integer t:fantassins.keySet()){
                        returnCarteGeneral(t);
                        for(Territoire ter:territoires)
                            if(ter.getNomTerritoire().equals(fantassins.get(t)))
                                nombreRegimentMax+=2;
                        fantassins.remove(t);
                        break;
                    }
                }
                if(nbCavalier == 1){
                    for(Integer j:cavaliers.keySet()){
                        returnCarteGeneral(j);
                        cavaliers.remove(j);
                        break;
                    }
                    for(Integer k:fantassins.keySet()){
                        returnCarteGeneral(k);
                        for(Territoire ter:territoires)
                            if(ter.getNomTerritoire().equals(fantassins.get(k)))
                                nombreRegimentMax+=2;
                        fantassins.remove(k);
                        break;
                    }
                    for(Integer t:canons.keySet()){
                        returnCarteGeneral(t);
                        for(Territoire ter:territoires)
                            if(ter.getNomTerritoire().equals(canons.get(t)))
                                nombreRegimentMax+=2;
                        canons.remove(t);
                        break;
                    }
                }
                nombreRegimentMax+=10;
                nbFantassin--;
                nbCanon--;
                nbCavalier--;
            }
            int troisCartes = 0;
            if (nbCanon >= 3) {
                for(Integer j:canons.keySet()){
                    returnCarteGeneral(j);
                    if(!canons.get(j).equals("JOKER"))
                        for(Territoire ter:territoires) {
                            if (ter.getNomTerritoire().equals(canons.get(j))) {
                                nombreRegimentMax += 2;
                            }
                        }
                    else {
                        nbCavalier--;
                        nbFantassin--;
                    }
                    nbCanon--;
                    canons.remove(j);
                    troisCartes++;
                    if(troisCartes==3)
                        break;
                }
                nombreRegimentMax+=8;
            }
            if (nbCavalier >= 3) {
                for(Integer j:cavaliers.keySet()){
                    returnCarteGeneral(j);
                    if(!cavaliers.get(j).equals("JOKER"))
                        for(Territoire ter:territoires) {
                            if (ter.getNomTerritoire().equals(cavaliers.get(j))) {
                                nombreRegimentMax += 2;
                            }
                        }
                    else {
                        nbCanon--;
                        nbFantassin--;
                    }
                    nbCavalier--;
                    cavaliers.remove(j);
                    troisCartes++;
                    if(troisCartes==3)
                        break;
                }
                nombreRegimentMax+=6;
            }
            if (nbFantassin >= 3) {
                for(Integer j:fantassins.keySet()){
                    returnCarteGeneral(j);
                    if(!fantassins.get(j).equals("JOKER"))
                        for(Territoire ter:territoires) {
                            if (ter.getNomTerritoire().equals(fantassins.get(j))) {
                                nombreRegimentMax += 2;
                            }
                        }
                    else {
                        nbCavalier--;
                        nbCanon--;
                    }
                    nbFantassin--;
                    fantassins.remove(j);
                    troisCartes++;
                    if(troisCartes==3)
                        break;
                }
                nombreRegimentMax+=4;
            }
        }
    }

    // Renvoie des cartes de la main vers la pioche du General
    private void returnCarteGeneral(int i)
    {
        //System.out.println("yes");
        ACLMessage message = new ACLMessage(ACLMessage.PROPOSE);
        message.setConversationId("return carte");
        message.addReceiver(new AID(general.getLocalName(), AID.ISLOCALNAME));

        try {
            message.setContentObject(main.get(i));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        send(message);
        main.remove(i);

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
                if(dfd.getName().equals(general)) {
                    intermediaire=null;
                    window.println(dfd.getName().getLocalName() + " s'est desinscrit de  : " + model.getAllServices().get(0));
                }
            }
        });
    }

    // fonction qui permet de mettre à jour les continents possedes
    public void updateContinents(){
        ACLMessage infoRegimentTerritoire = new ACLMessage(ACLMessage.INFORM);
        try {
            infoRegimentTerritoire.setContentObject((Serializable) territoires);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        infoRegimentTerritoire.addReceiver(topicUpdateContinent);
        send(infoRegimentTerritoire);
    }

    private void detectGeneral() {
        var model = AgentServicesTools.createAgentDescription("general", "link");

        //souscription au service des pages jaunes pour recevoir une alerte en cas mouvement sur le service travel agency'seller
        addBehaviour(new DFSubscriber(this, model) {
            @Override
            public void onRegister(DFAgentDescription dfd) {
                general=dfd.getName();
                window.println(dfd.getName().getLocalName() + " s'est inscrit en tant que general : " + model.getAllServices().get(0));
            }

            @Override
            public void onDeregister(DFAgentDescription dfd) {
                if(dfd.getName().equals(general)) {
                    general=null;
                    window.println(dfd.getName().getLocalName() + " s'est desinscrit de  : " + model.getAllServices().get(0));
                }
            }

        });

    }
    
    /*
     * Fonction pour update les informations des nombres de regiment présent sur les territoires adjacents
     */
    public void infoRegimentTerritoireAdjacent()
    {
    	int i,j;
    	for(i = 0; i < this.territoires.size(); i++) // parcours des territoires
    	{
    		for(j = 0; j < this.territoires.get(i).getTerritoires_adjacents().size(); j++) // parcours de tous les territoires adjacents
    		{
    			//variable pour raccourcir le nom
    			Territoire t_actuel = this.territoires.get(i).getTerritoires_adjacents().get(j);
    			if(this.territoires.contains(t_actuel)) // alors on possède déja l'info
    			{
    				//affectation nombre de regiment
    				Territoire temp = getTerritoireByName(this.territoires.get(i).getTerritoires_adjacents().get(j).getNomTerritoire());
    				this.territoires.get(i).getTerritoires_adjacents().get(j).setRegimentSurTerritoire(temp.getRegimentSurTerritoire());
    				window.println("Info déjà en notre possession : "+temp);
    				//si dernier update, alors affichage
                    if(i == territoires.size()-1 && j == territoires.get(i).getTerritoires_adjacents().size()-1)
                    {
                    	window.println("\n\nTerritoires adjacents regiments update :");
                        window.println(territoires.toString());
                        
                        //FIN PHASE DE RENFORT
                        autorisationPhaseCombat();
                    }
    			}
    			else // on demande a intermedaire d'update
    			{
    				ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
    				message.setConversationId("update regiment territoire adjacent");
    				message.addReceiver(new AID(intermediaire.getLocalName(), AID.ISLOCALNAME));
    				message.setContent(t_actuel.getNomTerritoire()+","+i+","+j);
    				send(message);
    				
    				//le retour ce fera grace au model3 dans setup()
    			}
    		}
    	}
    }

    private void manoeuvreRegiment() {
        Random rand = new Random();
        //boolean manoeuvre = rand.nextBoolean();
        boolean manoeuvre = true;
        int noTerritoire,nbRegiment;
        if(manoeuvre){
            List<Territoire> tempT = new ArrayList<>();
            for(Territoire t1:territoires){
                for(Territoire t2: t1.getTerritoires_adjacents()){
                    if(territoires.contains(t2)){
                        tempT.add(t1);
                    }
                }
            }
            if(!tempT.isEmpty()) {
                noTerritoire = rand.nextInt(tempT.size());
                nbRegiment = rand.nextInt(tempT.get(noTerritoire).getRegimentSurTerritoire() - 1) + 1;
                tempT.get(noTerritoire).addRegimentSurTerritoire(-nbRegiment);
                if(tempT.get(noTerritoire).getTerritoires_adjacents().contains(tempT.get(noTerritoire+1))){
                    tempT.get(noTerritoire+1).addRegimentSurTerritoire(nbRegiment);
                }
                System.out.println(nbRegiment +" \t\t\t" +tempT.get(noTerritoire)+"\t\t"+tempT.get(noTerritoire+1));
            }
        }
    }
    
    /*
     * Fonction que tous les joueurs envoie a intermediaire pour notifier qu'ils ont fini leurs phase de renfort
     */
    private void autorisationPhaseCombat()
    {
    	window.println("\nEnvoie autorisation commencement phase de combat a Intermediaire.");
    	ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
		message.setConversationId("autorisation phase combat");
		message.addReceiver(new AID(intermediaire.getLocalName(), AID.ISLOCALNAME));
		send(message);
    }
    
    private void phaseCombatJoueur()
    {
    	/*
    	 * Gerer les attaques A FAIRE
    	 */
    	
    	//FIN DU TOUR (plus de combat a mener) ON INDIQUE A INTERMEDIAIRE QU'ON A TERMINE POUR QU'UN AUTRE JOUEUR COMMENCE SON TOUR
    	window.println("\nFin du tour de la phase de combat, Notification a Intermediaire fin de tour");
    	ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
		message.setConversationId("fin tour combat joueur");
		message.addReceiver(new AID(intermediaire.getLocalName(), AID.ISLOCALNAME));
		send(message);
    }
    
    public Territoire getTerritoireByName(String territoire){
        for(Territoire t:territoires)
                if (t.getNomTerritoire().equals(territoire))
                    return t;
        return null;
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
