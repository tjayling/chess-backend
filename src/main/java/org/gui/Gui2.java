package org.gui;

import org.gui.chess.ChessAreaGui;
import org.gui.perft.PerftGuiController;
import org.gui.perft.PerftPanel;
import org.logic.Mediator;

import javax.swing.*;
import java.awt.*;

public class Gui2 extends JFrame {
  public Gui2() {
    setLayout(new BorderLayout());
    setTitle("Chess");
    setDefaultCloseOperation(EXIT_ON_CLOSE);
    setResizable(false);

    Mediator mediator = new Mediator();
    PerftGuiController controller = PerftGuiController.instantiate(mediator);

    PerftPanel perftPanel = new PerftPanel();
    ChessAreaGui chessAreaGui = new ChessAreaGui();

    add(chessAreaGui, BorderLayout.WEST);
    add(perftPanel, BorderLayout.EAST);

    controller.registerViews(perftPanel, chessAreaGui);

    pack();
    setLocationRelativeTo(null);
    setVisible(true); // Set visible to true on the Swing event dispatch thread
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(Gui2::new);
  }
}
