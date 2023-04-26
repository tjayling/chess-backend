package org.gui;

import org.gui.perft.PerftGui;
import org.logic.Mediator;

import javax.swing.*;
import java.awt.*;

public class Gui extends JFrame {

    public Gui() {
        setLayout(new BorderLayout());
        setTitle("Chess");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        Mediator mediator = new Mediator();
        GuiController controller = new GuiController(mediator);

        PerftGui perftGui = new PerftGui(controller);
        ChessGui chessGui = new ChessGui(controller);

        add(chessGui, BorderLayout.WEST);
        add(perftGui, BorderLayout.EAST);

        controller.registerViews(perftGui, chessGui);

        pack();
        setLocationRelativeTo(null);
        setVisible(true); // Set visible to true on the Swing event dispatch thread
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(Gui::new);
    }
}
