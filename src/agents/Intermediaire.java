package agents;

import java.awt.Color;

import jade.core.AID;
import jade.core.AgentServicesTools;
import jade.core.behaviours.ReceiverBehaviour;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import plateau.Monde;

public class Intermediaire extends GuiAgent {

    private gui.IntermediaireGui window;
    public static final int EXIT = 0;

    /**
     * topic du joueur demandant les informations du territoire
     */
    AID topicTerritoire;

    @Override
    protected void setup(){
        window = new gui.IntermediaireGui(this);
        window.display();
        window.setColor(Color.LIGHT_GRAY);
        window.println("Hello! Agent  " + getLocalName() + " is ready, my address is " + this.getAID().getName());

        Monde plateau = new Monde();
        topicTerritoire = AgentServicesTools.generateTopicAID(this, "INFO TERRITOIRE");
        //ecoute des messages radio
        addBehaviour(new ReceiverBehaviour(this, -1, MessageTemplate.MatchTopic(topicTerritoire), true, (a, m)->{
            window.println("Message recu sur le topic " + topicTerritoire.getLocalName() + ". Contenu " + m.getContent()
                    + " emis par :  " + m.getSender().getLocalName());

            /*
             * A FAIRE
             * prendre le territoire du plateau et le renvoyer au joueur que a fait la demande
             */
            ACLMessage retour = m.createReply();
            //plateau
            //retour.setContentObject();
        }));
    }

    @Override
    protected void onGuiEvent(GuiEvent guiEvent) {
        if (guiEvent.getType() == Intermediaire.EXIT) {
            doDelete();
        }
    }
}
