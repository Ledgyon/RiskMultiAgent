package comportements;

import agents.Joueur;
import gui.JoueurGui;
import jade.core.AID;
import jade.core.Agent;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.proto.ContractNetInitiator;
import plateau.Territoire;

import java.io.IOException;
import java.util.*;


/**
 * Journey Buyer Behaviour by contract net
 *
 * @author revised by Emmanuel ADAM
 * @version 191017
 */
public class ContractNetAttaque extends ContractNetInitiator {
    /**
     * agent gui
     */
    private final JoueurGui window;

    /**
     * acheteur lie a ce comportement
     */
    private final Joueur monAgent;

    /**
     * initialisation
     *
     * @param agent       agent initiator
     * @param msg         initial message to send
     */
    public ContractNetAttaque(Agent agent, ACLMessage msg) {
        super();
        monAgent = (Joueur) agent;
        window = monAgent.getWindow();
        // définition du prococole
        msg.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
        // Réponse plus tard dans 1 sec
        msg.setReplyByDate(new Date(System.currentTimeMillis() + 1000));
        AID intermediaire = monAgent.getIntermediaire();
        vendeurs.forEach(msg::addReceiver);
        // relancer le comportement pour fixer la date de remise au plus tard, les destinataires, ...
        this.reset(msg);
    }


    /**
     * methode lancee a la reception de chaque refus
     *
     * @param refuse refus recu
     */
    @Override
    protected void handleRefuse(ACLMessage refuse) {
        window.println("Agent " + refuse.getSender().getLocalName() + " refuse");
    }

    /**
     * methode lancee a la reception d'un message d'erreur (impossibilite de poursuivre la vente)
     *
     * @param failure erreur recue
     */
    @Override
    protected void handleFailure(ACLMessage failure) {
        if (failure.getSender().equals(myAgent.getAMS())) {
            // ERREUR : le destinataire n'existe pas
            window.println("Le destinataire n'existe pas...");
        } else
            window.println("Agent " + failure.getSender().getLocalName() + " a echoue");
    }

    /**
     * methode lancee si toutes les reponses sont arrivees ou si le temps est ecoule<br>
     * accepte la meilleure offre, calcul basé sur la notoriété la plus haute et le prix le plus bas à part égale ici<br>
     * n'accepte pas l'offre si d�passe le prix max fix� par l'acheteur
     *
     * @param responses   reponses recues
     * @param acceptances vecteur des messages a transmettre en retour aux reponses recues
     * @see ContractNetInitiator#handleAllResponses(List, List)
     */
    @Override
    protected void handleAllResponses(List<ACLMessage> responses, List<ACLMessage> acceptances) {
        //catalog of journeys built from answers
        var catalogs = new JourneysList();
        var catalogs2 = new JourneysList();

        JourneysList receivedCatalog2 = null;

        //map <name to the agent (agence), Msg built to answer to it>
        Map<String, ACLMessage> reponses = new HashMap<>();
        for (ACLMessage ans : responses) {
            if (ans.getPerformative() == ACLMessage.PROPOSE) {
                JourneysList receivedCatalog = null;
                try {
                    receivedCatalog = (JourneysList) ans.getContentObject();
                    receivedCatalog2 = (JourneysList) ans.getContentObject();
                } catch (UnreadableException e) {
                    e.printStackTrace();
                }
                if (receivedCatalog != null) {
                    receivedCatalog2.removeIf(j->j.getPlaces()>0);
                    System.out.println(receivedCatalog2);
                    receivedCatalog.removeIf(j->j.getPlaces()==0);
                    catalogs.addJourneys(receivedCatalog);
                    catalogs2.addJourneys(receivedCatalog2);
                    monAgent.println("reçu de " + ans.getSender().getLocalName() + " : ");
                    monAgent.println(receivedCatalog.toString());
                }

            }
            var reply = ans.createReply();
            reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
            acceptances.add(reply);
            reponses.put(ans.getSender().getLocalName(), reply);
        }
        monAgent.setCatalogs(catalogs);
        monAgent.println("j'ai bien recu les calalogues : ");
        monAgent.println(catalogs.toString());
        monAgent.println("je fais mon choix...");

        monAgent.println("catalog2");
        monAgent.println(catalogs2.toString());
        monAgent.computeComposedJourney(from, to, departure, preference);
        //map <name to the agent (agence), list of journeys to buy to it>
        Map<String, ArrayList<Journey>> voyagesAAcheter = new HashMap<>();
        var journey = monAgent.getMyJourney();
        journey.getJourneys().forEach(j ->
                voyagesAAcheter.compute(j.getProposedBy(),
                        (agence, list) -> {
                            if (list == null) list = new ArrayList<>();
                            list.add(j);
                            return list;
                        }));
        voyagesAAcheter.forEach((agence, journeys) -> {
            var msg = reponses.get(agence);
            msg.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
            try {
                msg.setContentObject(journeys);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * methode lancee a la reception d'un message d'information (vente confirmee)
     *
     * @param inform message recu
     */
    @Override
    protected void handleInform(ACLMessage inform) {
        window.println("Agent " + inform.getSender().getLocalName() + " : " + inform.getContent());
    }

}
