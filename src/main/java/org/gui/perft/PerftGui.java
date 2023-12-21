package org.gui.perft;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import org.gui.chess.ChessAreaGui;
import org.gui.shared.HeaderPanel;
import org.logic.Mediator;

public class PerftGui extends JPanel {
  public PerftGui() {
    setLayout(new BorderLayout());

    Mediator mediator = new Mediator();
    PerftGuiController controller = PerftGuiController.instantiate(mediator);

    HeaderPanel headerPanel = new HeaderPanel();
    PerftPanel perftPanel = new PerftPanel();
    ChessAreaGui chessAreaGui = new ChessAreaGui();

    add(chessAreaGui, BorderLayout.WEST);
    add(perftPanel, BorderLayout.EAST);

    controller.registerViews(perftPanel, chessAreaGui);

    setVisible(true);
  }
}
