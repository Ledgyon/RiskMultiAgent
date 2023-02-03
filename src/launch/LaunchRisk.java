package launch;

import agents.General;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.util.ExtendedProperties;
import plateau.Monde;

import java.util.Properties;

public class LaunchRisk {
    public LaunchRisk() {
    }

    public static void main(String[] args) {
        // preparer les arguments pout le conteneur JADE
        Properties prop = new ExtendedProperties();
        // demander la fenetre de controle
        prop.setProperty(Profile.GUI, "true");
        // add the Topic Management Service
        prop.setProperty(Profile.SERVICES, "jade.core.messaging.TopicManagementService;jade.core.event.NotificationService");
        // nommer les agents
        StringBuilder sb = new StringBuilder();
        for(int i = 1; i <= 6; i++)
            sb.append("Joueur_").append(i).append(":agents.Joueur;");
        sb.append("General:agents.General;");
        sb.append("Intermediaire:agents.Intermediaire;");
        prop.setProperty(Profile.AGENTS, sb.toString());
        // creer le profile pour le conteneur principal
        ProfileImpl profMain = new ProfileImpl(prop);
        // lancer le conteneur principal
        Runtime rt = Runtime.instance();
        rt.createMainContainer(profMain);

        /*Monde plateau = new Monde();
        System.out.println(plateau);
        General general = new General();
        System.out.println("\n"+general);*/
    }
}

