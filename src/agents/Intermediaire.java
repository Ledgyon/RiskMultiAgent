package agents;

import java.awt.Color;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

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
    AID topicUpdateRegimentAdjacent; // apres un combat, envoie a tous les joueurs l'update des regiments pour leurs territoires adjacents
    AID topicAffichageFinTour;
    
    /**
     * liste des joueurs
     */
    private ArrayList<AID> joueurs;
    
    /*
     * map cle : nom du territoire / valeur : adresse du joueur la possedant
     */
    private Map<String,AID> mapTerritoireJoueur;
    
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
        mapTerritoireJoueur = new HashMap<>();

        //AgentServicesTools.register(this, "intermediaire", "link");

        TopicManagementHelper topicHelper = null;
        try {
            topicHelper =  ( TopicManagementHelper ) getHelper (TopicManagementHelper.SERVICE_NAME) ;
            topicRepartition = topicHelper.createTopic("REPARTITION REGIMENT");
            topicAutorisationUpdateRegimentTerritoireAdjacent = topicHelper.createTopic("Update Regiment Territoire Adjacent");
            topicUpdateRegimentAdjacent = topicHelper.createTopic("UPDATE REGIMENT ADJACENT");
            topicAffichageFinTour = topicHelper.createTopic("AFFICHAGE FIN TOUR");
            //topicTerritoireRetour = topicHelper.createTopic("RETOUR INFO TERRITOIRE");
            //topicHelper.register(topicTerritoireRetour);
            topicHelper.register(topicRepartition);
            topicHelper.register(topicAutorisationUpdateRegimentTerritoireAdjacent);
            topicHelper.register(topicUpdateRegimentAdjacent);
            topicHelper.register(topicAffichageFinTour);
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
            
            //Attribution de l'adresse du joueur au territoire
            mapTerritoireJoueur.put(tempT.getNomTerritoire(),m.getSender());
        }));

        
        //init du model
        var model0 = MessageTemplate.MatchConversationId("update regiment territoire adjacent");
        
        //Reception des info du territoire et stockage, fonction ne captant que les messages du model cr�er precedemment
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
        
        var model1 = MessageTemplate.MatchConversationId("autorisation debut partie");
        
        //Reception des autorisation de cmmencement de la phase de combat, des que tous les joueurs sont pret (fin phase de renfort), on peut lancer
        addBehaviour(new MsgReceiver(this,model1,MsgReceiver.INFINITE,null,null){
        	protected void handleMessage(ACLMessage msg) {
        		window.println("\nReception autorisation debut de partie de " + msg.getSender().getLocalName() + "\n");
        		nbAutorisation += 1;
        		if(nbAutorisation == 6)
        		{
        			nbAutorisation = 0; // reset pour la prochaine phase
        			debutPartie(); //debut partie
        		}
        		reset(model1,MsgReceiver.INFINITE,null,null);
        	}
        });
        
        //init du model
        var model2 = MessageTemplate.MatchConversationId("fin tour joueur");
        
        //Reception des notifications de fin de tour de combat des joueurs, pour permettre au prochain de commencer sa phase de combat
        addBehaviour(new MsgReceiver(this,model2,MsgReceiver.INFINITE,null,null){
        	protected void handleMessage(ACLMessage msg) {
        		
        		//update de plateau si manoeuvre
        		if(msg.getContent() != null)
        		{
        			window.println("Bonne reception de la manoeuvre");
        			
        			var infos = msg.getContent().split(",");
        			
        			plateau.getTerritoireByName(infos[0]).setRegimentSurTerritoire(Integer.parseInt(infos[1]));
            		updatePlateauRegimentTerrAdj(infos[0],Integer.parseInt(infos[1]));
            		
            		plateau.getTerritoireByName(infos[2]).setRegimentSurTerritoire(Integer.parseInt(infos[3]));
            		updatePlateauRegimentTerrAdj(infos[2],Integer.parseInt(infos[3]));
        		}
        		
    			if(iJoueurTourCombat < 6) // alors tous les joueurs n'ont pas joue
    			{
    				iJoueurTourCombat++; // pour passer au joueur suivant
    				debutPartie(); // nouveau tour pour ce nouveau joueur
    			}
    			else 
    			{
    				window.println("fin 1er tour");; // tous le monde a fait sa phase de combat, DEBUT PHASE MANOEUVRE
    				
    				ACLMessage assignRegiment = new ACLMessage(ACLMessage.INFORM);
    		        assignRegiment.addReceiver(topicAffichageFinTour);
    		        send(assignRegiment);
    		        
    		        window.println("\n" + plateau.toString());
    			}
        		reset(model2,MsgReceiver.INFINITE,null,null);
        	}
        });
        
        //init du model
        var model3 = MessageTemplate.MatchConversationId("lancement attaque");
        
        //Reception des info du territoire et stockage, fonction ne captant que les messages du model cr�er precedemment
        addBehaviour(new MsgReceiver(this,model3,MsgReceiver.INFINITE,null,null){
        	@SuppressWarnings("null")
			protected void handleMessage(ACLMessage msg) {
        		if(msg!=null)
        		{
        			var infos = msg.getContent().split(",");
                    
                    window.println("Message recu sur le model " + model3.toString() + ". Contenu " + msg.getContent().toString()
                    + " emis par :  " + msg.getSender().getLocalName());
                    
                    String nomTerritoireAttaque = infos[0], nomTerritoireDefense = infos[1];
                	int nbRegimentAttaquant = Integer.parseInt(infos[2]), nbRegimentDefenseur = Integer.parseInt(infos[3]);
                	
                	// savoir combien de des ils peuvent lances
                	int nbDesAttaquant = nbDes("attaquant",nbRegimentAttaquant);
                	int nbDesDefenseur = nbDes("defenseur",nbRegimentDefenseur);
                	
                	// resultat lancement
                	Random rand = new Random();
                	List<Integer> resultatsAtt = new ArrayList<>();
                	List<Integer> resultatsDef = new ArrayList<>();
                	int i;
                	for(i = 0; i < nbDesAttaquant; i++) resultatsAtt.add(rand.nextInt(6) + 1); // random entre 1 et 6
                	for(i = 0; i < nbDesDefenseur; i++) resultatsDef.add(rand.nextInt(6) + 1); // random entre 1 et 6
                	// trie decroissant
                	Collections.sort(resultatsAtt,Collections.reverseOrder());
                	Collections.sort(resultatsDef,Collections.reverseOrder());
                	// confrontation lancement
                	int nbConfrontation, nbRegimentAttaquantUpdate = nbRegimentAttaquant, nbRegimentDefenseurUpdate = nbRegimentDefenseur;
                	if(resultatsAtt.size() >= 2 && resultatsDef.size() >= 2) nbConfrontation = 2; // car nbDes max de defenseur = 2, donc max 2 comparaisons
                	else nbConfrontation = 1;
                	for(i=0;i<nbConfrontation;i++) 
                	{
                		if(resultatsAtt.get(i) > resultatsDef.get(i)) nbRegimentDefenseurUpdate--;
                		else nbRegimentAttaquantUpdate--;
                	}
                	
                	
                	
                	//Retour des resultats pour l attaquant
                	ACLMessage message1 = new ACLMessage(ACLMessage.REQUEST);
    				message1.setConversationId("retour resultat attaque");
    				message1.addReceiver(new AID(msg.getSender().getLocalName(), AID.ISLOCALNAME));
    				message1.setEncoding(nomTerritoireAttaque+","+nomTerritoireDefense+","+nbRegimentAttaquant+","+nbRegimentAttaquantUpdate+","+nbRegimentDefenseur+","+nbRegimentDefenseurUpdate+","+msg.getSender().getLocalName()+","+joueurs.size());
    				
                	//Retour des resultats pour le defenseur
    				ACLMessage message2 = new ACLMessage(ACLMessage.REQUEST);
    				message2.setConversationId("retour resultat defense");
    				message2.addReceiver(new AID(mapTerritoireJoueur.get(nomTerritoireDefense).getLocalName(), AID.ISLOCALNAME));
    				message2.setContent(nomTerritoireAttaque+","+nomTerritoireDefense+","+nbRegimentAttaquant+","+nbRegimentAttaquantUpdate+","+nbRegimentDefenseur+","+nbRegimentDefenseurUpdate+","+msg.getSender().getLocalName());
    				
    				
    				if(nbRegimentDefenseurUpdate == 0) //alors attribution du territoire a l'attaquant
    				{
    					//changement dans la map
    					mapTerritoireJoueur.remove(nomTerritoireDefense);
    					mapTerritoireJoueur.put(nomTerritoireDefense, msg.getSender());
    					
    					//update du plateau (nouveau territoire donc forcement update pour territoire Att et Def
                    	//attaque
        				plateau.getTerritoireByName(nomTerritoireAttaque).setRegimentSurTerritoire(plateau.getTerritoireByName(nomTerritoireAttaque).getRegimentSurTerritoire() - nbRegimentAttaquant);
                    	updatePlateauRegimentTerrAdj(nomTerritoireAttaque,plateau.getTerritoireByName(nomTerritoireAttaque).getRegimentSurTerritoire()); //update territoires adjacents du plateau si contient un territoire ayant change
                    	//defense
    	                plateau.getTerritoireByName(nomTerritoireDefense).setRegimentSurTerritoire(nbRegimentAttaquantUpdate); // les regiments qui ont attaques restent sur le nouveau territoire
                    	updatePlateauRegimentTerrAdj(nomTerritoireDefense,nbRegimentAttaquantUpdate);
                    	
                    	
    					//envoie du territoire
    					try {
							message1.setContentObject(plateau.getTerritoireByName(nomTerritoireDefense));
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
    				}
    				else
    				{
    					//update du plateau
                    	if(nbRegimentAttaquant > nbRegimentAttaquantUpdate) 
                    	{
                    		int regimentsAttRestant = plateau.getTerritoireByName(nomTerritoireAttaque).getRegimentSurTerritoire() - (nbRegimentAttaquant - nbRegimentAttaquantUpdate);
                    		plateau.getTerritoireByName(nomTerritoireAttaque).setRegimentSurTerritoire(regimentsAttRestant);
                    		updatePlateauRegimentTerrAdj(nomTerritoireAttaque,regimentsAttRestant); //update territoires adjacents du plateau si contient un territoire ayant change
                    	}
                    	if(nbRegimentDefenseur > nbRegimentDefenseurUpdate) 
                    	{
                    		plateau.getTerritoireByName(nomTerritoireDefense).setRegimentSurTerritoire(nbRegimentDefenseurUpdate);
                    		updatePlateauRegimentTerrAdj(nomTerritoireDefense,nbRegimentDefenseurUpdate);
                    	}
    				}
    				
    				send(message1);
    				send(message2);
    				
    				//MESSAGE TOPIC A TOUS LES JOUEURS POUR NOTIFIE DES MODIFS DE REGIMENT
    				//topic territoire attaque
					window.println("\nEnvoie topic update regiment adjacent du territoire attaquant");
			    	ACLMessage topic1 = new ACLMessage(ACLMessage.INFORM);
			    	topic1.addReceiver(topicUpdateRegimentAdjacent);
			    	topic1.setContent(nomTerritoireAttaque+","+plateau.getTerritoireByName(nomTerritoireAttaque).getRegimentSurTerritoire()+","+msg.getSender().getLocalName());
			        
			        //topic territoire defense
					window.println("\nEnvoie topic update regiment adjacent du territoire defense");
			    	ACLMessage topic2 = new ACLMessage(ACLMessage.INFORM);
			    	topic2.addReceiver(topicUpdateRegimentAdjacent);
			    	topic2.setContent(nomTerritoireDefense+","+plateau.getTerritoireByName(nomTerritoireDefense).getRegimentSurTerritoire()+","+msg.getSender().getLocalName()); //var2 = nouveu nombre de regiment a set

    				if(nbRegimentDefenseurUpdate == 0) // alors forcement on envoie les 2 topic car ajout et retrait (attribution nouveau territoire)
    				{
    					send(topic1);
    					send(topic2);
    				}
    				else
    				{
    					if(nbRegimentAttaquant > nbRegimentAttaquantUpdate)
    					{
        			        send(topic1);
    					}
    					
    			        if(nbRegimentDefenseur > nbRegimentDefenseurUpdate)
    			        {
    			        	send(topic2);
    			        }
    				}
    				
        		}
        		reset(model3,MsgReceiver.INFINITE,null,null);
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
    private void debutPartie()
    {
    	window.println("\nDebut tour");
    	String tourJoueur = "Joueur_"+iJoueurTourCombat; // iTourCombat variable globale
    	//window.println("tourJoueur = "+tourJoueur);
		int iJoueur = 0;
		while(!joueurs.get(iJoueur).getLocalName().toString().equals(tourJoueur)) // recherche du Joueur_iTourCombat (Joueur_1 -> Joueur_2 -> Joueur_3 -> ...)
		{
			//window.println(joueurs.get(iJoueur).getLocalName().toString() + " != " + tourJoueur);
			iJoueur++;
		}
		window.println("\nEnvoie autorisation commencement du tour de " + tourJoueur);
		
		//Envoie du message pour que le joueur commence son tour
    	ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
		message.setConversationId("debut tour");
		message.addReceiver(new AID(joueurs.get(iJoueur).getLocalName(), AID.ISLOCALNAME));
		send(message);
    }
    
    private int nbDes (String attDef, int nbRegiment)
    {
    	if(attDef.equals("attaquant"))
    	{
    		if(nbRegiment == 1) return 1;
    		if(nbRegiment == 2) return 2;
    		else return 3;
    	}
    	else // defenseur
    	{
    		if(nbRegiment == 1 || nbRegiment == 2) return 1;
    		else return 2;
    	}
    }
    
    /*
     * update territoires adjacents du plateau si contient un territoire ayant change
     */
    private void updatePlateauRegimentTerrAdj (String nomTerritoire, int nbRegimentUpdate)
    {
    	int i,j,k;
    	for(i = 0; i < this.plateau.getContinents().size(); i++) // parcours des territoires
    	{
    		for(j = 0; j < this.plateau.getContinents().get(i).getTerritoires().size(); j++) // parcours de tous les territoires adjacents
    		{
    			for(k = 0; k < this.plateau.getContinents().get(i).getTerritoires().get(j).getTerritoires_adjacents().size(); k++) // parcours de tous les territoires adjacents
        		{
	    			if(this.plateau.getContinents().get(i).getTerritoires().get(j).getTerritoires_adjacents().get(k).getNomTerritoire().equals(nomTerritoire))
	    			{
	    				this.plateau.getContinents().get(i).getTerritoires().get(j).getTerritoires_adjacents().get(k).setRegimentSurTerritoire(nbRegimentUpdate);
	    			}
        		}
    		}
    		
    	}
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
