package gui;

import agents.Joueur;
import jade.gui.GuiEvent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class JoueurGui extends JFrame {

    private static int nbJoueurGui = 0;
    private final int noJoueurGui;

    private final JTextArea jTextArea;

    private final Joueur myAgent;

    public JoueurGui(Joueur a) {
        super(a.getName());
        noJoueurGui = ++nbJoueurGui;
        myAgent = a;

        jTextArea = new JTextArea();
        jTextArea.setEditable(false);
        jTextArea.setColumns(40);
        jTextArea.setRows(5);
        JScrollPane jScrollPane = new JScrollPane(jTextArea);
        getContentPane().add(BorderLayout.CENTER, jScrollPane);

        // Make the agent terminate when the user closes
        // the GUI using the button on the upper right corner
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // SEND AN GUI EVENT TO THE AGENT !!!
                GuiEvent guiEv = new GuiEvent(this, Joueur.EXIT);
                myAgent.postGuiEvent(guiEv);
                // END SEND AN GUI EVENT TO THE AGENT !!!
            }
        });

        setResizable(true);
    }

    public void display() {
        pack();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int screenWidth = (int) screenSize.getWidth();
        int width = this.getWidth();
        int xx = (noJoueurGui * width) % screenWidth;
        int yy = ((noJoueurGui * width) / screenWidth) * getHeight();
        setLocation(xx, yy);
        setTitle(myAgent.getLocalName());
        setVisible(true);
    }

    public void println(String chaine) {
        String texte = jTextArea.getText();
        texte = texte + chaine + "\n";
        jTextArea.setText(texte);
        jTextArea.setCaretPosition(texte.length());
    }

    public int getNoJoueurGui() {
        return noJoueurGui;
    }

    public void setColor(Color color) {
        jTextArea.setBackground(color);
    }

    public JTextArea getjTextArea() {
        return jTextArea;
    }
}
