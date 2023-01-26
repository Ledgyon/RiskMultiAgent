package launch;

import agents.General;
import plateau.Monde;

public class LaunchRisk {
    public LaunchRisk() {
    }

    public static void main(String[] args) {
        Monde plateau = new Monde();
        System.out.println(plateau);
        General general = new General();
        System.out.println("\n"+general);
    }
}

