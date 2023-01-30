package agents;

import jade.gui.GuiAgent;
import jade.gui.GuiEvent;

import java.awt.*;

public class Intermediaire extends GuiAgent {

    private gui.IntermediaireGui window;
    public static final int EXIT = 0;

    @Override
    protected void setup(){
        window = new gui.IntermediaireGui(this);
        window.display();
        window.setColor(Color.LIGHT_GRAY);
        window.println("Hello! Agent  " + getLocalName() + " is ready, my address is " + this.getAID().getName());

    }

    @Override
    protected void onGuiEvent(GuiEvent guiEvent) {
        if (guiEvent.getType() == Intermediaire.EXIT) {
            doDelete();
        }
    }
}
