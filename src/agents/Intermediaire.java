package agents;

import java.awt.Color;
import java.io.IOException;

import jade.core.AID;
import jade.core.AgentServicesTools;
import jade.core.ServiceException;
import jade.core.behaviours.ReceiverBehaviour;
import jade.core.messaging.TopicManagementHelper;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
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
    AID topicRepartition;

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


        topicTerritoire = AgentServicesTools.generateTopicAID(this, "INFO TERRITOIRE");
        
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
