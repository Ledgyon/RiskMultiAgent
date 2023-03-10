package agents;

import carte.CarteMission;
import carte.CartePioche;
import carte.enumerations.TypeMission;
import jade.core.AID;
import jade.core.AgentServicesTools;
import jade.core.ServiceException;
import jade.core.behaviours.ReceiverBehaviour;
import jade.core.messaging.TopicManagementHelper;
import jade.domain.DFService;
import jade.domain.DFSubscriber;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAException;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.proto.states.MsgReceiver;
import plateau.Territoire;

import java.awt.*;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.*;

@SuppressWarnings("serial")
public class Joueur extends GuiAgent {
	
    private String couleur;
    private int nombreRegimentAPlacer;
    private int nombreRegimentMax;
    private String strategie;   // parametre qui va definir la strategie que l'IA va utiliser
    private List<CartePioche> main; // cartes que le joueur possede dans sa main (max 5)
    private CarteMission objectif; // objectif du joueur pour remporter la partie
    private boolean debutPartie = false;

    private List<Territoire> territoires; // territoires possedes par le joueur
    private List<Integer> territoiresPouvantAttaquer; //indices de la liste "territoires" des territoires n'ayant pas encore attaque au tour actuel
    private List<String> continents; // permet de savoir quel continent le joueur a conquis pour l'attribution des renforts et pour les objectifs
    private List<String> armees_eliminees; // liste des couleurs des armees que le joueur a eliminee (les adversaires eliminees, utile si objectif est d'eliminer un joueur precis)
    public static final int EXIT = 0;

    /*
     * VARIABLES controlant la bonne reception des messages permettant l'autorisation de lancer la phase de combat
     */
    private boolean autorIntermediaire = false;
    private int nbAutorJoueurRecquis = 500; // set a une valeur trop elevee expres pour ne pas cut la validation des autorisations
    private int nbAutorJoueur = 0;

    /*
     * VARIABLES controlant la bonne reception des messages permettant l'autorisation de lancer une nouvelle attaque
     */
    private boolean autorAtt = false;
    private boolean autorDef = false;
    private boolean autorTopicTerrAdj = false;
    private int nbAutorTopicTerrAdjRecquis = 20; // set a une valeur trop elevee expres pour ne pas cut la validation des autorisation
    private int nbAutorTopicTerrAdj = 0;
    private int nbTopicElim = 0; // nombre de topic elim recu, remplace autorDef lorsque le joueur Defense vient de se faire eliminee
    //bonus boolean pour ne piocher que 1 carte
    private boolean territoireConcquis = false;
    private List<Territoire> attaqueRevanche;

    private AID intermediaire;
    private AID general;
    /**
     * liste des joueurs
     */
    private ArrayList<AID> joueurs;
    /**
     * topic du joueur demandant les informations du territoire
     */
    AID topicTerritoire;
    /**
     * topic du joueur demandant de mettre ? jour les r?giments sur le plateau
     */
    AID topicRegimentTeritoire;
    /*
     * topic du joueur envoyant ? tous les joueurs l'update des regiments apres phase de renfort pour qu ils update leurs territoires adjacents
     */
    AID topicRegimentTerritoireAdjacentPourJoueursPhaseRenfort;
    /**
     * topic du joueur permettant de faire la repartition des regiments sur les territoires du joueur en debut de partie
     */
    AID topicRepartition;
    /**
     * topic du joueur permettant de mettre ? jour les continents possedes
     */
    AID topicUpdateContinent;
    /*
     * OUAIS
     */
    AID topicAutorisationUpdateRegimentTerritoireAdjacent;
    /*
     * apres un combat, envoie a tous les joueurs l'update des regiments pour leurs territoires adjacents SI CHANGEMENT
     */
    AID topicUpdateRegimentAdjacent;
    /*
     * recevoir l'info qu'un joueur a ete eliminee, utile si mission est d eliminer un joueur precis
     */
    AID topicElimJoueur;
    /*
     * topic affichage a chaque fin de tour
     */
    AID topicAffichageFinTour;

    private gui.JoueurGui window;

    @SuppressWarnings("deprecation")
    @Override
    protected void setup() {
        window = new gui.JoueurGui(this);
        window.display();
        Random rand = new Random();

        this.territoires = new ArrayList<>();
        this.territoiresPouvantAttaquer = new ArrayList<>();
        this.continents = new ArrayList<>();
        this.main = new ArrayList<>();
        this.armees_eliminees = new ArrayList<>();
        this.attaqueRevanche = new ArrayList<>();
        this.nombreRegimentAPlacer = nombreRegimentMax = 20;

        //gestion couleur des joueurs
        switch (window.getNoJoueurGui()) {
            case (1) -> {
                window.setColor(Color.YELLOW);
                this.couleur = "jaune";
            }
            case (2) -> {
                window.setColor(Color.RED);
                this.couleur = "rouge";
            }
            case (3) -> {
                window.setColor(Color.BLUE);
                this.couleur = "bleu";
                window.getjTextArea().setForeground(Color.WHITE);
            }
            case (4) -> {
                window.setColor(Color.BLACK);
                this.couleur = "noir";
                window.getjTextArea().setForeground(Color.WHITE);
            }
            case (5) -> {
                window.setColor(Color.MAGENTA);
                this.couleur = "violet";
            }
            case (6) -> {
                window.setColor(Color.GREEN);
                this.couleur = "vert";
            }
        }
        window.println("Hello! Agent  " + getLocalName() + " is ready, my address is " + this.getAID().getName());

        detectIntermediaire();
        detectGeneral();
        detectJoueurs();

        //gestion topic manager pour la communication avec l'agent INTERMEDIARE pour avoir les infos plus precise du territoire acquis
        TopicManagementHelper topicHelper;
        try {
            topicHelper = (TopicManagementHelper) getHelper(TopicManagementHelper.SERVICE_NAME);
            topicRegimentTeritoire = topicHelper.createTopic("INFO REGIMENT TERRITOIRE");
            topicRegimentTerritoireAdjacentPourJoueursPhaseRenfort = topicHelper.createTopic("UPDATE TERRITOIRES ADJACENTS POUR JOUEURS PHASE RENFORT");
            topicTerritoire = topicHelper.createTopic("INFO TERRITOIRE");
            topicUpdateContinent = topicHelper.createTopic("UPDATE CONTINENT");
            topicElimJoueur = topicHelper.createTopic("ELIMINATION JOUEUR");
            topicHelper.register(topicTerritoire);
            topicHelper.register(topicRegimentTeritoire);
            topicHelper.register(topicUpdateContinent);
            topicHelper.register(topicRegimentTerritoireAdjacentPourJoueursPhaseRenfort);
            topicHelper.register(topicElimJoueur);
        } catch (ServiceException e) {
            e.printStackTrace();
        }

        //enregistrement de son adresse dans GENERAL et INTERMEDIAIRE
        AgentServicesTools.register(this, "liste joueur", "get AID joueur");

        //A PARTIR DE MTN "PROPAGATE" NE SERT QUE POUR LE RENVOIE DES INFOS DE TERRITOIRE
        //var model0 = MessageTemplate.MatchPerformative(ACLMessage.PROPOSE);
        var model0 = MessageTemplate.MatchConversationId("init jeu");

        //Reception des info du territoire et stockage, fonction ne captant que les messages du model cr?er precedemment

        //reception des cartes territoires et des cartes missions
        addBehaviour(new MsgReceiver(this, model0, MsgReceiver.INFINITE, null, null) {
            protected void handleMessage(ACLMessage msg) {
                if (msg != null) {
                    try {
                        if (msg.getContentObject().getClass().getName().equals("carte.CartePioche")) {
                            //reception
                            CartePioche temp = (CartePioche) msg.getContentObject();

                            //demande a INTERMEDIAIRE les infos du territoire
                            ACLMessage info_territoire = new ACLMessage(ACLMessage.INFORM);
                            info_territoire.setContent(temp.getTerritoire());
                            info_territoire.addReceiver(topicTerritoire);
                            send(info_territoire);

                        } else if (msg.getContentObject().getClass().getName().equals("carte.CarteMission")) {
                            // AJout de la carte mission donn?? par le General

                            CarteMission temp = (CarteMission) msg.getContentObject();
                            if (temp.getCouleur() != null) {
                                if (temp.getCouleur().equals(couleur)) { // alors changement de type de mission
                                    temp = new CarteMission(temp.getNbTerritoire(), TypeMission.TERRITOIRES.toString());
                                }
                            }
                            objectif = temp;
                            strategie = msg.getEncoding();
                            window.println(objectif.toString());
                            String rajout = null;
                            if(strategie.equals("aleatoire")) rajout = ", strategie ou toutes les actions se font de fa?on aleatoire";
                            if(strategie.equals("attaque")) rajout = ", strategie ou les actions seront dirigees vers le territoire ennemi le plus faible";
                            if(strategie.equals("defense")) rajout = ", strategie ou les actions serviront soient a defendre le territoire le plus en danger soit a attaque le territoire l emoins dangereux";
                            if(strategie.equals("passive")) rajout = ", strategie ou le joueur n'attaque pas";
                            if(strategie.equals("equilibre")) rajout = ", strategie ou le joueur cherche a avoir un equilibre dans ses forces d'armees";
                            if(strategie.equals("revanche")) rajout = ", strategie ou le joueur attaque les territoires des joueurs l'ayant attaque";
                            window.println("Le " + getLocalName() + ", adopte une strategie " + strategie + rajout);
                        }
                    } catch (UnreadableException e) {
                        throw new RuntimeException(e);
                    }

                } else window.println("error territoire");
                block();
                reset(model0, MsgReceiver.INFINITE, null, null);
            }
        });

        topicRepartition = AgentServicesTools.generateTopicAID(this, "REPARTITION REGIMENT");

        //ecoute des messages radio
        addBehaviour(new ReceiverBehaviour(this, -1, MessageTemplate.MatchTopic(topicRepartition), true, (a, m) -> {
            window.println("Message recu sur le topic " + topicRepartition.getLocalName() + ". Contenu " + m.getContent()
                    + " emis par :  " + m.getSender().getLocalName());

            //RENFORT
            assignationRegimentTerritoire();
            window.println(territoires.toString());


        }));

        topicAutorisationUpdateRegimentTerritoireAdjacent = AgentServicesTools.generateTopicAID(this, "Update Regiment Territoire Adjacent");

        //ecoute des messages radio
        addBehaviour(new ReceiverBehaviour(this, -1, MessageTemplate.MatchTopic(topicAutorisationUpdateRegimentTerritoireAdjacent), true, (a, m) -> {
            window.println("Message recu sur le topic " + topicAutorisationUpdateRegimentTerritoireAdjacent.getLocalName() + ". Contenu " + m.getContent()
                    + " emis par :  " + m.getSender().getLocalName());

            //COMBAT
            infoRegimentTerritoireAdjacent();
            window.println("\n\nTerritoires adjacents regiments update :");
            window.println(territoires.toString());
        }));

        //A PARTIR DE MTN "PROPAGATE" NE SERT QUE POUR LE RENVOIE DES INFOS DE TERRITOIRE
        //var model1 = MessageTemplate.MatchPerformative(ACLMessage.PROPAGATE);

        //init du model
        var model1 = MessageTemplate.MatchConversationId("send infos territoire");

        //Reception des info du territoire et stockage, fonction ne captant que les messages du model cr?er precedemment
        addBehaviour(new MsgReceiver(this, model1, MsgReceiver.INFINITE, null, null) {
            protected void handleMessage(ACLMessage msg) {
                if (msg != null) {
                    Territoire tempT = null;
                    try {
                        tempT = (Territoire) msg.getContentObject();
                    } catch (UnreadableException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    assert tempT != null;
                    window.println("Message recu sur le model " + model1 + ". Contenu " + tempT
                            + " emis par :  " + msg.getSender().getLocalName());
                    territoires.add(tempT);

                    window.println("Liste de territoire = " + territoires.toString());

                }
                reset(model1, MsgReceiver.INFINITE, null, null);
            }
        });

        //init du model
        var model2 = MessageTemplate.MatchConversationId("retour update regiment territoire adjacent");

        //Reception des info du territoire et stockage, fonction ne captant que les messages du model cr?er precedemment
        addBehaviour(new MsgReceiver(this, model2, MsgReceiver.INFINITE, null, null) {
            protected void handleMessage(ACLMessage msg) {
                if (msg != null) {
                    var infos = msg.getContent().split(",");

                    window.println("Message recu sur le model " + model2 + ". Contenu " + msg.getContent()
                            + " emis par :  " + msg.getSender().getLocalName());

                    // Affectation du nombre de regiment
                    territoires.get(Integer.parseInt(infos[1])).getTerritoires_adjacents().get(Integer.parseInt(infos[2])).setRegimentSurTerritoire(Integer.parseInt(infos[3]));

                    //si dernier update, alors affichage
                    if (Integer.parseInt(infos[1]) == territoires.size() - 1 && Integer.parseInt(infos[2]) == territoires.get(Integer.parseInt(infos[1])).getTerritoires_adjacents().size() - 1) {
                        window.println("\n\nTerritoires adjacents regiments update :");
                        window.println(territoires.toString());
                        updateContinents(null);
                    }
                }
                reset(model2, MsgReceiver.INFINITE, null, null);
            }
        });

        var model3 = MessageTemplate.MatchConversationId("send update continent");


        //addBehaviour(new ContractNetAttaque(this, new ACLMessage(ACLMessage.CFP)));
        addBehaviour(new MsgReceiver(this, model3, MsgReceiver.INFINITE, null, null) {
            protected void handleMessage(ACLMessage msg) {
                if (msg != null) {
                    window.println("\nMessage recu sur le model " + model3 + " emis par :  " + msg.getSender().getLocalName());
                    var infos = msg.getContent().split(",");
                    continents.clear();
                    continents.addAll(Arrays.asList(infos));

                    if (debutPartie) {
                        //System.out.println("verifVictoire");
                        verifVictoire();
                    } else {
                        //System.out.println("debut partie = true");
                        debutPartie = true;
                        autorisationDebutPartie();
                    }

                }
                reset(model3, MsgReceiver.INFINITE, null, null);
            }
        });

        //init du model
        var model4 = MessageTemplate.MatchConversationId("debut tour");

        //Reception de la notification du debut de la phase de renfort
        addBehaviour(new MsgReceiver(this, model4, MsgReceiver.INFINITE, null, null) {
            protected void handleMessage(ACLMessage msg) {
                if (msg != null) {
                    window.println("\nMessage recu sur le model " + model4 + " emis par :  " + msg.getSender().getLocalName());

                    if (objectif.getTypeMission().equals(TypeMission.COULEUR.toString()) && armees_eliminees.contains(objectif.getCouleur())) { // alors victoire
                        String affichage = "Le " + getLocalName() + " a gagne la partie,"
                                + "\ncar il a complete sa mission, l armee " + objectif.getCouleur() + " a ete eliminee."
                                + "\nLe " + getLocalName() + " revendique la victoire.";
                    	window.println("\nEnvoie fin de partie a Intermediaire. ");
                        ACLMessage message1 = new ACLMessage(ACLMessage.REQUEST);
                        message1.setConversationId("victoire / fin de partie");
                        message1.addReceiver(new AID(intermediaire.getLocalName(), AID.ISLOCALNAME));
                        message1.setContent(affichage);
                        send(message1);
                    } else {
                        //RENFORT
                        nouveauxRenforts();
                        addRegimentTerritoire();
                    }
                }
                reset(model4, MsgReceiver.INFINITE, null, null);
            }
        });

        //topic declenche lors de la phase de renfort, recu par tous les joueurs pour update leurs territoires adjacents
        topicRegimentTerritoireAdjacentPourJoueursPhaseRenfort = AgentServicesTools.generateTopicAID(this, "UPDATE TERRITOIRES ADJACENTS POUR JOUEURS PHASE RENFORT");

        //ecoute des messages radio
        addBehaviour(new ReceiverBehaviour(this, -1, MessageTemplate.MatchTopic(topicRegimentTerritoireAdjacentPourJoueursPhaseRenfort), true, (a, m) -> {
            window.println("Message recu sur le topic " + topicRegimentTerritoireAdjacentPourJoueursPhaseRenfort.getLocalName() + ". Contenu " + m.getContent()
                    + " emis par :  " + m.getSender().getLocalName());

            var infos = m.getContent().split(",");

            String nomTerritoire = infos[0];
            int nbRegimentUpdate = Integer.parseInt(infos[1]);
            String nomJoueur = infos[2];

            //update territoires adjacents
            int i, j;
    		for (i = 0; i < this.territoires.size(); i++) // parcours des territoires
    		{
    			for (j = 0; j < this.territoires.get(i).getTerritoires_adjacents().size(); j++) // parcours de tous les territoires adjacents
    			{
					if (this.territoires.get(i).getTerritoires_adjacents().get(j).getNomTerritoire().equals(nomTerritoire)) {
						this.territoires.get(i).getTerritoires_adjacents().get(j).setRegimentSurTerritoire(nbRegimentUpdate);
					}
    			}
    		}

    		//Envoie du message pour que le joueur commence sa phase de combat
    		ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
    		message.setConversationId("autorisation combat");
    		message.addReceiver(new AID(nomJoueur, AID.ISLOCALNAME));
    		message.setContent("autorisation joueur");
    		send(message);
        }));

        //init du model
        var model4bis = MessageTemplate.MatchConversationId("autorisation combat");

        //Reception de la notification du debut de la phase de combat
        addBehaviour(new MsgReceiver(this, model4bis, MsgReceiver.INFINITE, null, null) {
            protected void handleMessage(ACLMessage msg) {
                if (msg != null) {
                    window.println("\nMessage recu sur le model " + model4bis + " emis par :  " + msg.getSender().getLocalName());

                    if(msg.getContent().equals("autorisation joueur"))
                    {
                    	nbAutorJoueur++;
                    	window.println("nbAutorJoueur = " + nbAutorJoueur + ", nbAutorJoueurRecquis = " + nbAutorJoueurRecquis);
                    }

                    if(msg.getContent().equals("autorisation intermediaire"))
                    {
                    	autorIntermediaire = true;
                    }

                    if(autorIntermediaire && nbAutorJoueurRecquis == nbAutorJoueur)
                    {
                    	//reset des variables
                    	autorIntermediaire = false;
                    	nbAutorJoueurRecquis = 500;
                    	nbAutorJoueur = 0;

                    	territoiresPouvantAttaquer.clear();
                        for (int i = 0; i < territoires.size(); i++) {
                            territoiresPouvantAttaquer.add(i);
                        }
                        phaseCombatJoueur(true);
                    }

                }
                reset(model4bis, MsgReceiver.INFINITE, null, null);
            }
        });

        //init du model
        var model5 = MessageTemplate.MatchConversationId("retour resultat attaque");

        //Reception des info du territoire et stockage, fonction ne captant que les messages du model cr?er precedemment
        addBehaviour(new MsgReceiver(this, model5, MsgReceiver.INFINITE, null, null) {
            @SuppressWarnings("null")
            protected void handleMessage(ACLMessage msg) {
                if (msg != null) {
                    var infos = msg.getEncoding().split(",");

                    window.println("Message recu sur le model " + model5 + ". Contenu " + msg.getEncoding()
                            + " emis par :  " + msg.getSender().getLocalName());

                    String nomTerritoireAttaque = infos[0];
                    String nomTerritoireDefense = infos[1];
                    int nbRegimentAttaquant = Integer.parseInt(infos[2]);
                    int nbRegimentAttaquantUpdate = Integer.parseInt(infos[3]);
                    int nbRegimentDefenseur = Integer.parseInt(infos[4]);
                    int nbRegimentDefenseurUpdate = Integer.parseInt(infos[5]);
                    String joueurAttaque = infos[6];
                    String nbJoueurs = infos[7];

                    // Set du nombre d'autorisations requis
                    int nbAutorTopic = 0;
                    if (nbRegimentDefenseurUpdate == 0) // alors forcement les 2 topic ont ete envoyes car ajout et retrait (attribution nouveau territoire)
                    {
                        nbAutorTopic = 2;
                    } else {
                        if (nbRegimentAttaquant > nbRegimentAttaquantUpdate) {
                            nbAutorTopic += 1;
                        }

                        if (nbRegimentDefenseur > nbRegimentDefenseurUpdate) {
                            nbAutorTopic += 1;
                        }
                    }
                    nbAutorTopicTerrAdjRecquis = nbAutorTopic; // avec 6 joueurs, il faut 6 ou 12 autorisations

                    window.println("Bilan de l'attaque de notre territoire" + nomTerritoireAttaque + " sur le territoire " + nomTerritoireDefense + " :");
                    //Perte personnel
                    if (nbRegimentAttaquant == nbRegimentAttaquantUpdate)
                        window.println("Aucune perte de regiment subit");
                    else
                        window.println("Vous avez perdu " + (nbRegimentAttaquant - nbRegimentAttaquantUpdate) + " regiment");
                    //Perte ennemi
                    if (nbRegimentDefenseur == nbRegimentDefenseurUpdate) window.println("Aucune perte ennemi");
                    else
                        window.println("L ennemi a perdu " + (nbRegimentDefenseur - nbRegimentDefenseurUpdate) + " regiment");

                    if (nbRegimentDefenseurUpdate == 0) { //Nouveau territoire
                        try {
                            Territoire tempT = (Territoire) msg.getContentObject();
                            territoires.add(tempT);
                            Territoire t = getTerritoireByName(nomTerritoireAttaque);
                            t.setRegimentSurTerritoire(t.getRegimentSurTerritoire() - nbRegimentAttaquant); // car peu importe si perte ou non -> regiment soit mort, soit sur le nouveau territoire concquis

                        } catch (UnreadableException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }

                        window.println("\nN'ayant plus d ennemis sur le territoire, vous avez concquis le territoire : " + nomTerritoireDefense);

                        if (!territoireConcquis) {
                            territoireConcquis = true;
                            if (strategie.equals("revanche"))
                                for (Territoire t : attaqueRevanche)
                                    if (t.getNomTerritoire().equals(nomTerritoireDefense))
                                        attaqueRevanche.remove(getTerritoireByName(nomTerritoireDefense));

                            //Demande d'une nouvelle carte au General
                            ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
                            message.setConversationId("demande carte pioche");
                            message.addReceiver(new AID(general.getLocalName(), AID.ISLOCALNAME));
                            send(message);
                        }

                    } else // pas de nouveau territoire
                    {
                        if (nbRegimentAttaquant > nbRegimentAttaquantUpdate) {
                            Territoire t = getTerritoireByName(nomTerritoireAttaque);
                            t.setRegimentSurTerritoire(t.getRegimentSurTerritoire() - (nbRegimentAttaquant - nbRegimentAttaquantUpdate));
                        }
                    }

                    //Lancement de l autorisation d une nouvelle attaque
                    ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
                    message.setConversationId("autorisation nouvelle attaque");
                    message.addReceiver(new AID(joueurAttaque, AID.ISLOCALNAME));
                    message.setContent("autorisation attaquant");
                    send(message);
                }
                reset(model5, MsgReceiver.INFINITE, null, null);
            }
        });

        //init du model
        var model6 = MessageTemplate.MatchConversationId("retour resultat defense");

        //Reception des info du territoire et stockage, fonction ne captant que les messages du model cr?er precedemment
        addBehaviour(new MsgReceiver(this, model6, MsgReceiver.INFINITE, null, null) {
            @SuppressWarnings("null")
            protected void handleMessage(ACLMessage msg) {
                if (msg != null) {
                    var infos = msg.getContent().split(",");

                    window.println("Message recu sur le model " + model6 + ". Contenu " + msg.getContent()
                            + " emis par :  " + msg.getSender().getLocalName());

                    String nomTerritoireAttaque = infos[0];
                    String nomTerritoireDefense = infos[1];
                    int nbRegimentAttaquant = Integer.parseInt(infos[2]);
                    int nbRegimentAttaquantUpdate = Integer.parseInt(infos[3]);
                    int nbRegimentDefenseur = Integer.parseInt(infos[4]);
                    int nbRegimentDefenseurUpdate = Integer.parseInt(infos[5]);
                    String joueurAttaque = infos[6];

                    window.println("Le " + joueurAttaque + " a attaque " + nomTerritoireDefense + " via le territoire " + nomTerritoireAttaque);
                    //Perte personnel
                    if (nbRegimentDefenseur == nbRegimentDefenseurUpdate)
                        window.println("Aucune perte de regiment subit");
                    else {
                        window.println("Vous avez perdu " + (nbRegimentDefenseur - nbRegimentDefenseurUpdate) + " regiment");
                        //update de la perte
                        getTerritoireByName(nomTerritoireDefense).setRegimentSurTerritoire(getTerritoireByName(nomTerritoireDefense).getRegimentSurTerritoire() - (nbRegimentDefenseur - nbRegimentDefenseurUpdate));
                    }
                    //Perte ennemi
                    if (nbRegimentAttaquant == nbRegimentAttaquantUpdate) window.println("Aucune perte ennemi");
                    else
                        window.println("L ennemi a perdu " + (nbRegimentAttaquant - nbRegimentAttaquantUpdate) + " regiment");

                    boolean continuer = true;
                    if (nbRegimentDefenseurUpdate <= 0) { //Perte territoire
                        territoires.remove(getTerritoireByName(nomTerritoireDefense));
                        if (strategie.equals("revanche"))
                            if (boolAttaquable(getTerritoireAdjacentByName(nomTerritoireAttaque)))
                                attaqueRevanche.remove(getTerritoireByName(nomTerritoireAttaque));
                        if (territoires.isEmpty()) {
                            window.println("Vous n'avez plus de territoire. Vous etes elimine.");
                            window.println("Strategie : "+strategie+"\nMission : "+objectif.toString()+"\nContinents possedes : "+continents);

                            continuer = false;

                            //Envoie au joueur l'ayant eliminer
                            ACLMessage message = new ACLMessage(ACLMessage.INFORM);
                            message.addReceiver(topicElimJoueur);
                            message.setContent(getLocalName() + "," + couleur + "," + joueurAttaque);
                            send(message);

                            doDelete(); // invoque le takeDown() pour delete l'agent
                        }
                    } else if (strategie.equals("revanche"))
                        attaqueRevanche.add(getTerritoireAdjacentByName(nomTerritoireAttaque));

                    if (continuer) {
                        //Lancement de l autorisation d une nouvelle attaque
                        ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
                        message.setConversationId("autorisation nouvelle attaque");
                        message.addReceiver(new AID(joueurAttaque, AID.ISLOCALNAME));
                        message.setContent("autorisation defenseur");
                        send(message);
                    }

                }
                reset(model6, MsgReceiver.INFINITE, null, null);
            }
        });

        topicUpdateRegimentAdjacent = AgentServicesTools.generateTopicAID(this, "UPDATE REGIMENT ADJACENT");

        //ecoute des messages radio
        addBehaviour(new ReceiverBehaviour(this, -1, MessageTemplate.MatchTopic(topicUpdateRegimentAdjacent), true, (a, m) -> {
            window.println("Message recu sur le topic " + topicUpdateRegimentAdjacent.getLocalName() + ". Contenu " + m.getContent()
                    + " emis par :  " + m.getSender().getLocalName());

            var infos = m.getContent().split(",");

            String nomTerritoire = infos[0];
            int nbRegimentUpdate = Integer.parseInt(infos[1]);
            String joueurAttaque = infos[2];

            int i, j;
            for (i = 0; i < this.territoires.size(); i++) // parcours des territoires
            {
                for (j = 0; j < this.territoires.get(i).getTerritoires_adjacents().size(); j++) // parcours de tous les territoires adjacents
                {
                    if (this.territoires.get(i).getTerritoires_adjacents().get(j).getNomTerritoire().equals(nomTerritoire)) {
                        this.territoires.get(i).getTerritoires_adjacents().get(j).setRegimentSurTerritoire(nbRegimentUpdate);
                    }
                }

            }

            //Lancement de l autorisation d une nouvelle attaque
            ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
            message.setConversationId("autorisation nouvelle attaque");
            message.addReceiver(new AID(joueurAttaque, AID.ISLOCALNAME));
            message.setContent("autorisation topic");
            send(message);
        }));

        var model7 = MessageTemplate.MatchConversationId("autorisation nouvelle attaque");

        addBehaviour(new MsgReceiver(this, model7, MsgReceiver.INFINITE, null, null) {
            protected void handleMessage(ACLMessage msg) {
                if (msg != null) {
                    if (msg.getContent().equals("autorisation attaquant")) {
                        //Autorisation nouvel attaque de l attaquant
                        autorAtt = true;
                        window.println("Attaquant a envoye son autorisation");
                    }
                    if (msg.getContent().equals("autorisation defenseur")) {
                        //Autorisation nouvel attaque du defenseur
                        autorDef = true;
                        window.println("Defenseur a envoye son autorisation");
                    }
                    if (msg.getContent().equals("autorisation topic elim")) {
                        nbTopicElim++;
                        if (nbTopicElim == joueurs.size()) // alors tous les joueurs ont recu l info du joueur eliminee
                        {
                            //Autorisation nouvel attaque du defenseur
                            autorDef = true;
                            window.println("Defenseur a envoye son autorisation");
                        }
                    }
                    if (msg.getContent().equals("autorisation topic")) {
                        //Autorisation nouvel attaque de l attaquant
                        nbAutorTopicTerrAdj++;
                        window.println(msg.getSender().getLocalName() + " a envoye son autorisation. NbTopic = " + nbAutorTopicTerrAdj + " / NbTopicRecquis = " + nbAutorTopicTerrAdjRecquis*joueurs.size());
                    }
                    if ((nbAutorTopicTerrAdjRecquis*joueurs.size()) == nbAutorTopicTerrAdj && !autorTopicTerrAdj) {
                        autorTopicTerrAdj = true;
                        window.println("Tous les topics ont envoyes son autorisation");
                    }

                    if (autorAtt && autorDef && autorTopicTerrAdj) // alors nouvelle attaque
                    {
                        //reset des variables
                        autorAtt = false;
                        autorDef = false;
                        autorTopicTerrAdj = false;
                        nbAutorTopicTerrAdjRecquis = 20;
                        nbAutorTopicTerrAdj = 0;
                        nbTopicElim = 0;

                        window.println("Tous les AGENTS ont envoyes leur autorisation");

                        //verif nouvelle attaque
                        if (strategie.equals("attaque")) {
                            if (!territoiresPouvantAttaquer.isEmpty()) {
                                phaseCombatJoueur(true);
                            } else {
                                territoireConcquis = false;
                                manoeuvreRegiment(); // lancement phase manoeuvre
                            }
                        } else phaseCombatJoueur(rand.nextBoolean());

                    }
                }
                reset(model7, MsgReceiver.INFINITE, null, null);
            }
        });

        var model8 = MessageTemplate.MatchConversationId("envoie carte pioche");

        addBehaviour(new MsgReceiver(this, model8, MsgReceiver.INFINITE, null, null) {
            protected void handleMessage(ACLMessage msg) {
                if (msg != null) {
                    try {
                        CartePioche tempCP = (CartePioche) msg.getContentObject();
                        main.add(tempCP);
                        window.println("\nUn territoire a ete concquis.\nReception d'une nouvelle carte = " + tempCP + ".\n");
                    } catch (UnreadableException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                reset(model8, MsgReceiver.INFINITE, null, null);
            }
        });

        topicAffichageFinTour = AgentServicesTools.generateTopicAID(this, "AFFICHAGE FIN TOUR");

        //ecoute des messages radio
        addBehaviour(new ReceiverBehaviour(this, -1, MessageTemplate.MatchTopic(topicAffichageFinTour), true, (a, m) -> {
            window.println("Message recu sur le topic " + topicAffichageFinTour.getLocalName() + ". Contenu " + m.getContent()
                    + " emis par :  " + m.getSender().getLocalName());

            window.println(territoires.toString());
            if(m.getEncoding() != null)
            {
            	int nbTerritoire = 0, nbRegiment = 0;
            	for(Territoire t : territoires)
            	{
            		nbTerritoire++;
            		nbRegiment += t.getRegimentSurTerritoire();
            	}
            	
            	String affichage = m.getEncoding();
            	window.println(affichage);
            	window.println("Strategie : "+strategie+"\nNombre de territoire : "+nbTerritoire+"\nNombre de regiment : "+nbRegiment+"\nMission : "+objectif.toString()+"\nContinents possedes : "+continents);
            }
        }));

        topicElimJoueur = AgentServicesTools.generateTopicAID(this, "ELIMINATION JOUEUR");

        //un adversaire a ete eliminee
        addBehaviour(new ReceiverBehaviour(this, -1, MessageTemplate.MatchTopic(topicElimJoueur), true, (a, m) -> {
            var infos = m.getContent().split(",");

            window.println("Message recu sur le topic " + topicElimJoueur.getLocalName() + ". Contenu " + m.getContent()
                    + " emis par :  " + m.getSender().getLocalName());

            window.println("Le " + infos[0] + " et donc l'armee " + infos[1] + " a ete eliminee par le " + infos[2]);

            armees_eliminees.add(infos[1]);

            //Lancement de l autorisation d une nouvelle attaque avec l'autorisation du defenseur, mtn elimine
            ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
            message.setConversationId("autorisation nouvelle attaque");
            message.addReceiver(new AID(infos[2], AID.ISLOCALNAME));
            message.setContent("autorisation topic elim");
            send(message);
        }));

    }

    // Fermeture de l'agent
    @Override
    protected void takeDown() {
        // S'effacer du service pages jaunes
        try {
            DFService.deregister(this);
        } catch (FIPAException ignored) {
        }
        System.out.println("TakeDown de " + this.getLocalName());
        //window.dispose();
    }

    private void detectIntermediaire() {
        var model = AgentServicesTools.createAgentDescription("intermediaire", "link");

        //souscription au service des pages jaunes pour recevoir une alerte en cas mouvement sur le service travel agency'seller
        addBehaviour(new DFSubscriber(this, model) {
            @Override
            public void onRegister(DFAgentDescription dfd) {
                intermediaire = dfd.getName();
                window.println(dfd.getName().getLocalName() + " s'est inscrit en tant qu'intermediaire : " + model.getAllServices().get(0));
            }

            @Override
            public void onDeregister(DFAgentDescription dfd) {
                if (dfd.getName().equals(general)) {
                    intermediaire = null;
                    window.println(dfd.getName().getLocalName() + " s'est desinscrit de  : " + model.getAllServices().get(0));
                }
            }
        });
    }

    private void detectGeneral() {
        var model = AgentServicesTools.createAgentDescription("general", "link");

        //souscription au service des pages jaunes pour recevoir une alerte en cas mouvement sur le service travel agency'seller
        addBehaviour(new DFSubscriber(this, model) {
            @Override
            public void onRegister(DFAgentDescription dfd) {
                general = dfd.getName();
                window.println(dfd.getName().getLocalName() + " s'est inscrit en tant que general : " + model.getAllServices().get(0));
            }

            @Override
            public void onDeregister(DFAgentDescription dfd) {
                if (dfd.getName().equals(general)) {
                    general = null;
                    window.println(dfd.getName().getLocalName() + " s'est desinscrit de  : " + model.getAllServices().get(0));
                }
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
            }
        });
        //System.out.println("Liste de joueurs" + joueurs);

    }

    // fonction permettant la premi?re assignation de r?giments sur les territoires du joueur
    public void assignationRegimentTerritoire() {
        for (Territoire t : territoires) {
            t.setRegimentSurTerritoire(1);
            nombreRegimentAPlacer -= 1;
        }
        Random rand = new Random();
        while (nombreRegimentAPlacer != 0) {
            int alea = rand.nextInt(territoires.size());
            territoires.get(alea).addRegimentSurTerritoire(1);
            nombreRegimentAPlacer--;
        }
        for (Territoire ter : territoires) {
            updateRegimentTerritoire(ter);
        }
    }

    public void updateRegimentTerritoire(Territoire ter) {
        ACLMessage infoRegimentTerritoire = new ACLMessage(ACLMessage.INFORM);
        try {
            infoRegimentTerritoire.setContentObject(ter);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        infoRegimentTerritoire.addReceiver(topicRegimentTeritoire);
        if (debutPartie) // alors la fonction sert a update le plateau apres nouveau renfort a chaque tour
        {
            infoRegimentTerritoire.setEncoding("" + territoires.size());
        }
        send(infoRegimentTerritoire);
    }

    public void updateRegimentTerritoire(Territoire ter, int size) {
        ACLMessage infoRegimentTerritoire = new ACLMessage(ACLMessage.INFORM);
        try {
            infoRegimentTerritoire.setContentObject(ter);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        infoRegimentTerritoire.addReceiver(topicRegimentTeritoire);
        if (debutPartie) // alors la fonction sert a update le plateau apres nouveau renfort a chaque tour
        {
            infoRegimentTerritoire.setEncoding("" + size);
        }
        send(infoRegimentTerritoire);
    }

    public void updateRegimentTerritoireAdjPourJoueursPhaseRenfort(Territoire ter) {
        ACLMessage infoRegimentTerritoireAdjPourJoueursPhaseRenfort = new ACLMessage(ACLMessage.INFORM);
        infoRegimentTerritoireAdjPourJoueursPhaseRenfort.addReceiver(topicRegimentTerritoireAdjacentPourJoueursPhaseRenfort);
        infoRegimentTerritoireAdjPourJoueursPhaseRenfort.setContent(ter.getNomTerritoire()+","+ter.getRegimentSurTerritoire()+","+getLocalName());
        send(infoRegimentTerritoireAdjPourJoueursPhaseRenfort);
    }

    /*
     * Fonction pour update les informations des nombres de regiment pr?sent sur les territoires adjacents
     */
    public void infoRegimentTerritoireAdjacent() {
        int i, j;
        for (i = 0; i < this.territoires.size(); i++) // parcours des territoires
        {
            for (j = 0; j < this.territoires.get(i).getTerritoires_adjacents().size(); j++) // parcours de tous les territoires adjacents
            {
                //variable pour raccourcir le nom
                Territoire t_actuel = this.territoires.get(i).getTerritoires_adjacents().get(j);
                if (this.territoires.contains(t_actuel)) // alors on poss?de d?ja l'info
                {
                    //affectation nombre de regiment
                    Territoire temp = getTerritoireByName(this.territoires.get(i).getTerritoires_adjacents().get(j).getNomTerritoire());
                    this.territoires.get(i).getTerritoires_adjacents().get(j).setRegimentSurTerritoire(temp.getRegimentSurTerritoire());
                    window.println("Info d?j? en notre possession : " + temp);
                    //si dernier update, alors affichage
                    if (i == territoires.size() - 1 && j == territoires.get(i).getTerritoires_adjacents().size() - 1) {
                        window.println("\n\nTerritoires adjacents regiments update :");
                        window.println(territoires.toString());
                        updateContinents(null);
                    }
                } else // on demande a intermedaire d'update
                {
                    ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
                    message.setConversationId("update regiment territoire adjacent");
                    message.addReceiver(new AID(intermediaire.getLocalName(), AID.ISLOCALNAME));
                    message.setContent(t_actuel.getNomTerritoire() + "," + i + "," + j);
                    send(message);

                    //le retour ce fera grace au model3 dans setup()
                }
            }
        }
    }

    /*
     * Fonction que tous les joueurs envoie a intermediaire pour notifier qu'ils ont fini leurs phase de renfort
     */
    private void autorisationDebutPartie() {
        window.println("\nEnvoie autorisation commencement debut de partie a Intermediaire.");
        ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
        message.setConversationId("autorisation debut partie");
        message.addReceiver(new AID(intermediaire.getLocalName(), AID.ISLOCALNAME));
        send(message);
    }

    // Permet de rajouter d'obtenir de nouveaux renforts
    public void nouveauxRenforts() {
        String affichage;
        int nbAddRegiment = this.territoires.size() / 3;
        if (nbAddRegiment < 3)
            nbAddRegiment = 3;
        affichage = "Il a obtenu de nouveaux regiments\nIl en obtient :\n\t" + nbAddRegiment + " grace a ces territoires";
        this.nombreRegimentMax += nbAddRegiment;
        this.nombreRegimentAPlacer += nbAddRegiment;
        int nbRegimentContinent = 0;
        if (!this.continents.isEmpty())
            for (String c : continents)
                switch (c) {
                    case "AMERIQUE_NORD", "EUROPE" -> {
                        nbRegimentContinent += 5;
                        this.nombreRegimentAPlacer += 5;
                        this.nombreRegimentMax += 5;
                    }
                    case "AMERIQUE_SUD", "OCEANIE" -> {
                        nbRegimentContinent += 2;
                        this.nombreRegimentAPlacer += 2;
                        this.nombreRegimentMax += 2;
                    }
                    case "AFRIQUE" -> {
                        nbRegimentContinent += 3;
                        this.nombreRegimentAPlacer += 3;
                        this.nombreRegimentMax += 3;
                    }
                    case "ASIE" -> {
                        nbRegimentContinent += 7;
                        this.nombreRegimentAPlacer += 7;
                        this.nombreRegimentMax += 7;
                    }
                }
        if (nbRegimentContinent != 0)
            affichage += "\n\t" + nbRegimentContinent + " grace a ces continents qu'il possede";
        String affichageCartes = "";
        if (main.size() >= 3)
            affichageCartes = nouveauxRenfortsMain();
        if (!affichageCartes.isEmpty())
            affichage += "\n\t" + affichageCartes;
        window.println(affichage);

    }

    // Permet de rajouter d'obtenir de nouveaux renforts par rapport aux cartes se trouvant dans la main du joueur
    private String nouveauxRenfortsMain() {
        int nbFantassin = 0, nbCavalier = 0, nbCanon = 0, nbJoker = 0, troisCartes = 0;
        StringBuilder renvoieInfo = new StringBuilder();
        Map<Integer, String> fantassins = new HashMap<>();
        Map<Integer, String> cavaliers = new HashMap<>();
        Map<Integer, String> canons = new HashMap<>();
        Map<Integer, String> jokers = new HashMap<>();
        boolean joker = false;
        for (int i = 0; i < main.size(); i++) {
            switch (main.get(i).getUnite()) {
                case ("FANTASSIN") -> {
                    nbFantassin++;
                    fantassins.put(i, main.get(i).getTerritoire());
                }
                case ("CAVALIER") -> {
                    nbCavalier++;
                    cavaliers.put(i, main.get(i).getTerritoire());
                }
                case ("CANON") -> {
                    nbCanon++;
                    canons.put(i, main.get(i).getTerritoire());
                }
                case ("FANTASSIN_CAVALERIE_CANON") -> {
                    nbFantassin++;
                    fantassins.put(i, main.get(i).getTerritoire());
                    nbCavalier++;
                    cavaliers.put(i, main.get(i).getTerritoire());
                    nbCanon++;
                    canons.put(i, main.get(i).getTerritoire());
                    joker = true;
                    nbJoker++;
                    jokers.put(i, main.get(i).getTerritoire());
                }
            }
        }
        List<Integer> keyCanon = new ArrayList<>(canons.keySet());
        List<Integer> keyCanonSorted = keyCanon.stream().sorted(Comparator.reverseOrder()).toList();
        List<Integer> keyCavalier = new ArrayList<>(cavaliers.keySet());
        List<Integer> keyCavalierSorted = keyCavalier.stream().sorted(Comparator.reverseOrder()).toList();
        List<Integer> keyFantassin = new ArrayList<>(fantassins.keySet());
        List<Integer> keyFantassinSorted = keyFantassin.stream().sorted(Comparator.reverseOrder()).toList();
        List<Integer> keyJokers = new ArrayList<>(jokers.keySet());
        List<Integer> keyJokersSorted = keyJokers.stream().sorted(Comparator.reverseOrder()).toList();
        if ((nbFantassin > 0) && (nbCavalier > 0) && (nbCanon > 0) && !joker) {
            LinkedList<String> unite = new LinkedList<>();
            int j = keyFantassin.get(0);
            unite.add("FANTASSIN");
            int k = keyCavalier.get(0);
            if (k > j)
                unite.addFirst("CAVALIER");
            else
                unite.add("CAVALIER");
            int t = keyCanon.get(0);
            if ((t > k) && (t > j))
                unite.addFirst("CANON");
            else if ((t < k) && (t < j))
                unite.addLast("CANON");
            else
                unite.add(1, "CANON");
            for (String s : unite) {
                switch (s) {
                    case "FANTASSIN" -> {
                        returnCarteGeneral(j);
                        for (Territoire ter : territoires)
                            if (ter.getNomTerritoire().equals(fantassins.get(j))) {
                                nombreRegimentMax += 2;
                                nombreRegimentAPlacer += 2;
                                renvoieInfo.append(2 + " car la carte echange est un des territoires possedes par le joueur\n\t");
                            }
                        fantassins.remove(j);
                    }
                    case "CAVALIER" -> {
                        returnCarteGeneral(k);
                        for (Territoire ter : territoires)
                            if (ter.getNomTerritoire().equals(cavaliers.get(k))) {
                                nombreRegimentMax += 2;
                                nombreRegimentAPlacer += 2;
                                renvoieInfo.append(2 + " car la carte echange est un des territoires possedes par le joueur\n\t");
                            }
                        cavaliers.remove(k);
                    }
                    case "CANON" -> {
                        returnCarteGeneral(t);
                        for (Territoire ter : territoires)
                            if (ter.getNomTerritoire().equals(canons.get(t))) {
                                nombreRegimentMax += 2;
                                nombreRegimentAPlacer += 2;
                                renvoieInfo.append(2 + " car la carte echange est un des territoires possedes par le joueur\n\t");
                            }
                        canons.remove(t);
                    }
                }
            }
            renvoieInfo.append(10 + " grace a l'echange d'une carte fantassin, d'une carte cavalier et d'une carte canon");
            nombreRegimentMax += 10;
            nombreRegimentAPlacer += 10;
        } else if (joker && (((nbFantassin > 1) && (nbCanon > 1)) || ((nbFantassin > 1) && (nbCavalier > 1)) || ((nbCavalier > 1) && (nbCanon > 1)))) {
            LinkedList<String> unite = new LinkedList<>();
            if (nbJoker == 2) {
                if (!keyJokersSorted.contains(0)) {
                    unite.add(main.get(0).getUnite());
                    unite.add("JOKER");
                    unite.add("JOKER");
                } else if (!keyJokersSorted.contains(1)) {
                    unite.add("JOKER");
                    unite.add(main.get(1).getUnite());
                    unite.add("JOKER");
                } else if (!keyJokersSorted.contains(2)) {
                    unite.add("JOKER");
                    unite.add("JOKER");
                    unite.add(main.get(2).getUnite());
                }
            } else {
                if (keyJokersSorted.contains(0)) {
                    unite.add("JOKER");
                    unite.add(main.get(1).getUnite());
                    unite.add(main.get(2).getUnite());
                } else if (keyJokersSorted.contains(1)) {
                    unite.add(main.get(0).getUnite());
                    unite.add("JOKER");
                    unite.add(main.get(2).getUnite());
                } else if (keyJokersSorted.contains(2)) {
                    unite.add(main.get(0).getUnite());
                    unite.add(main.get(1).getUnite());
                    unite.add("JOKER");
                } else if (keyJokersSorted.contains(3)) {
                    unite.add(main.get(0).getUnite());
                    unite.add(main.get(1).getUnite());
                    unite.add(main.get(2).getUnite());
                    unite.add("JOKER");
                } else {
                    unite.add(main.get(0).getUnite());
                    unite.add(main.get(1).getUnite());
                    unite.add(main.get(2).getUnite());
                    unite.add(main.get(3).getUnite());
                    unite.add("JOKER");
                }
            }
            if (nbFantassin == 1) {
                boolean cavalierSend = false, canonSend = false;
                for (int i = (unite.size() - 1); i >= 0; i--) {
                    switch (unite.get(i)) {
                        case "JOKER" -> {
                            nbFantassin--;
                            nbCanon--;
                            nbCavalier--;
                            nbJoker--;
                            fantassins.remove(i);
                            cavaliers.remove(i);
                            canons.remove(i);
                            jokers.remove(i);
                        }
                        case "CAVALIER" -> {
                            if (!cavalierSend) {
                                cavalierSend = true;
                                nbCavalier--;
                                for (Territoire ter : territoires)
                                    if (ter.getNomTerritoire().equals(cavaliers.get(i))) {
                                        nombreRegimentMax += 2;
                                        nombreRegimentAPlacer += 2;
                                        renvoieInfo.append(2 + " car la carte echange est un des territoires possedes par le joueur\n\t");
                                    }
                                cavaliers.remove(i);
                            }
                        }
                        case "CAN0N" -> {
                            if (!canonSend) {
                                canonSend = true;
                                nbCanon--;
                                for (Territoire ter : territoires)
                                    if (ter.getNomTerritoire().equals(canons.get(i))) {
                                        nombreRegimentMax += 2;
                                        nombreRegimentAPlacer += 2;
                                        renvoieInfo.append(2 + " car la carte echange est un des territoires possedes par le joueur\n\t");
                                    }
                                canons.remove(i);
                            }
                        }
                    }
                    returnCarteGeneral(i);
                }
                renvoieInfo.append(10 + " grace a l'echange d'une carte joker, d'une carte cavalier et d'une carte canon");
            } else if (nbCanon == 1) {
                boolean cavalierSend = false, fantassinSend = false;
                for (int i = (unite.size() - 1); i >= 0; i--) {
                    switch (unite.get(i)) {
                        case "JOKER" -> {
                            nbFantassin--;
                            nbCanon--;
                            nbCavalier--;
                            nbJoker--;
                            fantassins.remove(i);
                            cavaliers.remove(i);
                            canons.remove(i);
                            jokers.remove(i);
                        }
                        case "CAVALIER" -> {
                            if (!cavalierSend) {
                                cavalierSend = true;
                                nbCavalier--;
                                for (Territoire ter : territoires)
                                    if (ter.getNomTerritoire().equals(cavaliers.get(i))) {
                                        nombreRegimentMax += 2;
                                        nombreRegimentAPlacer += 2;
                                        renvoieInfo.append(2 + " car la carte echange est un des territoires possedes par le joueur\n\t");
                                    }
                                cavaliers.remove(i);
                            }
                        }
                        case "FANTASSIN" -> {
                            if (!fantassinSend) {
                                fantassinSend = true;
                                nbFantassin--;
                                for (Territoire ter : territoires)
                                    if (ter.getNomTerritoire().equals(fantassins.get(i))) {
                                        nombreRegimentMax += 2;
                                        nombreRegimentAPlacer += 2;
                                        renvoieInfo.append(2 + " car la carte echange est un des territoires possedes par le joueur\n\t");
                                    }
                                fantassins.remove(i);
                            }
                        }
                    }
                    returnCarteGeneral(i);
                }
                renvoieInfo.append(10 + " grace a l'echange d'une carte fantassin, d'une carte cavalier et d'une carte joker");
            } else if (nbCavalier == 1) {
                boolean fantassinSend = false, canonSend = false;
                for (int i = (unite.size() - 1); i >= 0; i--) {
                    switch (unite.get(i)) {
                        case "JOKER" -> {
                            nbFantassin--;
                            nbCanon--;
                            nbCavalier--;
                            nbJoker--;
                            fantassins.remove(i);
                            cavaliers.remove(i);
                            canons.remove(i);
                            jokers.remove(i);
                        }
                        case "FANTASSIN" -> {
                            if (!fantassinSend) {
                                fantassinSend = true;
                                nbFantassin--;
                                for (Territoire ter : territoires)
                                    if (ter.getNomTerritoire().equals(fantassins.get(i))) {
                                        nombreRegimentMax += 2;
                                        nombreRegimentAPlacer += 2;
                                        renvoieInfo.append(2 + " car la carte echange est un des territoires possedes par le joueur\n\t");
                                    }
                                fantassins.remove(i);
                            }
                        }
                        case "CAN0N" -> {
                            if (!canonSend) {
                                canonSend = true;
                                nbCanon--;
                                for (Territoire ter : territoires)
                                    if (ter.getNomTerritoire().equals(canons.get(i))) {
                                        nombreRegimentMax += 2;
                                        nombreRegimentAPlacer += 2;
                                        renvoieInfo.append(2 + " car la carte echange est un des territoires possedes par le joueur\n\t");
                                    }
                                canons.remove(i);
                            }
                        }
                    }
                    returnCarteGeneral(i);
                }
                renvoieInfo.append(10 + " grace a l'echange d'une carte fantassin, d'une carte joker et d'une carte canon");
            }
            nombreRegimentMax += 10;
            nombreRegimentAPlacer += 10;
        } else if (nbCanon >= 3) {
            for (Integer j : keyCanonSorted) {
                returnCarteGeneral(j);
                if (!canons.get(j).equals("JOKER"))
                    for (Territoire ter : territoires) {
                        if (ter.getNomTerritoire().equals(canons.get(j))) {
                            nombreRegimentMax += 2;
                            nombreRegimentAPlacer += 2;
                            renvoieInfo.append(2 + " car la carte echange est un des territoires possedes par le joueur\n\t");
                        }
                    }
                else {
                    nbCavalier--;
                    nbFantassin--;
                    nbJoker--;
                    cavaliers.remove(j);
                    fantassins.remove(j);
                    jokers.remove(j);
                }
                nbCanon--;
                canons.remove(j);
                troisCartes++;
                if (troisCartes == 3)
                    break;
            }
            if (joker)
                renvoieInfo.append(8 + " grace a l'echange de deux cartes canon et d'une carte joker");
            else
                renvoieInfo.append(8 + " grace a l'echange de trois cartes canon");
            nombreRegimentMax += 8;
            nombreRegimentAPlacer += 8;
        } else if (nbCavalier >= 3) {
            for (Integer j : keyCavalierSorted) {
                returnCarteGeneral(j);
                if (!cavaliers.get(j).equals("JOKER"))
                    for (Territoire ter : territoires) {
                        if (ter.getNomTerritoire().equals(cavaliers.get(j))) {
                            nombreRegimentMax += 2;
                            nombreRegimentAPlacer += 2;
                            renvoieInfo.append(2 + " car la carte echange est un des territoires possedes par le joueur\n\t");
                        }
                    }
                else {
                    nbCanon--;
                    nbFantassin--;
                    nbJoker--;
                    fantassins.remove(j);
                    canons.remove(j);
                    jokers.remove(j);
                }
                nbCavalier--;
                cavaliers.remove(j);
                troisCartes++;
                if (troisCartes == 3)
                    break;
            }
            if (joker)
                renvoieInfo.append(6 + " grace a l'echange de deux cartes cavalier et d'une carte joker");
            else
                renvoieInfo.append(6 + " grace a l'echange de trois cartes cavalier");
            nombreRegimentMax += 6;
            nombreRegimentAPlacer += 6;
        } else if (nbFantassin >= 3) {
            for (Integer j : keyFantassinSorted) {
                returnCarteGeneral(j);
                if (!fantassins.get(j).equals("JOKER"))
                    for (Territoire ter : territoires) {
                        if (ter.getNomTerritoire().equals(fantassins.get(j))) {
                            nombreRegimentMax += 2;
                            nombreRegimentAPlacer += 2;
                            renvoieInfo.append(2 + " car la carte echange est un des territoires possedes par le joueur\n\t");
                        }
                    }
                else {
                    nbCavalier--;
                    nbCanon--;
                    nbJoker--;
                    cavaliers.remove(j);
                    canons.remove(j);
                    jokers.remove(j);
                }
                nbFantassin--;
                fantassins.remove(j);
                troisCartes++;
                if (troisCartes == 3)
                    break;
            }
            if (joker)
                renvoieInfo.append(4 + " grace a l'echange de deux cartes fantassin et d'une carte joker");
            else
                renvoieInfo.append(4 + " grace a l'echange de trois cartes fantassin");
            nombreRegimentMax += 4;
            nombreRegimentAPlacer += 4;
        }
        return renvoieInfo.toString();
    }

    // Renvoie des cartes de la main vers la pioche du General
    private void returnCarteGeneral(int i) {
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

    // fonction qui permet de mettre ? jour les continents possedes
    public void updateContinents(String changementManoeuvre) {
        ACLMessage continent = new ACLMessage(ACLMessage.INFORM);
        try {
            continent.setContentObject((Serializable) territoires);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        if (changementManoeuvre != null) {
            continent.setEncoding(changementManoeuvre);
        }
        continent.addReceiver(topicUpdateContinent);
        send(continent);

    }

    public void addRegimentTerritoire() {
        List<Territoire> terEnvoie = new ArrayList<>();
    	if(objectif.getTypeMission().equals(TypeMission.TERRITOIRES_ET_ARMEES_MIN.toString())) {
    		for(Territoire t : territoires) {
                while(nombreRegimentAPlacer != 0 && t.getRegimentSurTerritoire() < objectif.getNbArmee()) {
                    t.addRegimentSurTerritoire(1);
                    nombreRegimentAPlacer--;
                    if(!terEnvoie.contains(territoires.get(territoires.indexOf(t))))
                        terEnvoie.add(territoires.get(territoires.indexOf(t)));
                }
    		}
    	}
        switch (strategie) {
            case "aleatoire", "passive" -> {
                Random rand = new Random();
                while (nombreRegimentAPlacer != 0) {
                    int indexTer = rand.nextInt(territoires.size());
                    territoires.get(indexTer).addRegimentSurTerritoire(1);
                    nombreRegimentAPlacer--;
                    if(!terEnvoie.contains(territoires.get(indexTer)))
                        terEnvoie.add(territoires.get(indexTer));
                }
            }
            case "attaque" -> {
                String position = findPositionLowestValue();
                if(!position.isEmpty()) {
                    String[] pos = position.split(",");
                    int indexTer = Integer.parseInt(pos[0]);
                    territoires.get(indexTer).addRegimentSurTerritoire(nombreRegimentAPlacer);
                    nombreRegimentAPlacer = 0;
                    if(!terEnvoie.contains(territoires.get(indexTer)))
                        terEnvoie.add(territoires.get(indexTer));
                } else {
                    while (nombreRegimentAPlacer != 0) {
                        int indexTer = indexWeakestTerritoire();
                        territoires.get(indexTer).addRegimentSurTerritoire(1);
                        nombreRegimentAPlacer--;
                        if(!terEnvoie.contains(territoires.get(indexTer)))
                            terEnvoie.add(territoires.get(indexTer));
                    }
                }
            }
            case "defense" -> {
                int indexTer = indexGreatestDanger();
                if(indexTer != -1) {
                    territoires.get(indexTer).addRegimentSurTerritoire(nombreRegimentAPlacer);
                    nombreRegimentAPlacer = 0;
                    if(!terEnvoie.contains(territoires.get(indexTer)))
                        terEnvoie.add(territoires.get(indexTer));
                } else {
                    while (nombreRegimentAPlacer != 0) {
                        indexTer = indexWeakestTerritoire();
                        territoires.get(indexTer).addRegimentSurTerritoire(1);
                        nombreRegimentAPlacer--;
                        if(!terEnvoie.contains(territoires.get(indexTer)))
                            terEnvoie.add(territoires.get(indexTer));
                    }
                }
            }
            case "equilibre", "revanche" -> {
                while (nombreRegimentAPlacer != 0) {
                    int indexTer = indexWeakestTerritoire();
                    territoires.get(indexTer).addRegimentSurTerritoire(1);
                    nombreRegimentAPlacer--;
                    if(!terEnvoie.contains(territoires.get(indexTer)))
                        terEnvoie.add(territoires.get(indexTer));
                }
            }
        }

    	//set du nombre de topic de joueurs a recevoir avant de continuer
        nbAutorJoueurRecquis = joueurs.size() * terEnvoie.size();

        //envoie des update au plateau et aux joueurs
        for (Territoire ter : terEnvoie) {
            updateRegimentTerritoire(ter, terEnvoie.size());
            updateRegimentTerritoireAdjPourJoueursPhaseRenfort(ter);
        }
    }

    private void phaseCombatJoueur(boolean attaque) {
        window.println("\nAttaque");
        /*
         * Gerer les attaques A FAIRE
         */
        String nomTerritoireAttaque, nomTerritoireDefense;
        int nbRegimentAttaquant, nbRegimentDefenseur;
        Random rand = new Random();
        int tAtt; // indice du territoire adjacent a attaque
        switch (this.strategie) {
            case "attaque" -> {
                // on attaque avec le 1er territoire de la liste
                int i = territoiresPouvantAttaquer.get(0);
                // on remove pour ne plus attaquer ce territoire
                territoiresPouvantAttaquer.remove(0);
                if (this.territoires.get(i).getRegimentSurTerritoire() > 1) // alors assez d unite pour attaque
                {
                    tAtt = findPositionLowestValue(this.territoires.get(i));
                    if (tAtt != -1) {

                        ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
                        message.setConversationId("lancement attaque");
                        message.addReceiver(new AID(intermediaire.getLocalName(), AID.ISLOCALNAME));

                        nomTerritoireAttaque = this.territoires.get(i).getNomTerritoire();
                        nomTerritoireDefense = this.territoires.get(i).getTerritoires_adjacents().get(tAtt).getNomTerritoire();
                        nbRegimentAttaquant = this.territoires.get(i).getRegimentSurTerritoire() - 1;
                        nbRegimentDefenseur = this.territoires.get(i).getTerritoires_adjacents().get(tAtt).getRegimentSurTerritoire();
                        message.setContent(nomTerritoireAttaque + "," + nomTerritoireDefense + "," + nbRegimentAttaquant + "," + nbRegimentDefenseur);
                        window.println("Attente retour de l'attaque de " + nomTerritoireAttaque + " sur " + nomTerritoireDefense + " de Intermediaire");
                        send(message);
                    } else {
                        window.println("Les territoires adjacents ne sont que des territoires allies. Le territoire " + this.territoires.get(i).getNomTerritoire() + " ne peut pas attaquer");
                        //verif nouvelle attaque
                        if (!territoiresPouvantAttaquer.isEmpty()) {
                            phaseCombatJoueur(true);
                        } else {
                            manoeuvreRegiment(); // lancement phase manoeuvre
                            territoireConcquis = false;
                        }
                    }
                } else {
                    window.println("Le territoire " + this.territoires.get(i).getNomTerritoire() + " n a pas assez d unite pour attaquer");
                    //verif nouvelle attaque
                    if (!territoiresPouvantAttaquer.isEmpty()) {
                        phaseCombatJoueur(true);
                    } else {
                        manoeuvreRegiment(); // lancement phase manoeuvre
                        territoireConcquis = false;
                    }
                }
            }
            case "aleatoire" -> {
                if (attaque) {
                    int i, tempI, j;
                    List<Territoire> tempList = new ArrayList<>(territoires);
                    tempList.removeIf(t -> t.getRegimentSurTerritoire() <= 1);
                    if(!tempList.isEmpty()) {
                        tempI = rand.nextInt(tempList.size());
                        i = territoires.indexOf(tempList.get(tempI));
                        List<Territoire> listTemp = new ArrayList<>(this.territoires.get(i).getTerritoires_adjacents());
                        for (int k = (listTemp.size() - 1); k >= 0; --k)
                            if (!territoireNotContains(listTemp.get(k)))
                                listTemp.remove(k);
                        if (!listTemp.isEmpty()) {
                            j = rand.nextInt(listTemp.size());
                            window.println("\n Position de l'attaque \n" + i + "\t" + j);
                            ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
                            message.setConversationId("lancement attaque");
                            message.addReceiver(new AID(intermediaire.getLocalName(), AID.ISLOCALNAME));

                            nomTerritoireAttaque = this.territoires.get(i).getNomTerritoire();
                            nomTerritoireDefense = this.territoires.get(i).getTerritoires_adjacents().get(j).getNomTerritoire();
                            // random entre 1 et le total de regiment - 1 car au moins 1 unite doit rester 
                            nbRegimentAttaquant = (rand.nextInt(this.territoires.get(i).getRegimentSurTerritoire() - 1) + 1);
                            nbRegimentDefenseur = this.territoires.get(i).getTerritoires_adjacents().get(j).getRegimentSurTerritoire();
                            message.setContent(nomTerritoireAttaque + "," + nomTerritoireDefense + "," + nbRegimentAttaquant + "," + nbRegimentDefenseur);
                            send(message);
                        } else
                            manoeuvreRegiment();
                    } else
                        manoeuvreRegiment();
                } else {
                    manoeuvreRegiment();
                    territoireConcquis = false;
                }
            }
            case "defense", "equilibre" -> {
                if (attaque) {
                    int i, j;
                    String position = findPositionBiggestGap();
                    if (!position.isEmpty()) {
                        String[] string = position.split(",");
                        i = Integer.parseInt(string[0]);
                        j = Integer.parseInt(string[1]);
                        window.println("\n Position de l'attaque \n" + i + "\t" + j);
                        ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
                        message.setConversationId("lancement attaque");
                        message.addReceiver(new AID(intermediaire.getLocalName(), AID.ISLOCALNAME));

                        nomTerritoireAttaque = this.territoires.get(i).getNomTerritoire();
                        nomTerritoireDefense = this.territoires.get(i).getTerritoires_adjacents().get(j).getNomTerritoire();
                        nbRegimentAttaquant = this.territoires.get(i).getRegimentSurTerritoire() / 2;
                        nbRegimentDefenseur = this.territoires.get(i).getTerritoires_adjacents().get(j).getRegimentSurTerritoire();
                        message.setContent(nomTerritoireAttaque + "," + nomTerritoireDefense + "," + nbRegimentAttaquant + "," + nbRegimentDefenseur);
                        send(message);
                    } else
                        manoeuvreRegiment();
                    //phaseCombatJoueur();
                } else {
                    manoeuvreRegiment();
                    territoireConcquis = false;
                }
            }
            case "passive" -> {
                window.println("Le " + getLocalName() + " ayant une strategie passive, il n'attaque pas");
                manoeuvreRegiment();
            }
            case "revanche" -> {
                if (!attaqueRevanche.isEmpty()) {
                    int indexAtt = -1, indexDef = -1;
                    Territoire attR = attaqueRevanche.get(0);
                    String position = findPositionBiggestGap(attR);
                    if(!position.isEmpty()) {
                        String[] string = position.split(",");
                        indexAtt = Integer.parseInt(string[0]);
                        indexDef = Integer.parseInt(string[1]);
                    }
                    attaqueRevanche.remove(0);

                    if (indexAtt != -1 && indexDef != -1) {
                        ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
                        message.setConversationId("lancement attaque");
                        message.addReceiver(new AID(intermediaire.getLocalName(), AID.ISLOCALNAME));

                        nomTerritoireAttaque = this.territoires.get(indexAtt).getNomTerritoire();
                        nomTerritoireDefense = this.territoires.get(indexAtt).getTerritoires_adjacents().get(indexDef).getNomTerritoire();
                        nbRegimentAttaquant = this.territoires.get(indexAtt).getRegimentSurTerritoire() - 1;
                        nbRegimentDefenseur = this.territoires.get(indexAtt).getTerritoires_adjacents().get(indexDef).getRegimentSurTerritoire();
                        message.setContent(nomTerritoireAttaque + "," + nomTerritoireDefense + "," + nbRegimentAttaquant + "," + nbRegimentDefenseur);
                        send(message);
                    } else {
                        if (!attaqueRevanche.isEmpty()) {
                            phaseCombatJoueur(true);
                        } else {
                            manoeuvreRegiment(); // lancement phase manoeuvre
                            territoireConcquis = false;
                        }
                    }
                } else
                {
                	window.println("Je n'ai pas encore recu d'attaque donc je n'ai aucune raison d'attaquer");
                	manoeuvreRegiment();
                }
                    
                
            }
        }
    }

    // fonction permettant d'obtenir le territoire adjacent ayant le moins de regiment et ayant le plus d'ecart avec le territoire qui peut l'attaquer
    private String findPositionLowestValue() {
        String position = "";
        int minValue = Integer.MAX_VALUE, ecart = Integer.MIN_VALUE;
        for (int i = 0; i < territoires.size(); i++) {
            for (int j = 0; j < this.territoires.get(i).getTerritoires_adjacents().size(); j++) {
                Territoire courantTerritoire = this.territoires.get(i).getTerritoires_adjacents().get(j);
                if (territoireNotContains(courantTerritoire)) {
                    if ((courantTerritoire.getRegimentSurTerritoire() <= minValue) &&
                            (this.territoires.get(i).getRegimentSurTerritoire() - courantTerritoire.getRegimentSurTerritoire() >= ecart) &&
                            (this.territoires.get(i).getRegimentSurTerritoire() > 1) /* alors assez d unite pour attaque */) {
                        ecart = this.territoires.get(i).getRegimentSurTerritoire() - courantTerritoire.getRegimentSurTerritoire();
                        minValue = courantTerritoire.getRegimentSurTerritoire();
                        position = i + "," + j;
                    }
                }
            }
        }
        return position;
    }

    private int findPositionLowestValue(Territoire t) {
        int position = -1;
        int minValue = Integer.MAX_VALUE, ecart = Integer.MIN_VALUE;
        for (Territoire terAdj : t.getTerritoires_adjacents()) {
            if(territoireNotContains(terAdj)) {
                int i = t.getTerritoires_adjacents().indexOf(terAdj);
                if ((terAdj.getRegimentSurTerritoire() <= minValue) &&
                        (t.getRegimentSurTerritoire() - terAdj.getRegimentSurTerritoire() >= ecart)) {
                    ecart = t.getRegimentSurTerritoire() - terAdj.getRegimentSurTerritoire();
                    minValue = terAdj.getRegimentSurTerritoire();
                    position = i;
                }
            }
        }
        return position;
    }

    private String findPositionBiggestGap() {
        String position = "";
        int ecart = Integer.MIN_VALUE;
        for (int i = 0; i < territoires.size(); i++) {
            for (int j = 0; j < this.territoires.get(i).getTerritoires_adjacents().size(); j++) {
                Territoire courantTerritoire = this.territoires.get(i).getTerritoires_adjacents().get(j);
                if (territoireNotContains(courantTerritoire)) {
                    if ((this.territoires.get(i).getRegimentSurTerritoire() - courantTerritoire.getRegimentSurTerritoire() >= ecart) &&
                            (this.territoires.get(i).getRegimentSurTerritoire() > 1) /* alors assez d unite pour attaque */) {
                        ecart = this.territoires.get(i).getRegimentSurTerritoire() - courantTerritoire.getRegimentSurTerritoire();
                        position = i + "," + j;
                    }
                }
            }
        }
        return position;
    }

    private String findPositionBiggestGap(Territoire t) {
        String position = "";
        int ecart = Integer.MIN_VALUE;
        for (int i = 0; i < territoires.size(); i++) {
            for (int j = 0; j < this.territoires.get(i).getTerritoires_adjacents().size(); j++) {
                Territoire courantTerritoire = this.territoires.get(i).getTerritoires_adjacents().get(j);
                if(courantTerritoire.getNomTerritoire().equals(t.getNomTerritoire())) {
                    if (territoireNotContains(courantTerritoire)) {
                        if ((this.territoires.get(i).getRegimentSurTerritoire() - courantTerritoire.getRegimentSurTerritoire() >= ecart) &&
                                (this.territoires.get(i).getRegimentSurTerritoire() > 1) /* alors assez d unite pour attaque */) {
                            ecart = this.territoires.get(i).getRegimentSurTerritoire() - courantTerritoire.getRegimentSurTerritoire();
                            position = i + "," + j;
                        }
                    }
                }
            }
        }
        return position;
    }


    // fonction qui permet de renseigner si un territoire est contenu dans la variable territoires
    private boolean territoireNotContains(Territoire t) {
        for (Territoire ter : territoires) {
            if (ter.getNomTerritoire().equals(t.getNomTerritoire()))
                return false;
        }
        return true;
    }

    private void manoeuvreRegiment() {
        window.println("\nDebut phase manoeuvre");
        Random rand = new Random();
        boolean manoeuvre = rand.nextBoolean();
        int noTerritoireListMinus, noTerritoireMinus, noTerritoireAdd, nbRegiment = 0;
        Territoire terAdd = null, terMinus = null;
        if (manoeuvre) {
            List<List<Territoire>> tempTAdd = new ArrayList<>();        //  Liste temporaire des territoires qui pourront recevoir des regiments
            List<List<Territoire>> tempTMinus = new ArrayList<>();      //  Liste temporaire des territoires qui pourront retirer des regiments
            for (Territoire t1 : territoires) {     //  cherche si une manoeuvre est disponible
                List<Territoire> temp2TAdd = new ArrayList<>();
                List<Territoire> temp2TMinus = new ArrayList<>();
                for (Territoire t2 : t1.getTerritoires_adjacents()) {
                    for (Territoire t3 : territoires) {
                        if (t3.getNomTerritoire().equals(t2.getNomTerritoire())) {
                            if (!temp2TAdd.contains(t1)) {
                                temp2TMinus.add(t1);
                                temp2TAdd.add(t1);
                            }
                            temp2TMinus.add(t2);
                            temp2TAdd.add(t2);
                        }
                    }
                }
                if (!temp2TAdd.isEmpty())
                    tempTAdd.add(temp2TAdd);
                if (!temp2TMinus.isEmpty())
                    tempTMinus.add(temp2TMinus);
            }
            List<Integer> indexRemove = new ArrayList<>();
            for (int k = (tempTMinus.size() - 1); k >= 0; --k) {    // remove territoire avec 1 regiment de tempTMinus
                tempTMinus.get(k).removeIf(t -> (t.getRegimentSurTerritoire() <= 1));
                if (tempTMinus.get(k).isEmpty()) {    // remove liste de tempTMinus si vide
                    tempTMinus.remove(k);
                    indexRemove.add(k);
                }
            }
            for (int i : indexRemove) {     // remove liste de tempTAdd si une liste de tempTMinus a ete remove
                tempTAdd.remove(i);
            }
            if (!tempTAdd.isEmpty()) {
                switch (this.strategie) {
                    case "aleatoire", "passive" -> {
                        if (!tempTMinus.isEmpty()) {
                            noTerritoireListMinus = rand.nextInt(tempTMinus.size());
                            List<Territoire> listTemp = new ArrayList<>(tempTMinus.get(noTerritoireListMinus));
                            noTerritoireMinus = rand.nextInt(listTemp.size());
                            nbRegiment = rand.nextInt((listTemp.get(noTerritoireMinus).getRegimentSurTerritoire() - 1)) + 1;
                            terMinus = getTerritoireByName(listTemp.get(noTerritoireMinus).getNomTerritoire());
                            terMinus.addRegimentSurTerritoire(-nbRegiment);
                            tempTAdd.get(noTerritoireListMinus).remove(terMinus);
                            noTerritoireAdd = rand.nextInt(tempTAdd.get(noTerritoireListMinus).size());
                            terAdd = getTerritoireByName(tempTAdd.get(noTerritoireListMinus).get(noTerritoireAdd).getNomTerritoire());
                            terAdd.addRegimentSurTerritoire(nbRegiment);
                            window.println("\nManoeuvre effectue\n");
                        }
                    }
                    case "attaque" -> {
                        String position = findPositionLowestValue();
                        if (!position.isEmpty()) {
                            String[] string = position.split(",");
                            int posT = Integer.parseInt(string[0]);
                            Territoire t = territoires.get(posT);
                            int indexList = 0;
                            for (List<Territoire> listTemp : tempTAdd) {
                                if (listTemp.get(0).getNomTerritoire().equals(t.getNomTerritoire())) {
                                    indexList = tempTAdd.indexOf(listTemp);
                                }
                            }
                            terAdd = getTerritoireByName(tempTAdd.get(indexList).get(0).getNomTerritoire());
                            if (!tempTMinus.get(indexList).isEmpty()) {
                                int indexMinus = indexOfBiggestValue(tempTMinus.get(indexList), terAdd);
                                if (indexMinus != -1) {
                                    terMinus = getTerritoireByName(tempTMinus.get(indexList).get(indexMinus).getNomTerritoire());
                                    nbRegiment = terMinus.getRegimentSurTerritoire() - 1;
                                    terMinus.addRegimentSurTerritoire(-nbRegiment);
                                    terAdd.addRegimentSurTerritoire(nbRegiment);

                                }
                            }
                        }
                    }
                    case "defense" -> {
                        int indexList = indexGreatestDanger(tempTAdd);
                        terAdd = getTerritoireByName(tempTAdd.get(indexList).get(0).getNomTerritoire());
                        if (!tempTMinus.get(indexList).isEmpty()) {
                            int indexMinus = indexOfBiggestValue(tempTMinus.get(indexList), terAdd);
                            if (indexMinus != -1) {
                                terMinus = getTerritoireByName(tempTMinus.get(indexList).get(indexMinus).getNomTerritoire());
                                nbRegiment = terMinus.getRegimentSurTerritoire() - 1;
                                terMinus.addRegimentSurTerritoire(-nbRegiment);
                                terAdd.addRegimentSurTerritoire(nbRegiment);
                            }
                        }
                    }
                    case "equilibre", "revanche" -> {
                        String position = indexWeakestTerritoire(tempTAdd);
                        if (!position.isEmpty()) {
                            String[] string = position.split(",");
                            int indexList = Integer.parseInt(string[0]);
                            int indexAdd = Integer.parseInt(string[1]);
                            terAdd = getTerritoireByName(tempTAdd.get(indexList).get(indexAdd).getNomTerritoire());
                            if (!tempTMinus.get(indexList).isEmpty()) {
                                int indexMinus = indexOfBiggestValue(tempTMinus.get(indexList), terAdd);
                                if (indexMinus != -1) {
                                    terMinus = getTerritoireByName(tempTMinus.get(indexList).get(indexMinus).getNomTerritoire());
                                    nbRegiment = (terMinus.getRegimentSurTerritoire() - terAdd.getRegimentSurTerritoire()) / 2;
                                    terMinus.addRegimentSurTerritoire(-nbRegiment);
                                    terAdd.addRegimentSurTerritoire(nbRegiment);
                                }
                            }
                        }
                    }
                }
            }
        }

        String changementManoeuvre = null;
        if (terAdd != null && terMinus != null) // set des changements de la manoeuvre
        {
            window.println("Envoie changement a cause de la manoeuvre."
            		+ "\n" + terMinus.getNomTerritoire() + " a envoye " + nbRegiment + " regiment(s) au territoire " + terAdd.getNomTerritoire()
            		+ "\n" + terMinus.getNomTerritoire() + " possede " + terMinus.getRegimentSurTerritoire() + " regiments.\n"
            				+ terAdd.getNomTerritoire() + " possede " + terAdd.getRegimentSurTerritoire() + " regiments.");
            changementManoeuvre = terAdd.getNomTerritoire() + "," + terAdd.getRegimentSurTerritoire() + "," + terMinus.getNomTerritoire() + "," + terMinus.getRegimentSurTerritoire();
        }

        updateContinents(changementManoeuvre); // fonction pour savoir si le joueur rempli les conditions de victoire de sa carte Mission

    }

    /*
     *  fonction pour savoir si le joueur rempli les conditions de victoire de sa carte Mission
     */
    private void verifVictoire() {
        boolean finPartie = false;
        int i;
        String affichage;

        //preparation du modele d'envoie de la fin de partie, peut ne pas etre envoye si la condition de victoire n est pas remplie
        ACLMessage message1 = new ACLMessage(ACLMessage.REQUEST);
        message1.setConversationId("victoire / fin de partie");
        message1.addReceiver(new AID(intermediaire.getLocalName(), AID.ISLOCALNAME));

        //1er type de mission
        if (objectif.getTypeMission().equals(TypeMission.CONTINENTS.toString())) {
            boolean missionRemplie = false;
            int nbConditionsRemplies = 0;
            for (i = 0; i < 2; i++) // verif si les 2 continents demandes sont concquis
            {
                if (continents.contains(objectif.getContinentAConquerir().get(i))) {
                    nbConditionsRemplies++;
                }
            }
            if (nbConditionsRemplies == 2) // alors les 2 continents demandes sont concquis
            {
                missionRemplie = true;
            }

            if (missionRemplie) {
                if (objectif.getContinentAConquerir().size() == 2) // alors pas de "Autre", victoire
                {
                    finPartie = true;
                    affichage = "Le " + this.getLocalName() + " a gagne la partie,"
                            + "\ncar il a complete sa mission de concquerir les territoires " + objectif.getContinentAConquerir().get(0)
                            + " et " + objectif.getContinentAConquerir().get(1) + ".";
                    window.println("\nEnvoie fin de partie a Intermediaire. ");
                    message1.setContent(affichage);
                    send(message1);
                } else // besoin de ses 2 continents + un "Autre" au choix, si oui, victoire, sinon, la partie continue
                {
                    if (continents.size() >= 3) // alors victoire
                    {
                        finPartie = true;
                        affichage = "Le " + this.getLocalName() + " a gagne la partie,"
                                + "\ncar il a complete sa mission de concquerir les territoires " + objectif.getContinentAConquerir().get(0)
                                + ", " + objectif.getContinentAConquerir().get(1) + " et un autre de son choix.";
                        window.println("\nEnvoie fin de partie a Intermediaire. ");
                        message1.setContent(affichage);
                        send(message1);
                    }
                }
            }
        }

        //2eme type de mission
        if (objectif.getTypeMission().equals(TypeMission.COULEUR.toString())) {
            if (armees_eliminees.contains(objectif.getCouleur())) //alors victoire
            {
                finPartie = true;
                affichage = "Le " + this.getLocalName() + " a gagne la partie,"
                        + "\ncar il a complete sa mission d eliminer les armees " + objectif.getCouleur() + ".";
                window.println("\nEnvoie fin de partie a Intermediaire. ");
                message1.setContent(affichage);
                send(message1);
            }
        }

        //3eme type de mission
        if (objectif.getTypeMission().equals(TypeMission.TERRITOIRES.toString())) {
            if (territoires.size() == objectif.getNbTerritoire()) //alors victoire
            {
                finPartie = true;
                affichage = "Le " + this.getLocalName() + " a gagne la partie,"
                        + "\ncar il a complete sa mission de concquerir " + objectif.getNbTerritoire() + " territoires.";
                window.println("\nEnvoie fin de partie a Intermediaire. ");
                message1.setContent(affichage);
                send(message1);
            }
        }

        //4eme type de mission
        if (objectif.getTypeMission().equals(TypeMission.TERRITOIRES_ET_ARMEES_MIN.toString())) {
            if (territoires.size() == objectif.getNbTerritoire()) //alors verif
            {
                //Verif si assez d armees sur le nombre de territoire concquis demandes
                int nbTerritoiresAvecNbArmeesRecquis = 0;
                for (i = 0; i < territoires.size(); i++) {
                    if (territoires.get(i).getRegimentSurTerritoire() >= objectif.getNbArmee()) {
                        nbTerritoiresAvecNbArmeesRecquis++;
                    }
                }

                if (nbTerritoiresAvecNbArmeesRecquis >= objectif.getNbTerritoire()) // alors victoire
                {
                    finPartie = true;
                    affichage = "Le " + this.getLocalName() + " a gagne la partie,"
                            + "\ncar il a complete sa mission de concquerir " + objectif.getNbTerritoire() + " territoires "
                            + " avec au moins " + objectif.getNbArmee() + " sur ce nombre de territoire a concquerir.";
                    window.println("\nEnvoie fin de partie a Intermediaire. ");
                    message1.setContent(affichage);
                    send(message1);
                }
            }
        }

        if (!finPartie) // alors nouveau tour
        {
            window.println("\nEnvoie fin de tour a Intermediaire.");
            ACLMessage message2 = new ACLMessage(ACLMessage.REQUEST);
            message2.setConversationId("fin tour joueur");
            message2.addReceiver(new AID(intermediaire.getLocalName(), AID.ISLOCALNAME));
            send(message2);
        }
    }

    // fonction recuperant l'index du territoire avec le plus grand nombre de regiment d'une liste
    public int indexOfBiggestValue(List<Territoire> listTer, Territoire terAvoid) {
        int index = -1, maxValue = Integer.MIN_VALUE;
        for (Territoire ter : listTer) {
            if (!ter.getNomTerritoire().equals(terAvoid.getNomTerritoire()) && maxValue < ter.getRegimentSurTerritoire()) {
                maxValue = ter.getRegimentSurTerritoire();
                index = listTer.indexOf(ter);
            }
        }
        return index;
    }

    // fonction recuperant le territoire le plus en danger (qui a le plus de regiment ennemi adjacent)
    public int indexGreatestDanger(List<List<Territoire>> listTer) {
        int index = 0, regimentEnnemi = Integer.MIN_VALUE;
        for (List<Territoire> listTemp : listTer) {
            for (Territoire ter : listTemp) {
                int tempReg = 0;
                for (Territoire terAdj : ter.getTerritoires_adjacents()) {
                    if (territoireNotContains(terAdj)) {
                        tempReg += terAdj.getRegimentSurTerritoire();
                    }
                }
                if (tempReg > regimentEnnemi) {
                    regimentEnnemi = tempReg;
                    index = listTer.indexOf(listTemp);
                }
            }
        }
        return index;
    }

    public int indexGreatestDanger() {
        int index = 0, regimentEnnemi = Integer.MIN_VALUE;
        for (Territoire ter : territoires) {
            int tempReg = 0;
            for (Territoire terAdj : ter.getTerritoires_adjacents()) {
                if (territoireNotContains(terAdj)) {
                    tempReg += terAdj.getRegimentSurTerritoire();
                }
            }
            if (tempReg > regimentEnnemi) {
                regimentEnnemi = tempReg;
                index = territoires.indexOf(ter);
            }
        }
        return index;
    }

    // fonction recuperant le territoire possedant le moins de regiment
    public int indexWeakestTerritoire() {
        int index = 0, minValue = Integer.MAX_VALUE;
        for (Territoire t : territoires) {
            if (t.getRegimentSurTerritoire() < minValue) {
                index = territoires.indexOf(t);
                minValue = t.getRegimentSurTerritoire();
            }
        }
        return index;
    }

    // fonction recuperant le territoire possedant le moins de regiment par rapport a une liste donnee
    public String indexWeakestTerritoire(List<List<Territoire>> listAdd) {
        int minValue = Integer.MAX_VALUE;
        String index = "";
        for (List<Territoire> listTer : listAdd) {
            for (Territoire t : listTer) {
                if (t.getRegimentSurTerritoire() < minValue) {
                    index = listAdd.indexOf(listTer) + "," + listTer.indexOf(t);
                    minValue = t.getRegimentSurTerritoire();
                }
            }
        }
        return index;
    }

    public boolean boolAttaquable(Territoire t) {
        if (t != null)
            for (Territoire ter : territoires)
                for (Territoire terAdj : ter.getTerritoires_adjacents())
                    if (terAdj.getNomTerritoire().equals(t.getNomTerritoire()))
                        return false;
        return true;
    }

    public Territoire getTerritoireByName(String territoire) {
        for (Territoire t : territoires)
            if (t.getNomTerritoire().equals(territoire))
                return t;
        return null;
    }

    public Territoire getTerritoireAdjacentByName(String territoire) {
        for (Territoire t : territoires)
            for (Territoire terAdj : t.getTerritoires_adjacents())
                if (terAdj.getNomTerritoire().equals(territoire))
                    return terAdj;
        return null;
    }

    @Override
    protected void onGuiEvent(GuiEvent guiEvent) {
        if (guiEvent.getType() == Joueur.EXIT) {
            doDelete();
        }
    }
}
