package agents;

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

import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.*;

public class Intermediaire extends GuiAgent {

	private gui.IntermediaireGui window;

	private Monde plateau;
	public static final int EXIT = 0;

	public static final int LANCER_RISK = 1;

	public static final int LANCER_TOUR_RISK = 2;

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
	private Map<String, AID> mapTerritoireJoueur;

	//variable pour lancer updateRegimentTerritoireAjdacent
	private int nbTerritoireUpdate = 0;

	//variable pour gerer qui joue en phase de combat
	private int iJoueurTourCombat = 0;

	//numero du tour
	private int numTour = 1;

	@SuppressWarnings({"deprecation", "serial"})
	@Override
	protected void setup() {
		window = new gui.IntermediaireGui(this);
		window.display();
		window.setColor(Color.LIGHT_GRAY);
		window.println("Hello! Agent  " + getLocalName() + " is ready, my address is " + this.getAID().getName());

		detectJoueurs();

		plateau = new Monde();
		mapTerritoireJoueur = new HashMap<>();

		//AgentServicesTools.register(this, "intermediaire", "link");

		TopicManagementHelper topicHelper;
		try {
			topicHelper = (TopicManagementHelper) getHelper(TopicManagementHelper.SERVICE_NAME);
			topicRepartition = topicHelper.createTopic("REPARTITION REGIMENT");
			topicAutorisationUpdateRegimentTerritoireAdjacent = topicHelper.createTopic("Update Regiment Territoire Adjacent");
			topicUpdateRegimentAdjacent = topicHelper.createTopic("UPDATE REGIMENT ADJACENT");
			topicAffichageFinTour = topicHelper.createTopic("AFFICHAGE FIN TOUR");
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
		addBehaviour(new ReceiverBehaviour(this, -1, MessageTemplate.MatchTopic(topicTerritoire), true, (a, m) -> {
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
			mapTerritoireJoueur.put(tempT.getNomTerritoire(), m.getSender());
		}));


		//init du model
		var model0 = MessageTemplate.MatchConversationId("update regiment territoire adjacent");

		//Reception des info du territoire et stockage, fonction ne captant que les messages du model créer precedemment
		addBehaviour(new MsgReceiver(this, model0, MsgReceiver.INFINITE, null, null) {
			protected void handleMessage(ACLMessage msg) {
				if (msg != null) {
					//Reception message
					var infos = msg.getContent().split(",");

					window.println("\nMessage recu sur le model " + model0 + ". Contenu " + msg.getContent()
							+ " emis par :  " + msg.getSender().getLocalName());

					//Recherche territoire voulu du plateau
					int nbRegiment = plateau.getTerritoireByName(infos[0]).getRegimentSurTerritoire();

					//Renvoie de l'info
					ACLMessage retour = msg.createReply();

					//init du model
					retour.setConversationId("retour update regiment territoire adjacent");
					retour.setContent(msg.getContent() + "," + nbRegiment);
					send(retour);

					//territoires.add(tempT);

					window.println("Renvoie du territoire avec le bon nombre de regiment = " + nbRegiment);

				}
				reset(model0, MsgReceiver.INFINITE, null, null);
			}
		});


		addBehaviour(new ReceiverBehaviour(this, -1, MessageTemplate.MatchTopic(topicRegimentTeritoire), true, (a, m) -> {
			Territoire tempT = null;
			try {
				tempT = (Territoire) m.getContentObject();
			} catch (UnreadableException e) {
				//throw new RuntimeException(e);
			}

			window.println("Message recu sur le topic " + topicRegimentTeritoire.getLocalName() + ". Pour mettre à jour les informations du territoire " + tempT.getNomTerritoire()
			+ " emis par :  " + m.getSender().getLocalName());
			
			assert tempT != null;
			plateau.getTerritoireByName(tempT.getNomTerritoire()).setRegimentSurTerritoire(tempT.getRegimentSurTerritoire());
			//window.println(plateau.toString());

			nbTerritoireUpdate += 1;

			if (m.getEncoding() != null) {
				if (nbTerritoireUpdate >= Integer.parseInt(m.getEncoding())) {
					nbTerritoireUpdate = 0;
					autorisationCombat(m.getSender().getLocalName());
				}
			} else {
				if (nbTerritoireUpdate == 42) {
					nbTerritoireUpdate = 0;
					autorisationRegimentTerritoireAdjacent();
				}
			}

		}));


		addBehaviour(new ReceiverBehaviour(this, -1, MessageTemplate.MatchTopic(topicUpdateContinent), true, (a, m) -> {
			//update de plateau si manoeuvre
			if (m.getEncoding() != null) {
				String temp = m.getEncoding();
				if (temp != null) {
					window.println("Bonne reception de la manoeuvre");

					var infos = m.getEncoding().split(",");

					plateau.getTerritoireByName(infos[0]).setRegimentSurTerritoire(Integer.parseInt(infos[1]));
					updatePlateauRegimentTerrAdj(infos[0], Integer.parseInt(infos[1]));

					plateau.getTerritoireByName(infos[2]).setRegimentSurTerritoire(Integer.parseInt(infos[3]));
					updatePlateauRegimentTerrAdj(infos[2], Integer.parseInt(infos[3]));
				}
			}


			List<Territoire> tempT = new ArrayList<>();
			try {
				tempT.addAll((List<Territoire>) m.getContentObject());
			} catch (UnreadableException e) {
				//throw new RuntimeException(e);
			}
			StringBuilder continentPossede = new StringBuilder();
			for (Continent c : plateau.getContinents()) {
				int nbTerritoire = 0;
				for (Territoire t : c.getTerritoires()) {
					for (Territoire ter : tempT) {
						if (ter.getNomTerritoire().equals(t.getNomTerritoire())) {
							nbTerritoire++;
						}
					}
				}
				if (nbTerritoire == c.getTerritoires().size()) {
					if (!continentPossede.isEmpty())
						continentPossede.append(',');
					continentPossede.append(c.getNom());
				}
			}

			ACLMessage retour = m.createReply();

			//init du model
			retour.setConversationId("send update continent");
			retour.setContent(continentPossede.toString());

			send(retour);

			//window.println(plateau.toString());
		}));

		var model1 = MessageTemplate.MatchConversationId("autorisation debut partie");

		//Reception des autorisation de cmmencement de la phase de combat, des que tous les joueurs sont pret (fin phase de renfort), on peut lancer
		addBehaviour(new MsgReceiver(this, model1, MsgReceiver.INFINITE, null, null) {
			protected void handleMessage(ACLMessage msg) {
				window.println("\nReception autorisation debut de partie de " + msg.getSender().getLocalName() + "\n");
				nbAutorisation += 1;
				if (nbAutorisation == joueurs.size()) {
					nbAutorisation = 0; // reset pour la prochaine phase
					debutPartie(); //debut partie
				}
				reset(model1, MsgReceiver.INFINITE, null, null);
			}
		});

		//init du model
		var model2 = MessageTemplate.MatchConversationId("fin tour joueur");

		//Reception des notifications de fin de tour de combat des joueurs, pour permettre au prochain de commencer sa phase de combat
		addBehaviour(new MsgReceiver(this, model2, MsgReceiver.INFINITE, null, null) {
			protected void handleMessage(ACLMessage msg) {


				if (iJoueurTourCombat < joueurs.size() - 1) // alors tous les joueurs n'ont pas joue
				{
					iJoueurTourCombat++; // pour passer au joueur suivant
					window.println("\niJoueurTourCombat = " + iJoueurTourCombat);
					debutPartie(); // nouveau tour pour ce nouveau joueur
				} else {
					window.println("fin du tour " + numTour + " dont le dernier est " + msg.getSender().getLocalName());
					// tous le monde a fait sa phase de combat, DEBUT PHASE MANOEUVRE

					ACLMessage AffichageFinTour = new ACLMessage(ACLMessage.INFORM);
					AffichageFinTour.addReceiver(topicAffichageFinTour);
					send(AffichageFinTour);

					window.println("\n" + plateau.toString());

					numTour++;
					iJoueurTourCombat = 0;
					//if (numTour < 10)
					debutPartie();  // A METTRE EN COMMENTAIRE SI ON NE VEUT PLUS LOOP (si on veut ne faire que 1 seul tour)
				}
				reset(model2, MsgReceiver.INFINITE, null, null);
			}
		});

		//init du model
		var model3 = MessageTemplate.MatchConversationId("lancement attaque");

		//Reception des info du territoire et stockage, fonction ne captant que les messages du model créer precedemment
		addBehaviour(new MsgReceiver(this, model3, MsgReceiver.INFINITE, null, null) {
			@SuppressWarnings("null")
			protected void handleMessage(ACLMessage msg) {
				if (msg != null) {
					var infos = msg.getContent().split(",");

					window.println("Message recu sur le model " + model3 + ". Contenu " + msg.getContent()
							+ " emis par :  " + msg.getSender().getLocalName());

					String nomTerritoireAttaque = infos[0], nomTerritoireDefense = infos[1];
					int nbRegimentAttaquant = Integer.parseInt(infos[2]), nbRegimentDefenseur = Integer.parseInt(infos[3]);

					int nbRegimentAttaquantUpdate = nbRegimentAttaquant, nbRegimentDefenseurUpdate = nbRegimentDefenseur;
					window.println("\n");

					while (nbRegimentAttaquantUpdate > 0 && nbRegimentDefenseurUpdate > 0) {
						StringBuilder affichage = new StringBuilder(nomTerritoireAttaque + " possede " + nbRegimentAttaquantUpdate + " regiment(s).\n"
								+ nomTerritoireDefense + " possede " + nbRegimentDefenseurUpdate + " regiment(s).\nDebut du combat.");
						affichage.append("\n\tLancer de des");
						// savoir combien de des ils peuvent lances
						int nbDesAttaquant = nbDes("attaquant", nbRegimentAttaquantUpdate);
						int nbDesDefenseur = nbDes("defenseur", nbRegimentDefenseurUpdate);
						affichage.append("\n\tL attaquant lance ").append(nbDesAttaquant).append(" des. Le defenseur lance ").append(nbDesDefenseur).append(" des.");

						// resultat lancement
						Random rand = new Random();
						List<Integer> resultatsAtt = new ArrayList<>();
						List<Integer> resultatsDef = new ArrayList<>();
						int i;
						for (i = 0; i < nbDesAttaquant; i++)
							resultatsAtt.add(rand.nextInt(6) + 1); // random entre 1 et 6
						for (i = 0; i < nbDesDefenseur; i++)
							resultatsDef.add(rand.nextInt(6) + 1); // random entre 1 et 6
						// trie decroissant
						resultatsAtt.sort(Collections.reverseOrder());
						resultatsDef.sort(Collections.reverseOrder());
						affichage.append("\n\tResultat des lancer de l'attaquant : ").append(resultatsAtt);
						affichage.append("\n\tResultat des lancer du defenseur : ").append(resultatsDef);
						// confrontation lancement
						int nbConfrontation;
						if (resultatsAtt.size() >= 2 && resultatsDef.size() >= 2)
							nbConfrontation = 2; // car nbDes max de defenseur = 2, donc max 2 comparaisons
						else nbConfrontation = 1;
						for (i = 0; i < nbConfrontation; i++) {
							if (resultatsAtt.get(i) > resultatsDef.get(i)) {
								nbRegimentDefenseurUpdate--;
								affichage.append("\n\t").append(resultatsAtt.get(i)).append(" > ").append(resultatsDef.get(i)).append(", le defenseur perd 1 unite.");
							} else {
								nbRegimentAttaquantUpdate--;
								affichage.append("\n\t").append(resultatsAtt.get(i)).append(" <= ").append(resultatsDef.get(i)).append(", l attaquant perd 1 unite.");
							}
						}
						window.println(affichage.toString());
					}
					if (nbRegimentAttaquantUpdate == 0)
						window.println("L attaquant ne possede plus d unite. Victoire du defenseur " + nomTerritoireDefense);
					else
						window.println("Le defenseur ne possede plus d unite. Victoire de l'attaquant " + nomTerritoireAttaque);


					//Retour des resultats pour l attaquant
					ACLMessage message1 = new ACLMessage(ACLMessage.REQUEST);
					message1.setConversationId("retour resultat attaque");
					message1.addReceiver(new AID(msg.getSender().getLocalName(), AID.ISLOCALNAME));
					message1.setEncoding(nomTerritoireAttaque + "," + nomTerritoireDefense + "," + nbRegimentAttaquant + "," + nbRegimentAttaquantUpdate + "," + nbRegimentDefenseur + "," + nbRegimentDefenseurUpdate + "," + msg.getSender().getLocalName() + "," + joueurs.size());

					//Retour des resultats pour le defenseur
					ACLMessage message2 = new ACLMessage(ACLMessage.REQUEST);
					message2.setConversationId("retour resultat defense");
					message2.addReceiver(new AID(mapTerritoireJoueur.get(nomTerritoireDefense).getLocalName(), AID.ISLOCALNAME));
					message2.setContent(nomTerritoireAttaque + "," + nomTerritoireDefense + "," + nbRegimentAttaquant + "," + nbRegimentAttaquantUpdate + "," + nbRegimentDefenseur + "," + nbRegimentDefenseurUpdate + "," + msg.getSender().getLocalName());


					if (nbRegimentDefenseurUpdate == 0) //alors attribution du territoire a l'attaquant
					{
						//changement dans la map
						mapTerritoireJoueur.remove(nomTerritoireDefense);
						mapTerritoireJoueur.put(nomTerritoireDefense, msg.getSender());

						//update du plateau (nouveau territoire donc forcement update pour territoire Att et Def
						//attaque
						plateau.getTerritoireByName(nomTerritoireAttaque).setRegimentSurTerritoire(plateau.getTerritoireByName(nomTerritoireAttaque).getRegimentSurTerritoire() - nbRegimentAttaquant);
						updatePlateauRegimentTerrAdj(nomTerritoireAttaque, plateau.getTerritoireByName(nomTerritoireAttaque).getRegimentSurTerritoire()); //update territoires adjacents du plateau si contient un territoire ayant change
						//defense
						plateau.getTerritoireByName(nomTerritoireDefense).setRegimentSurTerritoire(nbRegimentAttaquantUpdate); // les regiments qui ont attaques restent sur le nouveau territoire
						updatePlateauRegimentTerrAdj(nomTerritoireDefense, nbRegimentAttaquantUpdate);


						//envoie du territoire
						try {
							message1.setContentObject(plateau.getTerritoireByName(nomTerritoireDefense));
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} else {
						//update du plateau
						if (nbRegimentAttaquant > nbRegimentAttaquantUpdate) {
							int regimentsAttRestant = plateau.getTerritoireByName(nomTerritoireAttaque).getRegimentSurTerritoire() - (nbRegimentAttaquant - nbRegimentAttaquantUpdate);
							plateau.getTerritoireByName(nomTerritoireAttaque).setRegimentSurTerritoire(regimentsAttRestant);
							updatePlateauRegimentTerrAdj(nomTerritoireAttaque, regimentsAttRestant); //update territoires adjacents du plateau si contient un territoire ayant change
						}
						if (nbRegimentDefenseur > nbRegimentDefenseurUpdate) {
							plateau.getTerritoireByName(nomTerritoireDefense).setRegimentSurTerritoire(nbRegimentDefenseurUpdate);
							updatePlateauRegimentTerrAdj(nomTerritoireDefense, nbRegimentDefenseurUpdate);
						}
					}

					send(message1);
					send(message2);

					//MESSAGE TOPIC A TOUS LES JOUEURS POUR NOTIFIE DES MODIFS DE REGIMENT
					//topic territoire attaque
					window.println("\nEnvoie topic update regiment adjacent du territoire attaquant");
					ACLMessage topic1 = new ACLMessage(ACLMessage.INFORM);
					topic1.addReceiver(topicUpdateRegimentAdjacent);
					topic1.setContent(nomTerritoireAttaque + "," + plateau.getTerritoireByName(nomTerritoireAttaque).getRegimentSurTerritoire() + "," + msg.getSender().getLocalName());

					//topic territoire defense
					window.println("\nEnvoie topic update regiment adjacent du territoire defense");
					ACLMessage topic2 = new ACLMessage(ACLMessage.INFORM);
					topic2.addReceiver(topicUpdateRegimentAdjacent);
					topic2.setContent(nomTerritoireDefense + "," + plateau.getTerritoireByName(nomTerritoireDefense).getRegimentSurTerritoire() + "," + msg.getSender().getLocalName()); //var2 = nouveu nombre de regiment a set

					if (nbRegimentDefenseurUpdate == 0) // alors forcement on envoie les 2 topic car ajout et retrait (attribution nouveau territoire)
					{
						send(topic1);
						send(topic2);
					} else {
						if (nbRegimentAttaquant > nbRegimentAttaquantUpdate) {
							send(topic1);
						}

						if (nbRegimentDefenseur > nbRegimentDefenseurUpdate) {
							send(topic2);
						}
					}

				}
				reset(model3, MsgReceiver.INFINITE, null, null);
			}
		});
		
		var model4 = MessageTemplate.MatchConversationId("victoire / fin de partie");


        addBehaviour(new MsgReceiver(this, model4, MsgReceiver.INFINITE, null, null) {
            protected void handleMessage(ACLMessage msg) {
                if (msg != null) {
                	window.println(plateau.toString());
                	
                    window.println("\nMessage recu sur le model " + model4 + " emis par :  " + msg.getSender().getLocalName());

                    String affichage = msg.getContent();
                    
                    window.println("Fin de la partie. " + affichage);
                    
                    ACLMessage finPartie = new ACLMessage(ACLMessage.INFORM);
                    finPartie.addReceiver(topicAffichageFinTour);
					finPartie.setEncoding(affichage);
					send(finPartie);

                }
                reset(model4, MsgReceiver.INFINITE, null, null);
            }
        });


	}

	/**
	 * ecoute des evenement de type enregistrement en tant que joueur, pour avoir acces a leurs adresses
	 */
	private void detectJoueurs() {
		var model = AgentServicesTools.createAgentDescription("liste joueur", "get AID joueur");
		if(this.joueurs == null)
			this.joueurs = new ArrayList<>();
		else
			this.joueurs.clear();

		//souscription au service des pages jaunes pour recevoir une alerte en cas mouvement sur le service travel agency'seller
		addBehaviour(new DFSubscriber(this, model) {
			@Override
			public void onRegister(DFAgentDescription dfd) { //au debut
				joueurs.add(dfd.getName());
				//System.out.println("Liste de joueurs AID" + joueurs);
				window.println(dfd.getName().getLocalName() + " s'est inscrit en tant que joueur : " + model.getAllServices().get(0));
			}

			@Override
			public void onDeregister(DFAgentDescription dfd) { // lorsque le joueur est mort
				joueurs.remove(dfd.getName());
				window.println(dfd.getName().getLocalName() + " s'est desinscrit de  : " + model.getAllServices().get(0));
				var infos = dfd.getName().getLocalName().split("_");
				if(Integer.parseInt(infos[1]) <= iJoueurTourCombat) iJoueurTourCombat--;
			}
		});
		//System.out.println("Liste de joueurs" + joueurs);

	}

	private void autorisationRegimentTerritoireAdjacent() {
		window.println("\nEnvoie autorisation commencement Regiment Territoire Adjacent aux joueurs.");
		ACLMessage assignRegiment = new ACLMessage(ACLMessage.INFORM);
		assignRegiment.addReceiver(topicAutorisationUpdateRegimentTerritoireAdjacent);
		send(assignRegiment);
	}

	/*
	 * Notification au Joueur_iTourCombat qu'il peut commencer sa phase d'attaque
	 */
	private void debutPartie() {
		window.println("\nDebut du tour " + numTour);
		window.println("\nEnvoie autorisation commencement du tour de " + joueurs.get(iJoueurTourCombat).getLocalName());

		//Envoie du message pour que le joueur commence son tour
		ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
		message.setConversationId("debut tour");
		message.addReceiver(new AID(joueurs.get(iJoueurTourCombat).getLocalName(), AID.ISLOCALNAME));
		send(message);
	}

	private void autorisationCombat(String nomJoueur) {
		window.println("\nEnvoie autorisation commencement de la phase de combat de " + joueurs.get(iJoueurTourCombat).getLocalName());

		//Envoie du message pour que le joueur commence sa phase de combat
		ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
		message.setConversationId("autorisation combat");
		message.addReceiver(new AID(nomJoueur, AID.ISLOCALNAME));
		message.setContent("autorisation intermediaire");
		send(message);
	}

	private int nbDes(String attDef, int nbRegiment) {
		if (attDef.equals("attaquant")) {
			if (nbRegiment == 1) return 1;
			if (nbRegiment == 2) return 2;
			else return 3;
		} else // defenseur
		{
			if (nbRegiment == 1 || nbRegiment == 2) return 1;
			else return 2;
		}
	}

	/*
	 * update territoires adjacents du plateau si contient un territoire ayant change
	 */
	private void updatePlateauRegimentTerrAdj(String nomTerritoire, int nbRegimentUpdate) {
		int i, j, k;
		for (i = 0; i < this.plateau.getContinents().size(); i++) // parcours des territoires
		{
			for (j = 0; j < this.plateau.getContinents().get(i).getTerritoires().size(); j++) // parcours de tous les territoires adjacents
			{
				for (k = 0; k < this.plateau.getContinents().get(i).getTerritoires().get(j).getTerritoires_adjacents().size(); k++) // parcours de tous les territoires adjacents
				{
					if (this.plateau.getContinents().get(i).getTerritoires().get(j).getTerritoires_adjacents().get(k).getNomTerritoire().equals(nomTerritoire)) {
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
		} else if (guiEvent.getType() == Intermediaire.LANCER_RISK) {

			//trie des joueurs par leur nom (Joueur_1 -> Joueur_6)
			Comparator<AID> joueurComparator
					= Comparator.comparing(AID::getLocalName);
			joueurs.sort(joueurComparator);

			System.out.println(joueurs);

			launchRisk();
		} else if (guiEvent.getType() == Intermediaire.LANCER_TOUR_RISK) {
			//trie des joueurs par leur nom (Joueur_1 -> Joueur_6)
			Comparator<AID> joueurComparator
					= Comparator.comparing(AID::getLocalName);
			joueurs.sort(joueurComparator);
			debutPartie();
		}
	}

	public void launchRisk() {
		window.println("Debut de la partie");

		ACLMessage assignRegiment = new ACLMessage(ACLMessage.INFORM);
		assignRegiment.addReceiver(topicRepartition);
		send(assignRegiment);


	}
}
