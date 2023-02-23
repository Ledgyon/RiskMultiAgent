package gui;

import agents.General;
import agents.Joueur;
import jade.gui.GuiEvent;

import javax.swing.*;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class GeneralGui extends JFrame {


    private final JTextArea jTextArea;

    private final General myAgent;
    public static final int EXIT = 0;

    public GeneralGui(General a) {
        super(a.getName());
        myAgent = a;

        jTextArea = new JTextArea();
        jTextArea.setBackground(new Color(255, 255, 240));
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
        
        JPanel p = new JPanel();
        p.setLayout(new GridLayout(0, 1, 0, 0));
        
        getContentPane().add(p, BorderLayout.SOUTH);
        
        JButton addButton = new JButton("Initialisation");
        addButton.addActionListener(event -> {
            try {
                // SEND AN GUI EVENT TO THE AGENT !!!
                GuiEvent guiEv = new GuiEvent(this, General.INITIALISATION_RISK);
                myAgent.postGuiEvent(guiEv);
                // END SEND AN GUI EVENT TO THE AGENT !!!
            } catch (Exception e) {
                JOptionPane.showMessageDialog(GeneralGui.this, "Invalid values. " + e.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
            // Permet de rendre le bouton inutilisable
            ((JButton)event.getSource()).setEnabled(false);
        });
        p.add(addButton);
    }

    public void display() {
        pack();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int screenWidth = (int) screenSize.getWidth();
        int width = this.getWidth();
        setTitle(myAgent.getLocalName());
        setVisible(true);
    }

    public void println(String chaine) {
        String texte = jTextArea.getText();
        texte = texte + chaine + "\n";
        jTextArea.setText(texte);
        jTextArea.setCaretPosition(texte.length());
    }

}
