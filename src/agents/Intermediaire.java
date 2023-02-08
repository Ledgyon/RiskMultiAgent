package agents;

import java.awt.Color;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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
import plateau.Monde;
import plateau.Territoire;

public class Intermediaire extends GuiAgent {

    private gui.IntermediaireGui window;

    private Monde plateau;
    public static final int EXIT = 0;

    public static final int LANCER_RISK = 1;

    private int nbAutorisation = 0; // boolean activant la phase de combat si == 6 (= tous les joueurs ont fini la phase de renfort)
    /**
     * topic du joueur demandant les informations du territoire
     */
    AID topicTerritoire;
    AID topicRegimentTeritoire;
    AID topicRepartition;
    AID topicAutorisationUpdateRegimentTerritoireAdjacent;
    AID topicUpdateContinent;
    
    /**
     * liste des joueurs
     */
    private ArrayList<AID> joueurs;
    
    //variable pour lancer updateRegimentTerritoireAjdacent
    int nbTerritoireUpdate = 0;
    
    //variable pour gerer qui joue en phase de combat
    int iJoueurTourCombat = 1;

    @SuppressWarnings({ "deprecation", "serial" })
	@Override
    protected void setup(){
        window = new gui.IntermediaireGui(this);
        window.display();
        window.setColor(Color.LIGHT_GRAY);
        window.println("Hello! Agent  " + getLocalName() + " is ready, my address is " + this.getAID().getName());
        
        detectJoueurs();

        plateau = new Monde();

        //AgentServicesTools.register(this, "intermediaire", "link");

        TopicManagementHelper topicHelper = null;
        try {
            topicHelper =  ( TopicManagementHelper ) getHelper (TopicManagementHelper.SERVICE_NAME) ;
            topicRepartition = topicHelper.createTopic("REPARTITION REGIMENT");
            topicAutorisationUpdateRegimentTerritoireAdjacent = topicHelper.createTopic("Update Regiment Territoire Adjacent");
            //topicTerritoireRetour = topicHelper.createTopic("RETOUR INFO TERRITOIRE");
            //topicHelper.register(topicTerritoireRetour);
            topicHelper.register(topicRepartition);
            topicHelper.register(topicAutorisationUpdateRegimentTerritoireAdjacent);
        } catch (ServiceException e) {
            e.printStackTrace();
        }
        
        AgentServicesTools.register(this, "intermediaire", "link");


        topicTerritoire = AgentServicesTools.generateTopicAID(this, "INFO TERRITOIRE");

        topicRegimentTeritoire = AgentServicesTools.generateTopicAID(this, "INFO REGIMENT TERRITOIRE");

        topicUpdateContinent = AgentServicesTools.generateTopicAID(this, "UPDATE CONTINENT");

        //ecoute des messages radio
        addBehaviour(new ReceiverBehaviour(this, -1, MessageTemplate.MatchTopic(topicTerritoire), true, (a, m)->{
            window.println("Message recu sur le topic " + topicTerritoire.getLocalName() + ". Contenu " + m.getContent()
                    + " emis par :  " + m.getSender().getLocalName());

            Territoire tempT = plateau.getTerritoireByName(m.getContent());
            window.println("Territoire complet = " + tempT + " class = " + tempT.getClass());
            		
            ACLMessage retour = m.createReply();
            //retour.setPerformative(ACLMessage.PROPAGATE); // IMPORTANT !!! Grace a ce mot cle PROPAGATE, cela va partir dans une fonction speciale de Joueur
            
            //init du model
            retour.setConversationId("send infos territoire");
            
            try {
				retour.setContentObject(tempT);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            send(retour);
        }));

        
        //init du model
        var model0 = MessageTemplate.MatchConversationId("update regiment territoire adjacent");
        
        //Reception des info du territoire et stockage, fonction ne captant que les messages du model créer precedemment
        addBehaviour(new MsgReceiver(this,model0,MsgReceiver.INFINITE,null,null){
        	protected void handleMessage(ACLMessage msg) {
        		if(msg!=null)
        		{
        			//Reception message
        			var infos = msg.getContent().split(",");
        			
                    window.println("\nMessage recu sur le model " + model0.toString() + ". Contenu " + msg.getContent().toString()
                    + " emis par :  " + msg.getSender().getLocalName());
                    
                    //Recherche territoire voulu du plateau
                    int nbRegiment = plateau.getTerritoireByName(infos[0]).getRegimentSurTerritoire();
                    
                    //Renvoie de l'info
                    ACLMessage retour = msg.createReply();
                    
                    //init du model
                    retour.setConversationId("retour update regiment territoire adjacent");
                    retour.setContent(msg.getContent()+","+nbRegiment);
                    send(retour);
                    
        			//territoires.add(tempT);

                    window.println("Renvoie du territoire avec le bon nombre de regiment = " + nbRegiment);
        			
        		}
        		reset(model0,MsgReceiver.INFINITE,null,null);
        	}
        });


        addBehaviour(new ReceiverBehaviour(this, -1, MessageTemplate.MatchTopic(topicRegimentTeritoire), true, (a, m)->{
            window.println("Message recu sur le topic " + topicRegimentTeritoire.getLocalName() + ". Contenu " + m.getContent()
                    + " emis par :  " + m.getSender().getLocalName());
            Territoire tempT = null;
            try {
                tempT = (Territoire) m.getContentObject();
            } catch (UnreadableException e) {
                //throw new RuntimeException(e);
            }

            plateau.getTerritoireByName(tempT.getNomTerritoire()).setRegimentSurTerritoire(tempT.getRegimentSurTerritoire());
            window.println(plateau.toString());
            
            nbTerritoireUpdate += 1;
            if(nbTerritoireUpdate == 42)
            {
            	nbTerritoireUpdate = 0;
            	autorisationRegimentTerritoireAdjacent();
            }
        }));


        addBehaviour(new ReceiverBehaviour(this, -1, MessageTemplate.MatchTopic(topicUpdateContinent), true, (a, m)->{
            window.println("Message recu sur le topic " + topicUpdateContinent.getLocalName() + ". Contenu " + m.getContent()
                    + " emis par :  " + m.getSender().getLocalName());
            List<Territoire> tempT = new ArrayList<>();
            try {
                tempT.addAll((List<Territoire>) m.getContentObject());
            } catch (UnreadableException e) {
                //throw new RuntimeException(e);
            }
            List<Continent> continentPossede = new ArrayList<>();
            for(Continent c: plateau.getContinents()){
                int nbTerritoire = 0;
                for(Territoire t:c.getTerritoires()){
                    for(Territoire ter:tempT){
                        if(ter.getNomTerritoire().equals(t.getNomTerritoire())){
                            nbTerritoire++;
                        }
                    }
                }
                if(nbTerritoire == c.getTerritoires().size()){
                    continentPossede.add(c);
                }
            }

            ACLMessage retour = m.createReply();

            //init du model
            retour.setConversationId("send update continent");

            try {
                retour.setContentObject((Serializable) continentPossede);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            send(retour);

            //window.println(plateau.toString());
        }));
        
        var model1 = MessageTemplate.MatchConversationId("autorisation phase combat");
        
        //Reception des autorisation de cmmencement de la phase de combat, des que tous les joueurs sont pret (fin phase de renfort), on peut lancer
        addBehaviour(new MsgReceiver(this,model1,MsgReceiver.INFINITE,null,null){
        	protected void handleMessage(ACLMessage msg) {
        		window.println("\nReception autorisation phase de combat de " + msg.getSender().getLocalName() + "\n");
        		nbAutorisation += 1;
        		if(nbAutorisation == 6)
        		{
        			nbAutorisation = 0; // reset pour la prochaine phase
        			phaseCombat(); //debut phase de combat
        		}
        		reset(model1,MsgReceiver.INFINITE,null,null);
        	}
        });
        
        //init du model
        var model2 = MessageTemplate.MatchConversationId("fin tour combat joueur");
        
        //Reception des notifications de fin de tour de combat des joueurs, pour permettre au prochain de commencer sa phase de combat
        addBehaviour(new MsgReceiver(this,model2,MsgReceiver.INFINITE,null,null){
        	protected void handleMessage(ACLMessage msg) {
    			if(iJoueurTourCombat < 6) // alors tous les joueurs n'ont pas joue
    			{
    				iJoueurTourCombat++; // pour passer au joueur suivant
    				phaseCombat(); // nouvel phase de combat pour ce nouveau joueur
    			}
    			else phaseManoeuvre(); // tous le monde a fait sa phase de combat, DEBUT PHASE MANOEUVRE
        		reset(model2,MsgReceiver.INFINITE,null,null);
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
    
    private void autorisationRegimentTerritoireAdjacent()
    {
    	window.println("\nEnvoie autorisation commencement Regiment Territoire Adjacent aux joueurs.");
    	ACLMessage assignRegiment = new ACLMessage(ACLMessage.INFORM);
        assignRegiment.addReceiver(topicAutorisationUpdateRegimentTerritoireAdjacent);
        send(assignRegiment);
    }
    
    /*
     * Notification au Joueur_iTourCombat qu'il peut commencer sa phase d'attaque
     */
    private void phaseCombat()
    {
    	window.println("\nDebut phase de combat");
    	String tourJoueur = "Joueur_"+iJoueurTourCombat; // iTourCombat variable globale
    	//window.println("tourJoueur = "+tourJoueur);
		int iJoueur = 0;
		while(!joueurs.get(iJoueur).getLocalName().toString().equals(tourJoueur)) // recherche du Joueur_iTourCombat (Joueur_1 -> Joueur_2 -> Joueur_3 -> ...)
		{
			//window.println(joueurs.get(iJoueur).getLocalName().toString() + " != " + tourJoueur);
			iJoueur++;
		}
		window.println("\nEnvoie autorisation commencement phase de combat a " + tourJoueur);
		
		//Envoie du message pour que le joueur commence son tour
    	ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
		message.setConversationId("debut phase combat");
		message.addReceiver(new AID(joueurs.get(iJoueur).getLocalName(), AID.ISLOCALNAME));
		send(message);
    }
    
    /*
     * Fonction pour commencer la phase de manoeuvre
     */
    private void phaseManoeuvre()
    {
    	window.println("\nDebut phase de manoeuvre");
    	
    	/*
    	 * A COMPLETER
    	 */
    }


    @Override
    protected void onGuiEvent(GuiEvent guiEvent) {
        if (guiEvent.getType() == Intermediaire.EXIT) {
            doDelete();
        }
        if (guiEvent.getType() == Intermediaire.LANCER_RISK){
            launchRisk();
        }
    }

    public void launchRisk(){
        window.println("Debut de la partie");

        ACLMessage assignRegiment = new ACLMessage(ACLMessage.INFORM);
        assignRegiment.addReceiver(topicRepartition);
        send(assignRegiment);
        
        
    }
}
