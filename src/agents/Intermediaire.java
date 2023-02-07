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

    /**
     * topic du joueur demandant les informations du territoire
     */
    AID topicTerritoire;
    AID topicRegimentTeritoire;
    AID topicRepartition;
    AID topicUpdateContinent;

    @SuppressWarnings({ "deprecation", "serial" })
	@Override
    protected void setup(){
        window = new gui.IntermediaireGui(this);
        window.display();
        window.setColor(Color.LIGHT_GRAY);
        window.println("Hello! Agent  " + getLocalName() + " is ready, my address is " + this.getAID().getName());

        plateau = new Monde();

        //AgentServicesTools.register(this, "intermediaire", "link");

        TopicManagementHelper topicHelper = null;
        try {
            topicHelper =  ( TopicManagementHelper ) getHelper (TopicManagementHelper.SERVICE_NAME) ;
            topicRepartition = topicHelper.createTopic("REPARTITION REGIMENT");
            //topicTerritoireRetour = topicHelper.createTopic("RETOUR INFO TERRITOIRE");
            //topicHelper.register(topicTerritoireRetour);
            topicHelper.register(topicRepartition);
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
        			
                    window.println("Message recu sur le topic " + topicTerritoire.getLocalName() + ". Contenu " + msg.getContent().toString()
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
                            System.out.println(nbTerritoire);
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

            window.println(plateau.toString());
        }));

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
