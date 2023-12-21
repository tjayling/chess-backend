package org.gui.chess;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JPanel;
import org.gui.perft.PerftGuiController;

public class ChessOptionsGui extends JPanel implements ActionListener {
  private final PerftGuiController controller;
  public ChessOptionsGui() {
    this.controller = PerftGuiController.getInstance();

    setFocusable(true);
    setBackground(Color.lightGray);

//    JCheckBox botModeToggle = new JCheckBox("Enable bot mode", true);
//    botModeToggle.addActionListener(e -> controller.setBotMode(botModeToggle.isSelected()));
//
//    add(botModeToggle);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    repaint();
  }
}
